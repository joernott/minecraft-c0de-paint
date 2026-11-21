package de.joernott.c0depaint.core.style;

import de.joernott.c0depaint.core.canvas.Block;
import de.joernott.c0depaint.core.canvas.Bounds;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * How a line, a face or the inside of a shape is drawn (docs/concept.md#styles). Styles are immutable.
 */
public sealed interface Style
        permits Style.None, Style.Empty, Style.Solid, Style.Random, Style.Gradient, Style.Pattern {
    /** Don't draw: the voxels stay as they are. */
    Style NONE = new None();

    /** Air, which erases voxels that were drawn before. */
    Style EMPTY = new Empty();

    /**
     * Draws the voxels of one part of a shape into the canvas.
     *
     * @param canvas the canvas
     * @param voxels the voxels of the part, each once
     * @param random the random number generator of the process
     */
    default void paint(Canvas canvas, List<Point> voxels, RandomGenerator random) {
        for (Point voxel : voxels) {
            Block block = block(voxel, random);
            if (block != null) {
                canvas.set(voxel, block);
            }
        }
    }

    /** The block of a voxel, or null if the voxel stays as it is. */
    Block block(Point voxel, RandomGenerator random);

    /** A short description for SAY, displays and the debugger, e.g. "STYLE SOLID minecraft:stone". */
    String text();

    /** A compact text of the style, saved with the cursor: its kind and its data, in lines. */
    String save();

    /** True for STYLE_NONE, which the parts of a shape replace with the style of the next part inward. */
    default boolean isNone() {
        return false;
    }

    /**
     * The style of a text created by {@link #save()}.
     *
     * @throws IllegalArgumentException if the text is not a saved style
     */
    static Style load(String data) {
        String[] lines = data.split("\n", -1);
        try {
            return switch (lines[0]) {
                case "NONE" -> NONE;
                case "EMPTY" -> EMPTY;
                case "SOLID" -> new Solid(Block.parse(lines[1]));
                case "RANDOM" -> new Random(weighted(lines, 1));
                case "GRADIENT" ->
                    new Gradient(
                            weighted(lines, 5),
                            point(lines[3]),
                            lines[4].equals("-") ? null : point(lines[4]),
                            lines[1],
                            Integer.parseInt(lines[2]));
                case "PATTERN" -> new Pattern(Canvas.load(lines[2]), point(lines[1]));
                default -> throw new IllegalArgumentException("Unknown style " + lines[0]);
            };
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Not a saved style: " + e.getMessage(), e);
        }
    }

    private static List<Weighted> weighted(String[] lines, int first) {
        List<Weighted> blocks = new ArrayList<>();
        for (int i = first; i < lines.length; i++) {
            blocks.add(Weighted.load(lines[i]));
        }
        return blocks;
    }

    private static Point point(String text) {
        String[] parts = text.split(",");
        return new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    private static String save(String kind, List<Weighted> blocks, String... data) {
        StringBuilder text = new StringBuilder(kind);
        for (String line : data) {
            text.append('\n').append(line);
        }
        for (Weighted block : blocks) {
            text.append('\n').append(block.save());
        }
        return text.toString();
    }

    private static String count(List<Weighted> blocks) {
        return blocks.size() + (blocks.size() == 1 ? " block" : " blocks");
    }

    /** STYLE_NONE. */
    record None() implements Style {
        @Override
        public void paint(Canvas canvas, List<Point> voxels, RandomGenerator random) {}

        @Override
        public Block block(Point voxel, RandomGenerator random) {
            return null;
        }

        @Override
        public String text() {
            return "STYLE NONE";
        }

        @Override
        public String save() {
            return "NONE";
        }

        @Override
        public boolean isNone() {
            return true;
        }
    }

    /** STYLE_EMPTY: air. */
    record Empty() implements Style {
        @Override
        public Block block(Point voxel, RandomGenerator random) {
            return Block.AIR;
        }

        @Override
        public String text() {
            return "STYLE EMPTY";
        }

        @Override
        public String save() {
            return "EMPTY";
        }
    }

    /** STYLE_SOLID: always the same block. */
    record Solid(Block block) implements Style {
        @Override
        public Block block(Point voxel, RandomGenerator random) {
            return block;
        }

        @Override
        public String text() {
            return "STYLE SOLID " + block.text();
        }

        @Override
        public String save() {
            return "SOLID\n" + block.text();
        }
    }

    /** STYLE_RANDOM: for every voxel a block chosen at random, with the chance of its weight. */
    record Random(List<Weighted> blocks) implements Style {
        public Random {
            if (blocks.isEmpty()) {
                throw new IllegalArgumentException("STYLE_RANDOM needs at least one block");
            }
            blocks = List.copyOf(blocks);
            Weighted.total(blocks);
        }

        @Override
        public Block block(Point voxel, RandomGenerator random) {
            int chosen = random.nextInt(Weighted.total(blocks));
            for (Weighted block : blocks) {
                chosen -= block.weight();
                if (chosen < 0) {
                    return block.block();
                }
            }
            return blocks.getLast().block();
        }

        @Override
        public String text() {
            return "STYLE RANDOM " + count(blocks);
        }

        @Override
        public String save() {
            return Style.save("RANDOM", blocks);
        }
    }

    /**
     * STYLE_GRADIENT: a gradient through the blocks in their order (docs/concept.md#gradient). A voxel at the distance
     * d from {@code from}, measured along {@code axes}, gets the block whose share contains (d + ½) · W / (L + 1); W is
     * the sum of the weights, L the length of the gradient.
     *
     * @param blocks the blocks in their order
     * @param from where the gradient starts
     * @param to where it ends, or null for the voxel of the part that is farthest from {@code from}
     * @param axes the axes along which the distance is measured, e.g. "xyz" or "y"
     * @param fuzzy how blurred the borders are, 0 to 3
     */
    record Gradient(List<Weighted> blocks, Point from, Point to, String axes, int fuzzy) implements Style {
        public Gradient {
            if (blocks.isEmpty()) {
                throw new IllegalArgumentException("STYLE_GRADIENT needs at least one block");
            }
            blocks = List.copyOf(blocks);
            Weighted.total(blocks);
            if (axes.isEmpty()
                    || !axes.chars().allMatch(c -> c == 'x' || c == 'y' || c == 'z')
                    || axes.chars().distinct().count() != axes.length()) {
                throw new IllegalArgumentException("The axes are '" + axes + "', not a combination of x, y and z");
            }
            if (fuzzy < 0 || fuzzy > 3) {
                throw new IllegalArgumentException("fuzzy is " + fuzzy + ", not 0 to 3");
            }
        }

        /** The distance of a point from {@code from}, along the axes. */
        public double distance(Point point) {
            double sum = 0;
            for (int i = 0; i < axes.length(); i++) {
                int axis = axes.charAt(i) - 'x';
                double delta = point.get(axis) - from.get(axis);
                sum += delta * delta;
            }
            return Math.sqrt(sum);
        }

        /** The index of the block at the distance d of a gradient with the length. */
        int index(double d, double length) {
            int total = Weighted.total(blocks);
            double share = (Math.max(d, 0) + 0.5) * total / (length + 1);
            int sum = 0;
            for (int i = 0; i < blocks.size(); i++) {
                sum += blocks.get(i).weight();
                if (share < sum) {
                    return i;
                }
            }
            return blocks.size() - 1;
        }

        @Override
        public void paint(Canvas canvas, List<Point> voxels, RandomGenerator random) {
            double length = 0;
            if (to != null) {
                length = distance(to);
            } else {
                for (Point voxel : voxels) {
                    length = Math.max(length, distance(voxel));
                }
            }
            for (Point voxel : voxels) {
                canvas.set(voxel, block(voxel, length, random));
            }
        }

        @Override
        public Block block(Point voxel, RandomGenerator random) {
            return block(voxel, to == null ? distance(voxel) : distance(to), random);
        }

        private Block block(Point voxel, double length, RandomGenerator random) {
            double d = distance(voxel);
            int index = index(d, length);
            for (int line = 1; line <= fuzzy; line++) {
                for (int side : new int[] {1, -1}) {
                    int other = index(d + side * line, length);
                    if (other != index) {
                        // The voxel is in the line `line` next to a border: it takes the block of the other side with
                        // a chance of 50 %, 25 % or 12.5 %
                        if (random.nextDouble() < Math.pow(0.5, line)) {
                            return blocks.get(other).block();
                        }
                        return blocks.get(index).block();
                    }
                }
            }
            return blocks.get(index).block();
        }

        @Override
        public String text() {
            return "STYLE GRADIENT " + count(blocks);
        }

        @Override
        public String save() {
            return Style.save(
                    "GRADIENT",
                    blocks,
                    axes,
                    Integer.toString(fuzzy),
                    from.toString(),
                    to == null ? "-" : to.toString());
        }
    }

    /**
     * STYLE_PATTERN: the canvas {@code pattern} repeated along all three axes, with the minimum corner of its bounds at
     * {@code from} (docs/concept.md#pattern). Voxels that are not set in the pattern are not drawn.
     */
    record Pattern(Canvas pattern, Point from) implements Style {
        public Pattern {
            if (pattern.isEmpty()) {
                throw new IllegalArgumentException("The pattern is empty");
            }
            // The style keeps its own copy, so changes of the canvas of the program don't change it
            pattern = pattern.copy();
        }

        @Override
        public Block block(Point voxel, RandomGenerator random) {
            Bounds bounds = pattern.bounds();
            return pattern.get(
                    bounds.min().x() + Math.floorMod(voxel.x() - from.x(), bounds.size(0)),
                    bounds.min().y() + Math.floorMod(voxel.y() - from.y(), bounds.size(1)),
                    bounds.min().z() + Math.floorMod(voxel.z() - from.z(), bounds.size(2)));
        }

        @Override
        public String text() {
            Bounds bounds = pattern.bounds();
            return "STYLE PATTERN " + bounds.size(0) + "×" + bounds.size(1) + "×" + bounds.size(2);
        }

        @Override
        public String save() {
            return "PATTERN\n" + from + "\n" + pattern.save();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Pattern p && p.pattern.equals(pattern) && p.from.equals(from);
        }

        @Override
        public int hashCode() {
            return pattern.hashCode() * 31 + from.hashCode();
        }
    }
}
