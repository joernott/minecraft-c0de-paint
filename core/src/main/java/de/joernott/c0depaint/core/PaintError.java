package de.joernott.c0depaint.core;

/**
 * The errors of c0de_paint/cpaint (docs/concept.md): the codes of c0de that fit, and codes of its own from 1100 on
 * (c0de net uses 1000 to 1099). The library has the errors of its own as INTEGER constants, e.g. {@code
 * c0de_paint/cpaint/CANVAS_LIMIT}.
 */
public enum PaintError {
    /** An argument is not valid, e.g. a weight below 1. The code of c0de. */
    INVALID_ARGUMENT(309, false),
    /** A drawing would set a voxel outside the limits of the configuration (canvasMin, canvasMax). */
    CANVAS_LIMIT(1100, true),
    /** The canvas has no set voxels, so it has no bounds. */
    CANVAS_EMPTY(1101, true),
    /** The points of a 2D shape lie on one line, so they don't define a plane. */
    NOT_A_PLANE(1102, true);

    private final int code;
    private final boolean own;

    PaintError(int code, boolean own) {
        this.code = code;
        this.own = own;
    }

    /** The error code that programs see. */
    public int code() {
        return code;
    }

    /** True for the errors of the library itself, which it has as constants. */
    public boolean own() {
        return own;
    }
}
