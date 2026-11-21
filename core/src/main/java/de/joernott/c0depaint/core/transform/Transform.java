package de.joernott.c0depaint.core.transform;

import de.joernott.c0depaint.core.canvas.Block;
import de.joernott.c0depaint.core.canvas.Bounds;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Limits;
import de.joernott.c0depaint.core.canvas.Point;
import de.joernott.c0depaint.core.draw.Vec;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.random.RandomGenerator;

/**
 * A transformation of a canvas (CANVAS_SCALE, CANVAS_ROTATE, CANVAS_MIRROR): every voxel of the box of the result takes
 * the nearest voxel of the source, so there are no holes and no doubled voxels. The result is built in a canvas of its
 * own and replaces the content of the canvas only when the job is done, so an error leaves the canvas unchanged.
 */
public final class Transform implements Job {
    /** Where the voxel of the result comes from, in the coordinates of the source. */
    public interface Mapping {
        Vec source(Vec target);
    }

    private final Canvas canvas;
    private final Canvas source;
    private final Canvas result = new Canvas();
    private final Mapping mapping;
    private final Function<Block, Block> states;
    private final Map<Block, Block> turned = new HashMap<>();
    private final Limits limits;
    private final Bounds box;
    private long next;
    private final long cells;

    /**
     * @param canvas the canvas that gets the result
     * @param box the box of the result, or null if the canvas is empty
     * @param mapping where a voxel of the result comes from
     * @param states the block with its states transformed, or the block itself
     * @param limits the limits of canvases
     */
    public Transform(Canvas canvas, Bounds box, Mapping mapping, Function<Block, Block> states, Limits limits) {
        this.canvas = canvas;
        this.source = canvas.copy();
        this.box = box;
        this.mapping = mapping;
        this.states = states;
        this.limits = limits;
        this.cells = box == null ? 0 : (long) box.size(0) * box.size(1) * box.size(2);
    }

    /** The number of voxels of the box of the result, which the job visits. */
    public long cells() {
        return cells;
    }

    @Override
    public boolean step(int budget, RandomGenerator random) {
        long end = Math.min(cells, next + budget);
        int sizeY = box == null ? 1 : box.size(1);
        int sizeZ = box == null ? 1 : box.size(2);
        for (; next < end; next++) {
            int x = box.min().x() + (int) (next / ((long) sizeY * sizeZ));
            int y = box.min().y() + (int) ((next / sizeZ) % sizeY);
            int z = box.min().z() + (int) (next % sizeZ);
            Point from = mapping.source(new Vec(x, y, z)).round();
            Block block = source.get(from);
            if (block != null) {
                Point to = new Point(x, y, z);
                limits.check(to);
                result.set(to, turned.computeIfAbsent(block, states));
            }
        }
        if (next >= cells) {
            canvas.assign(result);
            return true;
        }
        return false;
    }

    /** The box that contains the images of the corners of the voxels of the bounds under a forward mapping. */
    public static Bounds image(Bounds bounds, Mapping forward) {
        double[] min = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
        double[] max = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        for (int i = 0; i < 8; i++) {
            Vec corner = new Vec(
                    (i & 1) == 0 ? bounds.min().x() - 0.5 : bounds.max().x() + 0.5,
                    (i & 2) == 0 ? bounds.min().y() - 0.5 : bounds.max().y() + 0.5,
                    (i & 4) == 0 ? bounds.min().z() - 0.5 : bounds.max().z() + 0.5);
            Vec image = forward.source(corner);
            for (int axis = 0; axis < 3; axis++) {
                min[axis] = Math.min(min[axis], image.get(axis));
                max[axis] = Math.max(max[axis], image.get(axis));
            }
        }
        return new Bounds(
                new Point(
                        (int) Math.floor(min[0] + 0.5), (int) Math.floor(min[1] + 0.5), (int) Math.floor(min[2] + 0.5)),
                new Point((int) Math.ceil(max[0] - 0.5), (int) Math.ceil(max[1] - 0.5), (int) Math.ceil(max[2] - 0.5)));
    }

    /** CANVAS_SCALE: x, y and z are percentages of 1 or more, around the center. */
    public static Transform scale(Canvas canvas, int x, int y, int z, Point center, Limits limits) {
        if (x < 1 || y < 1 || z < 1) {
            throw new IllegalArgumentException("The percentages are " + x + ", " + y + ", " + z + ", not 1 or more");
        }
        double[] factor = {x / 100.0, y / 100.0, z / 100.0};
        Vec c = Vec.of(center);
        Mapping forward = v -> new Vec(
                c.x() + (v.x() - c.x()) * factor[0],
                c.y() + (v.y() - c.y()) * factor[1],
                c.z() + (v.z() - c.z()) * factor[2]);
        Mapping inverse = v -> new Vec(
                c.x() + (v.x() - c.x()) / factor[0],
                c.y() + (v.y() - c.y()) / factor[1],
                c.z() + (v.z() - c.z()) / factor[2]);
        Bounds bounds = canvas.bounds();
        return new Transform(canvas, bounds == null ? null : image(bounds, forward), inverse, block -> block, limits);
    }

    /**
     * CANVAS_ROTATE: about the axis (0 = x, 1 = y, 2 = z) through the center, by any angle in degrees (right-hand
     * rule). The states of the blocks turn by the nearest multiple of 90°, if the block can take them.
     */
    public static Transform rotate(
            Canvas canvas, Point center, int axis, double degrees, Function<Block, Block> valid, Limits limits) {
        double radians = Math.toRadians(degrees);
        // Exact values for multiples of 90°, so that such rotations map voxels exactly
        double quarter = degrees / 90;
        double cos;
        double sin;
        if (quarter == Math.rint(quarter)) {
            int q = (int) Math.floorMod((long) quarter, 4);
            cos = new double[] {1, 0, -1, 0}[q];
            sin = new double[] {0, 1, 0, -1}[q];
        } else {
            cos = Math.cos(radians);
            sin = Math.sin(radians);
        }
        Vec c = Vec.of(center);
        Mapping forward = v -> turn(v.minus(c), axis, cos, sin).plus(c);
        Mapping inverse = v -> turn(v.minus(c), axis, cos, -sin).plus(c);
        int quarters = (int) Math.floorMod(Math.round(degrees / 90), 4);
        Function<Block, Block> states = block -> {
            Block rotated = BlockStates.rotate(block, axis, quarters);
            return rotated == null ? block : valid.apply(rotated) == null ? block : rotated;
        };
        Bounds bounds = canvas.bounds();
        return new Transform(canvas, bounds == null ? null : image(bounds, forward), inverse, states, limits);
    }

    private static Vec turn(Vec v, int axis, double cos, double sin) {
        return switch (axis) {
            case 0 -> new Vec(v.x(), v.y() * cos - v.z() * sin, v.y() * sin + v.z() * cos);
            case 1 -> new Vec(v.x() * cos + v.z() * sin, v.y(), -v.x() * sin + v.z() * cos);
            default -> new Vec(v.x() * cos - v.y() * sin, v.x() * sin + v.y() * cos, v.z());
        };
    }

    /**
     * CANVAS_MIRROR: at the plane perpendicular to the axis at {@code twice} / 2, e.g. 9 for the plane through 4.5; the
     * states of the blocks are mirrored, if the block can take them.
     */
    public static Transform mirror(Canvas canvas, int axis, long twice, Function<Block, Block> valid, Limits limits) {
        Mapping mapping = v -> {
            double[] p = {v.x(), v.y(), v.z()};
            p[axis] = twice - p[axis];
            return new Vec(p[0], p[1], p[2]);
        };
        Function<Block, Block> states = block -> {
            Block mirrored = BlockStates.mirror(block, axis);
            return mirrored == null ? block : valid.apply(mirrored) == null ? block : mirrored;
        };
        Bounds bounds = canvas.bounds();
        return new Transform(canvas, bounds == null ? null : image(bounds, mapping), mapping, states, limits);
    }
}
