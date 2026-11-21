package de.joernott.c0depaint.core.transform;

import de.joernott.c0depaint.core.canvas.Block;
import java.util.Map;
import java.util.TreeMap;

/**
 * The states of blocks in transformations (docs/concept.md#block-states-in-transformations), computed from their names
 * and values: {@code facing}, {@code axis}, {@code rotation}, {@code half}, the {@code type} of slabs, the
 * {@code shape} of stairs and rails and the {@code hinge} of doors. Whether the block can take the new states is
 * checked by the mod ({@link de.joernott.c0depaint.core.library.PaintWorld#valid}); a block that can't keeps its
 * states.
 *
 * <p>Rotations follow the right-hand rule about the axis: +90° about y turns east (+x) to north (−z).
 */
public final class BlockStates {
    private static final String[] NAMES = {"east", "west", "up", "down", "south", "north"};
    private static final int[][] VECTORS = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

    private BlockStates() {}

    /** The direction of a name, or null. */
    private static int[] vector(String name) {
        for (int i = 0; i < NAMES.length; i++) {
            if (NAMES[i].equals(name)) {
                return VECTORS[i];
            }
        }
        return null;
    }

    private static String name(int[] vector) {
        for (int i = 0; i < VECTORS.length; i++) {
            if (VECTORS[i][0] == vector[0] && VECTORS[i][1] == vector[1] && VECTORS[i][2] == vector[2]) {
                return NAMES[i];
            }
        }
        throw new IllegalStateException();
    }

    /** A vector turned by quarter turns about an axis (0 = x, 1 = y, 2 = z), by the right-hand rule. */
    static int[] rotate(int[] v, int axis, int quarters) {
        int[] r = v.clone();
        for (int q = 0; q < Math.floorMod(quarters, 4); q++) {
            int x = r[0];
            int y = r[1];
            int z = r[2];
            r = switch (axis) {
                case 0 -> new int[] {x, -z, y};
                case 1 -> new int[] {z, y, -x};
                default -> new int[] {-y, x, z};
            };
        }
        return r;
    }

    /** The states of a block turned by quarter turns about an axis; null if nothing changes. */
    public static Block rotate(Block block, int axis, int quarters) {
        quarters = Math.floorMod(quarters, 4);
        if (quarters == 0 || block.properties().isEmpty()) {
            return null;
        }
        int turns = quarters;
        Map<String, String> states = new TreeMap<>(block.properties());
        states.computeIfPresent("facing", (key, value) -> turned(value, axis, turns));
        states.computeIfPresent("axis", (key, value) -> {
            int[] v = {value.equals("x") ? 1 : 0, value.equals("y") ? 1 : 0, value.equals("z") ? 1 : 0};
            int[] r = rotate(v, axis, turns);
            return r[0] != 0 ? "x" : r[1] != 0 ? "y" : "z";
        });
        if (axis == 1) {
            // rotation counts 16 steps clockwise seen from above, from south; +90° about y is counterclockwise
            states.computeIfPresent(
                    "rotation",
                    (key, value) -> Integer.toString(Math.floorMod(Integer.parseInt(value) - 4 * turns, 16)));
            states.computeIfPresent("shape", (key, value) -> railShape(value, v -> rotate(v, 1, turns)));
            for (String side : NAMES) {
                // Fences, walls, glass panes and redstone wire have a state per side
                if (block.properties().containsKey(side) && vector(side)[1] == 0) {
                    states.put(turned(side, 1, turns), block.properties().get(side));
                }
            }
        } else if (rotate(new int[] {0, 1, 0}, axis, turns)[1] == -1) {
            // Upside down: top and bottom swap
            flipVertical(states);
        }
        return Block.of(block.id(), states);
    }

    /** The states of a block mirrored at a plane perpendicular to an axis; null if nothing changes. */
    public static Block mirror(Block block, int axis) {
        if (block.properties().isEmpty()) {
            return null;
        }
        Map<String, String> states = new TreeMap<>(block.properties());
        states.computeIfPresent("facing", (key, value) -> {
            int[] v = vector(value);
            if (v == null) {
                return value;
            }
            int[] m = v.clone();
            m[axis] = -m[axis];
            return name(m);
        });
        if (axis == 1) {
            flipVertical(states);
        } else {
            // Seen from above, a mirror at x or z swaps left and right
            states.computeIfPresent("rotation", (key, value) -> {
                int r = Integer.parseInt(value);
                return Integer.toString(Math.floorMod(axis == 0 ? -r : 8 - r, 16));
            });
            states.computeIfPresent("hinge", (key, value) -> swap(value, "left", "right"));
            states.computeIfPresent("shape", (key, value) -> {
                if (value.contains("left") || value.contains("right")) {
                    return value.contains("left") ? value.replace("left", "right") : value.replace("right", "left");
                }
                return railShape(value, v -> {
                    int[] m = v.clone();
                    m[axis] = -m[axis];
                    return m;
                });
            });
            for (String side : NAMES) {
                int[] v = vector(side);
                if (block.properties().containsKey(side) && v[axis] != 0) {
                    int[] m = v.clone();
                    m[axis] = -m[axis];
                    states.put(name(m), block.properties().get(side));
                }
            }
        }
        return Block.of(block.id(), states);
    }

    private static String turned(String value, int axis, int quarters) {
        int[] v = vector(value);
        return v == null ? value : name(rotate(v, axis, quarters));
    }

    private static void flipVertical(Map<String, String> states) {
        states.computeIfPresent("half", (key, value) -> swap(value, "top", "bottom"));
        states.computeIfPresent("type", (key, value) -> swap(value, "top", "bottom"));
        states.computeIfPresent("facing", (key, value) -> swap(value, "up", "down"));
    }

    private static String swap(String value, String a, String b) {
        return value.equals(a) ? b : value.equals(b) ? a : value;
    }

    /**
     * The shape of a rail with its directions changed, e.g. {@code north_east} or {@code ascending_west}; other shapes
     * (of stairs) stay as they are.
     */
    private static String railShape(String value, java.util.function.UnaryOperator<int[]> change) {
        String[] parts = value.split("_");
        boolean ascending = parts[0].equals("ascending");
        if (ascending ? parts.length != 2 : parts.length != 2) {
            return value;
        }
        String[] directions = ascending ? new String[] {parts[1]} : parts;
        String[] changed = new String[directions.length];
        for (int i = 0; i < directions.length; i++) {
            int[] v = vector(directions[i]);
            if (v == null || v[1] != 0) {
                return value;
            }
            changed[i] = name(change.apply(v));
        }
        if (ascending) {
            return "ascending_" + changed[0];
        }
        // Minecraft names straight rails north_south and east_west, curves with north or south first
        java.util.List<String> order = java.util.List.of("north", "south", "east", "west");
        if (order.indexOf(changed[0]) > order.indexOf(changed[1])) {
            return changed[1] + "_" + changed[0];
        }
        return changed[0] + "_" + changed[1];
    }
}
