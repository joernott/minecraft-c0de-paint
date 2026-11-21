package de.joernott.c0depaint.core.draw;

import de.joernott.c0depaint.core.canvas.Point;

/** A point or direction in space, with real coordinates. */
public record Vec(double x, double y, double z) {
    public static final Vec ZERO = new Vec(0, 0, 0);

    public static Vec of(Point point) {
        return new Vec(point.x(), point.y(), point.z());
    }

    public Vec plus(Vec other) {
        return new Vec(x + other.x, y + other.y, z + other.z);
    }

    public Vec minus(Vec other) {
        return new Vec(x - other.x, y - other.y, z - other.z);
    }

    public Vec times(double factor) {
        return new Vec(x * factor, y * factor, z * factor);
    }

    public double dot(Vec other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public Vec cross(Vec other) {
        return new Vec(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x);
    }

    public double length() {
        return Math.sqrt(dot(this));
    }

    public Vec unit() {
        double length = length();
        return new Vec(x / length, y / length, z / length);
    }

    /** The coordinate on an axis: 0 is x, 1 is y, 2 is z. */
    public double get(int axis) {
        return switch (axis) {
            case 0 -> x;
            case 1 -> y;
            default -> z;
        };
    }

    /** The voxel whose center is nearest. */
    public Point round() {
        return new Point((int) Math.floor(x + 0.5), (int) Math.floor(y + 0.5), (int) Math.floor(z + 0.5));
    }
}
