package de.joernott.c0depaint.core.canvas;

/** The coordinates of a voxel in a canvas. */
public record Point(int x, int y, int z) {
    public static final Point ORIGIN = new Point(0, 0, 0);

    /** This point moved by another one. */
    public Point plus(Point offset) {
        return new Point(x + offset.x, y + offset.y, z + offset.z);
    }

    /** The coordinate on an axis: 0 is x, 1 is y, 2 is z. */
    public int get(int axis) {
        return switch (axis) {
            case 0 -> x;
            case 1 -> y;
            case 2 -> z;
            default -> throw new IllegalArgumentException("axis " + axis);
        };
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }
}
