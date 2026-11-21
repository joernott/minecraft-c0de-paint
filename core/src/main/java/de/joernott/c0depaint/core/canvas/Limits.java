package de.joernott.c0depaint.core.canvas;

import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.PaintException;

/**
 * The coordinates a canvas may use on every axis, from the configuration (canvasMin, canvasMax). A drawing that would
 * set a voxel outside raises CANVAS_LIMIT and draws nothing (docs/concept.md#canvas).
 *
 * @param min the smallest coordinate
 * @param max the largest coordinate
 */
public record Limits(int min, int max) {
    /** True if the voxel lies within the limits. */
    public boolean contains(int x, int y, int z) {
        return x >= min && x <= max && y >= min && y <= max && z >= min && z <= max;
    }

    /** Raises CANVAS_LIMIT if the voxel lies outside the limits. */
    public void check(Point point) {
        if (!contains(point.x(), point.y(), point.z())) {
            throw outside(point);
        }
    }

    /** Raises CANVAS_LIMIT if a corner of the box lies outside the limits. */
    public void check(Bounds bounds) {
        if (bounds != null) {
            check(bounds.min());
            check(bounds.max());
        }
    }

    /** The error for a voxel outside the limits. */
    public PaintException outside(Point point) {
        return new PaintException(
                PaintError.CANVAS_LIMIT,
                "The voxel " + point + " lies outside the limits of a canvas (" + min + " to " + max + ")");
    }
}
