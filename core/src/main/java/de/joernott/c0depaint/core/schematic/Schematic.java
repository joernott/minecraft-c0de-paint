package de.joernott.c0depaint.core.schematic;

import de.joernott.c0depaint.core.canvas.Block;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A schematic of Litematica ({@code .litematic}) or a structure file of Minecraft and Create ({@code .nbt}), read
 * without Minecraft (task 9.4, docs/concept.md#schematics-020).
 *
 * <p>The voxels are kept as in the file (a palette and the packed or listed states) and are decoded while they are
 * visited, so a large schematic costs about as much memory as its file. The minimum corner of the schematic is 0, 0,
 * 0; air and structure voids are left out, so their voxels stay not set.
 */
public final class Schematic {
    /** The first data version of Minecraft 1.13, the oldest that is read. */
    public static final int MIN_DATA_VERSION = 1631;

    private static final Set<String> NOT_SET =
            Set.of("minecraft:air", "minecraft:cave_air", "minecraft:void_air", "minecraft:structure_void");

    /** The keys of block entity data that are not part of the data of a block: its position and type. */
    private static final Set<String> POSITION_KEYS = Set.of("x", "y", "z", "id");

    /** Upgrades blocks and block entity data from an older data version of Minecraft; the mod uses the DataFixer. */
    public interface Upgrader {
        Upgrader NONE = (block, version) -> block;

        /** The block (an entry of a palette, without data) as the current version of Minecraft knows it. */
        Block upgrade(Block block, int dataVersion);

        /**
         * The block with the data of its block entity: the compound of the file with its {@code id} (the type of the
         * block entity, which the data fixer needs); the data of the block is it without position and type.
         */
        default Block blockEntity(Block block, Map<String, Object> blockEntity, int dataVersion) {
            return block.withData(dataText(blockEntity));
        }
    }

    /** Visits a voxel of the schematic. */
    @FunctionalInterface
    public interface Visitor {
        void visit(int x, int y, int z, Block block);
    }

    /** A region: a box with its palette; the voxels are packed states (Litematica) or a list of positions. */
    private record Region(
            String name,
            int minX,
            int minY,
            int minZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            List<Block> palette,
            long[] packed,
            int bits,
            int[] listed,
            Map<Long, Map<String, Object>> data) {}

    private final String format;
    private final int dataVersion;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final int entities;
    private final List<Region> regions;

    private Schematic(
            String format, int dataVersion, int sizeX, int sizeY, int sizeZ, int entities, List<Region> regions) {
        this.format = format;
        this.dataVersion = dataVersion;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.entities = entities;
        this.regions = regions;
    }

    /**
     * Reads a schematic.
     *
     * @param name the name of the file; its extension decides the format
     * @param bytes the content of the file
     * @param maxBytes the most bytes the uncompressed data may have
     * @param region the only region of a {@code .litematic} to read, or null for all
     * @param upgrader upgrades blocks of older versions of Minecraft
     * @throws IllegalArgumentException if the file isn't a schematic of a supported format and version
     */
    public static Schematic read(String name, byte[] bytes, long maxBytes, String region, Upgrader upgrader) {
        String lower = name.toLowerCase(Locale.ROOT);
        Map<String, Object> root;
        try {
            root = Nbt.read(bytes, maxBytes);
        } catch (Nbt.NbtException e) {
            throw new IllegalArgumentException(name + " is not a schematic: " + e.getMessage());
        }
        if (lower.endsWith(".litematic")) {
            return litematic(name, root, region, upgrader);
        }
        if (lower.endsWith(".nbt")) {
            if (region != null && !region.isEmpty()) {
                throw new IllegalArgumentException(name + " has no regions; only a .litematic has");
            }
            return structure(name, root, upgrader);
        }
        throw new IllegalArgumentException(name + " is neither a .litematic nor a .nbt file");
    }

    // ---------- .litematic ----------

    private static Schematic litematic(String name, Map<String, Object> root, String only, Upgrader upgrader) {
        int version = integer(root, "Version", 0);
        int dataVersion = integer(root, "MinecraftDataVersion", 0);
        if (version > 7) {
            throw new IllegalArgumentException(name + " has the Litematica version " + version + ", newer than 7");
        }
        if (version < 5 || dataVersion < MIN_DATA_VERSION) {
            throw new IllegalArgumentException(name + " was saved before Minecraft 1.13 and can't be read");
        }
        Map<String, Object> regionsTag = compound(root, "Regions");
        if (only != null && !only.isEmpty() && !regionsTag.containsKey(only)) {
            throw new IllegalArgumentException(name + " has no region " + only + "; it has " + regionsTag.keySet());
        }
        List<Region> regions = new ArrayList<>();
        int entities = 0;
        for (Map.Entry<String, Object> entry : regionsTag.entrySet()) {
            if (only != null && !only.isEmpty() && !entry.getKey().equals(only)) {
                continue;
            }
            Map<String, Object> tag = asCompound(entry.getValue(), name);
            int[] position = xyz(compound(tag, "Position"));
            int[] size = xyz(compound(tag, "Size"));
            int[] min = new int[3];
            int[] abs = new int[3];
            for (int axis = 0; axis < 3; axis++) {
                if (size[axis] == 0) {
                    throw new IllegalArgumentException(name + ": the region " + entry.getKey() + " is empty");
                }
                int end = position[axis] + (size[axis] > 0 ? size[axis] - 1 : size[axis] + 1);
                min[axis] = Math.min(position[axis], end);
                abs[axis] = Math.abs(size[axis]);
            }
            List<Block> palette = palette(list(tag, "BlockStatePalette"), name, dataVersion, upgrader);
            int bits = Math.max(2, 32 - Integer.numberOfLeadingZeros(Math.max(1, palette.size() - 1)));
            long volume = (long) abs[0] * abs[1] * abs[2];
            if (!(tag.get("BlockStates") instanceof long[] packed) || packed.length < (volume * bits + 63) / 64) {
                throw new IllegalArgumentException(name + ": the block states of " + entry.getKey() + " are missing");
            }
            Map<Long, Map<String, Object>> data = new HashMap<>();
            for (Object element : list(tag, "TileEntities")) {
                Map<String, Object> blockEntity = asCompound(element, name);
                int x = integer(blockEntity, "x", -1);
                int y = integer(blockEntity, "y", -1);
                int z = integer(blockEntity, "z", -1);
                if (x < 0 || y < 0 || z < 0 || x >= abs[0] || y >= abs[1] || z >= abs[2]) {
                    continue;
                }
                data.put(key(x, y, z), blockEntity);
            }
            entities += list(tag, "Entities").size();
            regions.add(new Region(
                    entry.getKey(), min[0], min[1], min[2], abs[0], abs[1], abs[2], palette, packed, bits, null, data));
        }
        if (regions.isEmpty()) {
            throw new IllegalArgumentException(name + " has no regions");
        }
        return normalized("litematic", dataVersion, entities, regions);
    }

    // ---------- .nbt (structures of Minecraft and Create) ----------

    private static Schematic structure(String name, Map<String, Object> root, Upgrader upgrader) {
        int dataVersion = integer(root, "DataVersion", 0);
        if (dataVersion < MIN_DATA_VERSION) {
            throw new IllegalArgumentException(name + " was saved before Minecraft 1.13 and can't be read");
        }
        int[] size = xyz(list(root, "size"), name);
        List<Object> paletteTag = list(root, "palette");
        if (paletteTag.isEmpty() && !list(root, "palettes").isEmpty()) {
            // Structures with several palettes (shipwrecks): the first one
            paletteTag = asList(list(root, "palettes").getFirst(), name);
        }
        List<Block> palette = palette(paletteTag, name, dataVersion, upgrader);
        List<Object> blocks = list(root, "blocks");
        int[] listed = new int[blocks.size() * 4];
        Map<Long, Map<String, Object>> data = new HashMap<>();
        for (int i = 0; i < blocks.size(); i++) {
            Map<String, Object> block = asCompound(blocks.get(i), name);
            int[] pos = xyz(list(block, "pos"), name);
            int state = integer(block, "state", -1);
            if (state < 0 || state >= palette.size()) {
                throw new IllegalArgumentException(name + ": a block has the state " + state + ", not in the palette");
            }
            for (int axis = 0; axis < 3; axis++) {
                if (pos[axis] < 0 || pos[axis] >= size[axis]) {
                    throw new IllegalArgumentException(name + ": a block lies outside of the size of the structure");
                }
            }
            listed[i * 4] = pos[0];
            listed[i * 4 + 1] = pos[1];
            listed[i * 4 + 2] = pos[2];
            listed[i * 4 + 3] = state;
            if (block.get("nbt") instanceof Map<?, ?> nbt) {
                data.put(key(pos[0], pos[1], pos[2]), cast(nbt));
            }
        }
        Region region = new Region("", 0, 0, 0, size[0], size[1], size[2], palette, null, 0, listed, data);
        return normalized("nbt", dataVersion, list(root, "entities").size(), List.of(region));
    }

    // ---------- common ----------

    /** Moves the regions so that the minimum corner of the schematic is 0, 0, 0. */
    private static Schematic normalized(String format, int dataVersion, int entities, List<Region> regions) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        long maxX = Long.MIN_VALUE;
        long maxY = Long.MIN_VALUE;
        long maxZ = Long.MIN_VALUE;
        for (Region r : regions) {
            minX = Math.min(minX, r.minX);
            minY = Math.min(minY, r.minY);
            minZ = Math.min(minZ, r.minZ);
            maxX = Math.max(maxX, (long) r.minX + r.sizeX);
            maxY = Math.max(maxY, (long) r.minY + r.sizeY);
            maxZ = Math.max(maxZ, (long) r.minZ + r.sizeZ);
        }
        List<Region> moved = new ArrayList<>();
        for (Region r : regions) {
            moved.add(new Region(
                    r.name,
                    r.minX - minX,
                    r.minY - minY,
                    r.minZ - minZ,
                    r.sizeX,
                    r.sizeY,
                    r.sizeZ,
                    r.palette,
                    r.packed,
                    r.bits,
                    r.listed,
                    r.data));
        }
        return new Schematic(
                format, dataVersion, (int) (maxX - minX), (int) (maxY - minY), (int) (maxZ - minZ), entities, moved);
    }

    private static List<Block> palette(List<Object> tag, String name, int dataVersion, Upgrader upgrader) {
        if (tag.isEmpty()) {
            throw new IllegalArgumentException(name + " has an empty palette");
        }
        List<Block> palette = new ArrayList<>();
        for (Object element : tag) {
            Map<String, Object> entry = asCompound(element, name);
            if (!(entry.get("Name") instanceof String id) || id.isEmpty()) {
                throw new IllegalArgumentException(name + ": an entry of the palette has no name");
            }
            Map<String, String> properties = new LinkedHashMap<>();
            if (entry.get("Properties") instanceof Map<?, ?> states) {
                states.forEach((key, value) -> properties.put((String) key, String.valueOf(value)));
            }
            Block block = Block.of(id.contains(":") ? id : "minecraft:" + id, properties);
            palette.add(NOT_SET.contains(block.id()) ? null : upgrader.upgrade(block, dataVersion));
        }
        return palette;
    }

    /** The data of a block entity as SNBT, without its position and type. */
    public static String dataText(Map<String, Object> blockEntity) {
        Map<String, Object> data = new LinkedHashMap<>(blockEntity);
        POSITION_KEYS.forEach(data::remove);
        return data.isEmpty() ? "" : Nbt.snbt(data);
    }

    /**
     * Visits every voxel that isn't air, with its block and the data of its block entity; regions that overlap visit
     * a voxel more than once, the later region last.
     */
    public void forEach(Visitor visitor, Upgrader upgrader) {
        for (Region r : regions) {
            if (r.packed != null) {
                long mask = (1L << r.bits) - 1;
                long index = 0;
                for (int y = 0; y < r.sizeY; y++) {
                    for (int z = 0; z < r.sizeZ; z++) {
                        for (int x = 0; x < r.sizeX; x++, index++) {
                            long bit = index * r.bits;
                            int word = (int) (bit >>> 6);
                            int offset = (int) (bit & 63);
                            long value = r.packed[word] >>> offset;
                            if (offset + r.bits > 64) {
                                value |= r.packed[word + 1] << (64 - offset);
                            }
                            int state = (int) (value & mask);
                            visit(r, x, y, z, state, visitor, upgrader);
                        }
                    }
                }
            } else {
                for (int i = 0; i < r.listed.length; i += 4) {
                    visit(r, r.listed[i], r.listed[i + 1], r.listed[i + 2], r.listed[i + 3], visitor, upgrader);
                }
            }
        }
    }

    private void visit(Region r, int x, int y, int z, int state, Visitor visitor, Upgrader upgrader) {
        if (state >= r.palette.size()) {
            throw new IllegalArgumentException("A voxel of the region " + r.name + " has the state " + state
                    + ", but the palette has " + r.palette.size());
        }
        Block block = r.palette.get(state);
        if (block == null) {
            return;
        }
        Map<String, Object> blockEntity = r.data.get(key(x, y, z));
        if (blockEntity != null) {
            block = upgrader.blockEntity(block, blockEntity, dataVersion);
        }
        visitor.visit(r.minX + x, r.minY + y, r.minZ + z, block);
    }

    public String format() {
        return format;
    }

    public int dataVersion() {
        return dataVersion;
    }

    public int sizeX() {
        return sizeX;
    }

    public int sizeY() {
        return sizeY;
    }

    public int sizeZ() {
        return sizeZ;
    }

    /** The entities of the file, which a canvas leaves out. */
    public int entities() {
        return entities;
    }

    /** The names of the regions (one region named "" for a .nbt file). */
    public List<String> regions() {
        return regions.stream().map(Region::name).toList();
    }

    // ---------- tags ----------

    private static long key(int x, int y, int z) {
        return ((long) x << 42) | ((long) y << 21) | z;
    }

    private static int integer(Map<String, Object> tag, String key, int otherwise) {
        return tag.get(key) instanceof Number number ? number.intValue() : otherwise;
    }

    private static Map<String, Object> compound(Map<String, Object> tag, String key) {
        return tag.get(key) instanceof Map<?, ?> map ? cast(map) : Map.of();
    }

    private static List<Object> list(Map<String, Object> tag, String key) {
        return tag.get(key) instanceof List<?> list ? List.copyOf(list) : List.of();
    }

    private static Map<String, Object> asCompound(Object value, String name) {
        if (value instanceof Map<?, ?> map) {
            return cast(map);
        }
        throw new IllegalArgumentException(name + " has an element that isn't a compound");
    }

    private static List<Object> asList(Object value, String name) {
        if (value instanceof List<?> list) {
            return List.copyOf(list);
        }
        throw new IllegalArgumentException(name + " has a palette that isn't a list");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> cast(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }

    private static int[] xyz(Map<String, Object> tag) {
        return new int[] {integer(tag, "x", 0), integer(tag, "y", 0), integer(tag, "z", 0)};
    }

    private static int[] xyz(List<Object> list, String name) {
        if (list.size() != 3
                || !(list.get(0) instanceof Number x)
                || !(list.get(1) instanceof Number y)
                || !(list.get(2) instanceof Number z)) {
            throw new IllegalArgumentException(name + " has a position or size that isn't three numbers");
        }
        return new int[] {x.intValue(), y.intValue(), z.intValue()};
    }
}
