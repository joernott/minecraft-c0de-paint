package de.joernott.c0depaint.core.canvas;

/** The smallest box that contains all set voxels of a canvas, with both corners included. */
public record Bounds(Point min, Point max) {
    /** The number of voxels along an axis (0 = x, 1 = y, 2 = z). */
    public int size(int axis) {
        return max.get(axis) - min.get(axis) + 1;
    }

    /** True if the point lies in the box. */
    public boolean contains(Point point) {
        return point.x() >= min.x()
                && point.x() <= max.x()
                && point.y() >= min.y()
                && point.y() <= max.y()
                && point.z() >= min.z()
                && point.z() <= max.z();
    }

    /** The box between two opposite corners, in any order. */
    public static Bounds of(Point a, Point b) {
        return new Bounds(
                new Point(Math.min(a.x(), b.x()), Math.min(a.y(), b.y()), Math.min(a.z(), b.z())),
                new Point(Math.max(a.x(), b.x()), Math.max(a.y(), b.y()), Math.max(a.z(), b.z())));
    }
}
