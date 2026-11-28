package de.joernott.c0depaint.core.schematic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0depaint.core.canvas.Block;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/** Reading schematics of Litematica and Create without Minecraft (task 9.4). */
class SchematicTest {
    private static final long MAX = 16 * 1024 * 1024;

    // ---------- helpers that write files like Litematica and Create ----------

    private static Map<String, Object> compound(Object... keysAndValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }

    private static Map<String, Object> state(String name, Object... properties) {
        return properties.length == 0
                ? compound("Name", name)
                : compound("Name", name, "Properties", compound(properties));
    }

    /** Packs palette indices like LitematicaBitArray: bits per entry, spanning two longs where needed. */
    private static long[] pack(int[] states, int bits) {
        long[] packed = new long[(int) (((long) states.length * bits + 63) / 64)];
        for (int i = 0; i < states.length; i++) {
            long bit = (long) i * bits;
            int word = (int) (bit >>> 6);
            int offset = (int) (bit & 63);
            packed[word] |= (long) states[i] << offset;
            if (offset + bits > 64) {
                packed[word + 1] |= (long) states[i] >>> (64 - offset);
            }
        }
        return packed;
    }

    private static Map<String, Object> region(
            int[] position, int[] size, List<Object> palette, int[] states, List<Object> tileEntities) {
        int bits = Math.max(2, 32 - Integer.numberOfLeadingZeros(Math.max(1, palette.size() - 1)));
        return compound(
                "Position", compound("x", position[0], "y", position[1], "z", position[2]),
                "Size", compound("x", size[0], "y", size[1], "z", size[2]),
                "BlockStatePalette", palette,
                "BlockStates", pack(states, bits),
                "TileEntities", tileEntities,
                "Entities", List.of(),
                "PendingBlockTicks", List.of());
    }

    private static byte[] litematic(int version, int dataVersion, Map<String, Object> regions) {
        return Nbt.write(compound(
                "MinecraftDataVersion", dataVersion,
                "Version", version,
                "SubVersion", 1,
                "Metadata", compound("Name", "test", "Author", "tester"),
                "Regions", regions));
    }

    private static Map<List<Integer>, Block> voxels(Schematic schematic) {
        Map<List<Integer>, Block> voxels = new java.util.HashMap<>();
        schematic.forEach((x, y, z, block) -> voxels.put(List.of(x, y, z), block), Schematic.Upgrader.NONE);
        return voxels;
    }

    // ---------- tests ----------

    @Test
    void nbtComesBackAsItWasWritten() {
        Map<String, Object> root = compound(
                "b",
                (byte) 1,
                "s",
                (short) 2,
                "i",
                3,
                "l",
                4L,
                "f",
                1.5f,
                "d",
                2.5d,
                "text",
                "hi \"you\"",
                "bytes",
                new byte[] {1, 2},
                "ints",
                new int[] {3, 4},
                "longs",
                new long[] {5L},
                "list",
                List.of(compound("a", 1), compound("a", 2)),
                "empty",
                List.of());
        Map<String, Object> read = Nbt.read(Nbt.write(root), MAX);
        assertEquals(root.keySet(), read.keySet());
        assertEquals(
                "{b:1b,s:2s,i:3,l:4L,f:1.5f,d:2.5d,text:\"hi \\\"you\\\"\",bytes:[B;1B,2B],ints:[I;3,4],longs:[L;5L],"
                        + "list:[{a:1},{a:2}],empty:[]}",
                Nbt.snbt(read));
        assertEquals("{\"odd key\":1}", Nbt.snbt(compound("odd key", 1)));
    }

    @Test
    void hostileNbtIsRefused() {
        assertThrows(Nbt.NbtException.class, () -> Nbt.read(new byte[] {1, 2, 3}, MAX));
        // Larger than allowed when unpacked
        byte[] big = Nbt.write(compound("data", new byte[100_000]));
        assertThrows(Nbt.NbtException.class, () -> Nbt.read(big, 10_000));
        // Deeply nested lists
        Object nested = List.of();
        for (int i = 0; i < 600; i++) {
            nested = List.of(nested);
        }
        byte[] deep = Nbt.write(compound("deep", nested));
        assertThrows(Nbt.NbtException.class, () -> Nbt.read(deep, MAX));
    }

    @Test
    void litematicRegionsArePackedAcrossLongs() {
        // Five states need 3 bits, so entries span two longs at bit 63
        List<Object> palette = List.of(
                state("minecraft:air"),
                state("minecraft:stone"),
                state("minecraft:oak_stairs", "facing", "east", "half", "bottom"),
                state("minecraft:chest", "facing", "north"),
                state("create:shaft", "axis", "y"));
        int[] size = {5, 3, 4};
        int[] states = new int[60];
        for (int i = 0; i < states.length; i++) {
            states[i] = i % 5;
        }
        List<Object> tileEntities = List.of(compound(
                "x",
                3,
                "y",
                0,
                "z",
                0,
                "id",
                "minecraft:chest",
                "Items",
                List.of(compound("Slot", (byte) 0, "id", "minecraft:stone", "count", 3))));
        byte[] file =
                litematic(6, 3700, compound("main", region(new int[] {0, 0, 0}, size, palette, states, tileEntities)));
        Schematic schematic = Schematic.read("castle.litematic", file, MAX, null, Schematic.Upgrader.NONE);
        assertEquals(5, schematic.sizeX());
        assertEquals(3, schematic.sizeY());
        assertEquals(4, schematic.sizeZ());
        Map<List<Integer>, Block> voxels = voxels(schematic);
        // Index = y*sizeX*sizeZ + z*sizeX + x; every fifth one is air and not set
        assertEquals(48, voxels.size());
        assertEquals(new Block("minecraft:stone"), voxels.get(List.of(1, 0, 0)));
        // Index 21 is x 1, y 1, z 0 (21 = 1 * 20 + 1): 21 % 5 = 1, stone
        assertEquals(new Block("minecraft:stone"), voxels.get(List.of(1, 1, 0)));
        // Index 22: x 2, y 1, z 0 -> 22 % 5 = 2, stairs (bits 66..68 span the first and second long)
        assertEquals(
                Block.of("minecraft:oak_stairs", Map.of("facing", "east", "half", "bottom")),
                voxels.get(List.of(2, 1, 0)));
        Block chest = voxels.get(List.of(3, 0, 0));
        assertEquals("minecraft:chest", chest.id());
        assertEquals("{Items:[{Slot:0b,id:\"minecraft:stone\",count:3}]}", chest.data());
        assertEquals("create:shaft", voxels.get(List.of(4, 0, 0)).id());
    }

    @Test
    void negativeSizesAndSeveralRegionsKeepTheirPlaces() {
        List<Object> palette = List.of(state("minecraft:air"), state("minecraft:stone"));
        // Region a: from 0,0,0 one voxel; region b: Position 4,0,0 with size -2,1,1 covers x 3 and 4
        Map<String, Object> regions = compound(
                "a", region(new int[] {0, 0, 0}, new int[] {1, 1, 1}, palette, new int[] {1}, List.of()),
                "b", region(new int[] {4, 2, 0}, new int[] {-2, 1, 1}, palette, new int[] {1, 1}, List.of()));
        Schematic schematic =
                Schematic.read("two.litematic", litematic(7, 3953, regions), MAX, null, Schematic.Upgrader.NONE);
        assertEquals(5, schematic.sizeX());
        assertEquals(3, schematic.sizeY());
        assertEquals(List.of("a", "b"), schematic.regions());
        assertEquals(
                Map.of(
                        List.of(0, 0, 0), new Block("minecraft:stone"),
                        List.of(3, 2, 0), new Block("minecraft:stone"),
                        List.of(4, 2, 0), new Block("minecraft:stone")),
                voxels(schematic));
        // Only region b: its minimum corner becomes 0, 0, 0
        Schematic b = Schematic.read("two.litematic", litematic(7, 3953, regions), MAX, "b", Schematic.Upgrader.NONE);
        assertEquals(
                Map.of(List.of(0, 0, 0), new Block("minecraft:stone"), List.of(1, 0, 0), new Block("minecraft:stone")),
                voxels(b));
        assertThrows(
                IllegalArgumentException.class,
                () -> Schematic.read("two.litematic", litematic(7, 3953, regions), MAX, "c", Schematic.Upgrader.NONE));
    }

    @Test
    void structuresOfCreateAreRead() {
        Map<String, Object> root = compound(
                "DataVersion", 3955,
                "size", List.of(2, 2, 1),
                "palette",
                        List.of(
                                state("minecraft:air"),
                                state("minecraft:oak_sign", "rotation", "4"),
                                state("create:andesite_casing")),
                "blocks",
                        List.of(
                                compound("pos", List.of(0, 0, 0), "state", 2),
                                compound("pos", List.of(1, 0, 0), "state", 0),
                                compound(
                                        "pos",
                                        List.of(0, 1, 0),
                                        "state",
                                        1,
                                        "nbt",
                                        compound(
                                                "id",
                                                "minecraft:sign",
                                                "front_text",
                                                compound("messages", List.of("\"Hi\""))))),
                "entities", List.of(compound("pos", List.of(0.5, 0.5, 0.5))));
        Schematic schematic = Schematic.read("Gate.NBT", Nbt.write(root), MAX, null, Schematic.Upgrader.NONE);
        assertEquals("nbt", schematic.format());
        assertEquals(1, schematic.entities());
        Map<List<Integer>, Block> voxels = voxels(schematic);
        assertEquals(2, voxels.size());
        assertEquals(new Block("create:andesite_casing"), voxels.get(List.of(0, 0, 0)));
        assertEquals(
                "{front_text:{messages:[\"\\\"Hi\\\"\"]}}",
                voxels.get(List.of(0, 1, 0)).data());
    }

    @Test
    void theUpgraderSeesEveryBlockOfTheVersionOfTheFile() {
        List<Object> palette = List.of(state("minecraft:grass"));
        byte[] file = litematic(
                5,
                2586,
                compound("r", region(new int[] {0, 0, 0}, new int[] {1, 1, 1}, palette, new int[] {0}, List.of())));
        List<Integer> versions = new ArrayList<>();
        Schematic schematic = Schematic.read("old.litematic", file, MAX, null, (block, version) -> {
            versions.add(version);
            return block.id().equals("minecraft:grass") ? new Block("minecraft:short_grass") : block;
        });
        assertEquals(List.of(2586), versions);
        assertTrue(voxels(schematic).containsValue(new Block("minecraft:short_grass")));
    }

    @Test
    void unsupportedFilesAreRefused() {
        Map<String, Object> regions = compound(
                "r",
                region(
                        new int[] {0, 0, 0},
                        new int[] {1, 1, 1},
                        List.of(state("minecraft:stone")),
                        new int[] {0},
                        List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> Schematic.read("old.litematic", litematic(4, 1343, regions), MAX, null, Schematic.Upgrader.NONE));
        assertThrows(
                IllegalArgumentException.class,
                () -> Schematic.read("new.litematic", litematic(8, 5000, regions), MAX, null, Schematic.Upgrader.NONE));
        assertThrows(
                IllegalArgumentException.class,
                () -> Schematic.read("house.schem", litematic(7, 3953, regions), MAX, null, Schematic.Upgrader.NONE));
        assertThrows(
                IllegalArgumentException.class,
                () -> Schematic.read("broken.nbt", new byte[] {31, -117, 0}, MAX, null, Schematic.Upgrader.NONE));
        // A block outside of the size
        Map<String, Object> outside = compound(
                "DataVersion",
                3955,
                "size",
                List.of(1, 1, 1),
                "palette",
                List.of(state("minecraft:stone")),
                "blocks",
                List.of(compound("pos", List.of(1, 0, 0), "state", 0)));
        assertThrows(
                IllegalArgumentException.class,
                () -> Schematic.read("out.nbt", Nbt.write(outside), MAX, null, Schematic.Upgrader.NONE));
    }

    @Test
    void blocksKeepTheirSortedStates() {
        Block block = Block.of("minecraft:oak_stairs", new TreeMap<>(Map.of("half", "top", "facing", "east")));
        assertEquals("minecraft:oak_stairs[facing=east,half=top]", block.state());
    }
}
