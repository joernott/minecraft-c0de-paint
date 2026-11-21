package de.joernott.c0depaint.core.canvas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * A three-dimensional array of voxels without a fixed size (docs/concept.md#canvas). A voxel is either not set or
 * holds a {@link Block}; air is a block.
 *
 * <p>The voxels are stored in sections of 16³, each with a palette of its blocks; only sections with content take
 * memory. {@link #copy()} shares the sections with the copy, and a section is copied when one of the canvases changes
 * it (copy on write), so copies are cheap.
 */
public final class Canvas {
    private static final int SIZE = 16;
    private static final int VOXELS = SIZE * SIZE * SIZE;
    private static final int FORMAT = 1;

    private final HashMap<Long, Section> sections;
    private int count;
    private Bounds bounds;
    private boolean boundsKnown = true;

    public Canvas() {
        this(new HashMap<>(), 0);
    }

    private Canvas(HashMap<Long, Section> sections, int count) {
        this.sections = sections;
        this.count = count;
        this.boundsKnown = count == 0;
    }

    /** A copy that shares the sections until one of the canvases changes them. */
    public Canvas copy() {
        HashMap<Long, Section> shared = new HashMap<>(sections);
        shared.values().forEach(section -> section.shared = true);
        Canvas copy = new Canvas(shared, count);
        copy.bounds = bounds;
        copy.boundsKnown = boundsKnown;
        return copy;
    }

    /** The number of set voxels. */
    public int count() {
        return count;
    }

    /** True if no voxel is set. */
    public boolean isEmpty() {
        return count == 0;
    }

    /** The block of a voxel, or null if it is not set. */
    public Block get(int x, int y, int z) {
        Section section = sections.get(key(x >> 4, y >> 4, z >> 4));
        return section == null ? null : section.get(index(x, y, z));
    }

    /** The block of a voxel, or null if it is not set. */
    public Block get(Point point) {
        return get(point.x(), point.y(), point.z());
    }

    /** Sets a voxel to a block, or unsets it if the block is null. */
    public void set(int x, int y, int z, Block block) {
        long key = key(x >> 4, y >> 4, z >> 4);
        Section section = sections.get(key);
        if (section == null) {
            if (block == null) {
                return;
            }
            section = new Section();
            sections.put(key, section);
        } else if (section.shared) {
            section = section.copy();
            sections.put(key, section);
        }
        int before = section.count;
        section.set(index(x, y, z), block);
        count += section.count - before;
        if (section.count == 0) {
            sections.remove(key);
        }
        if (block != null && boundsKnown && bounds != null) {
            Point point = new Point(x, y, z);
            if (!bounds.contains(point)) {
                bounds = Bounds.of(
                        new Point(
                                Math.min(x, bounds.min().x()),
                                Math.min(y, bounds.min().y()),
                                Math.min(z, bounds.min().z())),
                        new Point(
                                Math.max(x, bounds.max().x()),
                                Math.max(y, bounds.max().y()),
                                Math.max(z, bounds.max().z())));
            }
        } else if (block != null && boundsKnown && count == 1) {
            bounds = new Bounds(new Point(x, y, z), new Point(x, y, z));
        } else if (block == null) {
            boundsKnown = count == 0;
            bounds = null;
        }
    }

    /** Sets a voxel to a block, or unsets it if the block is null. */
    public void set(Point point, Block block) {
        set(point.x(), point.y(), point.z(), block);
    }

    /** Replaces the content with the content of another canvas, sharing its sections until one of them changes. */
    public void assign(Canvas other) {
        sections.clear();
        other.sections.values().forEach(section -> section.shared = true);
        sections.putAll(other.sections);
        count = other.count;
        bounds = other.bounds;
        boundsKnown = other.boundsKnown;
    }

    /** Unsets all voxels. */
    public void clear() {
        sections.clear();
        count = 0;
        bounds = null;
        boundsKnown = true;
    }

    /** The smallest box that contains all set voxels, or null if the canvas is empty. */
    public Bounds bounds() {
        if (!boundsKnown) {
            int[] min = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
            int[] max = {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
            forEach((x, y, z, block) -> {
                min[0] = Math.min(min[0], x);
                min[1] = Math.min(min[1], y);
                min[2] = Math.min(min[2], z);
                max[0] = Math.max(max[0], x);
                max[1] = Math.max(max[1], y);
                max[2] = Math.max(max[2], z);
            });
            bounds = count == 0
                    ? null
                    : new Bounds(new Point(min[0], min[1], min[2]), new Point(max[0], max[1], max[2]));
            boundsKnown = true;
        }
        return bounds;
    }

    /** A set voxel. */
    @FunctionalInterface
    public interface Voxel {
        void accept(int x, int y, int z, Block block);
    }

    /** Calls the action for every set voxel, section by section. */
    public void forEach(Voxel action) {
        for (Map.Entry<Long, Section> entry : sections.entrySet()) {
            long key = entry.getKey();
            int baseX = sectionX(key) * SIZE;
            int baseY = sectionY(key) * SIZE;
            int baseZ = sectionZ(key) * SIZE;
            Section section = entry.getValue();
            for (int i = 0; i < VOXELS; i++) {
                Block block = section.get(i);
                if (block != null) {
                    action.accept(baseX + (i >> 8), baseY + ((i >> 4) & 15), baseZ + (i & 15), block);
                }
            }
        }
    }

    /** The blocks of the canvas with the number of voxels that hold them. */
    public Map<Block, Integer> blocks() {
        Map<Block, Integer> blocks = new HashMap<>();
        forEach((x, y, z, block) -> blocks.merge(block, 1, Integer::sum));
        return blocks;
    }

    /** A short description for SAY, displays and the debugger, e.g. "CANVAS 20×10×8, 1600 voxels". */
    public String text() {
        Bounds box = bounds();
        if (box == null) {
            return "CANVAS empty";
        }
        return "CANVAS " + box.size(0) + "×" + box.size(1) + "×" + box.size(2) + ", " + count
                + (count == 1 ? " voxel" : " voxels");
    }

    /** Two canvases are equal if they set the same voxels to the same blocks. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Canvas canvas) || canvas.count != count) {
            return false;
        }
        boolean[] equal = {true};
        forEach((x, y, z, block) -> {
            if (equal[0] && !block.equals(canvas.get(x, y, z))) {
                equal[0] = false;
            }
        });
        return equal[0];
    }

    @Override
    public int hashCode() {
        int[] hash = {count};
        forEach((x, y, z, block) -> hash[0] += (31 * (31 * x + y) + z) ^ block.hashCode());
        return hash[0];
    }

    @Override
    public String toString() {
        return text();
    }

    /**
     * A compact text of the canvas, saved with the cursor: Base64 of the compressed sections, each with its palette
     * and its voxels as runs of the same palette entry.
     */
    public String save() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(new DeflaterOutputStream(bytes))) {
            out.writeByte(FORMAT);
            out.writeInt(sections.size());
            for (Map.Entry<Long, Section> entry : new TreeMap<>(sections).entrySet()) {
                out.writeLong(entry.getKey());
                Section section = entry.getValue();
                out.writeShort(section.palette.size());
                for (Block block : section.palette) {
                    out.writeUTF(block.text());
                }
                int i = 0;
                while (i < VOXELS) {
                    short value = section.data[i];
                    int run = 1;
                    while (i + run < VOXELS && section.data[i + run] == value) {
                        run++;
                    }
                    out.writeShort(run - 1);
                    out.writeShort(value);
                    i += run;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }

    /**
     * The canvas of a text created by {@link #save()}.
     *
     * @throws IllegalArgumentException if the text is not a saved canvas
     */
    public static Canvas load(String data) {
        Canvas canvas = new Canvas();
        try (DataInputStream in = new DataInputStream(new InflaterInputStream(
                new ByteArrayInputStream(Base64.getDecoder().decode(data))))) {
            if (in.readByte() != FORMAT) {
                throw new IllegalArgumentException("Unknown format of a canvas");
            }
            int sectionCount = in.readInt();
            for (int s = 0; s < sectionCount; s++) {
                long key = in.readLong();
                Section section = new Section();
                int paletteSize = in.readShort();
                for (int p = 0; p < paletteSize; p++) {
                    Block block = Block.parse(in.readUTF());
                    section.index.put(block, (short) (section.palette.size() + 1));
                    section.palette.add(block);
                }
                int i = 0;
                while (i < VOXELS) {
                    int run = in.readShort() + 1;
                    short value = in.readShort();
                    for (int r = 0; r < run; r++) {
                        section.data[i++] = value;
                    }
                    if (value != 0) {
                        section.count += run;
                    }
                }
                if (section.count > 0) {
                    canvas.sections.put(key, section);
                    canvas.count += section.count;
                }
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalArgumentException("Not a saved canvas: " + e.getMessage(), e);
        }
        canvas.boundsKnown = canvas.count == 0;
        return canvas;
    }

    private static int index(int x, int y, int z) {
        return ((x & 15) << 8) | ((y & 15) << 4) | (z & 15);
    }

    private static long key(int sx, int sy, int sz) {
        return ((long) (sx & 0x1FFFFF) << 42) | ((long) (sy & 0x1FFFFF) << 21) | (sz & 0x1FFFFF);
    }

    private static int sectionX(long key) {
        return (int) (key << 1 >> 43);
    }

    private static int sectionY(long key) {
        return (int) (key << 22 >> 43);
    }

    private static int sectionZ(long key) {
        return (int) (key << 43 >> 43);
    }

    /** 16³ voxels: 0 is not set, n is the block n of the palette. */
    private static final class Section {
        final List<Block> palette = new ArrayList<>();
        final Map<Block, Short> index = new HashMap<>();
        final short[] data = new short[VOXELS];
        int count;
        boolean shared;

        Block get(int i) {
            short value = data[i];
            return value == 0 ? null : palette.get(value - 1);
        }

        void set(int i, Block block) {
            short value = 0;
            if (block != null) {
                Short known = index.get(block);
                if (known == null) {
                    palette.add(block);
                    known = (short) palette.size();
                    index.put(block, known);
                }
                value = known;
            }
            if (data[i] == 0 && value != 0) {
                count++;
            } else if (data[i] != 0 && value == 0) {
                count--;
            }
            data[i] = value;
        }

        Section copy() {
            Section copy = new Section();
            copy.palette.addAll(palette);
            copy.index.putAll(index);
            System.arraycopy(data, 0, copy.data, 0, VOXELS);
            copy.count = count;
            return copy;
        }
    }
}
