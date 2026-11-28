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
    NOT_A_PLANE(1102, true),
    /** There is no schematic with the name in the folder schematics (c0de_paint/schematics, 0.2.0). */
    SCHEMATIC_NOT_FOUND(1103, true, "schematics"),
    /** The file is not a schematic of a supported format and version, or has no such region. */
    SCHEMATIC_INVALID(1104, true, "schematics"),
    /** The file is larger than maxSchematicSize, or its schematic larger than a canvas may be. */
    SCHEMATIC_TOO_LARGE(1105, true, "schematics"),
    /** The player who runs the process is not online or has no c0de paint on the client. */
    CLIENT_UNAVAILABLE(1106, true, "schematics"),
    /** The client of the player doesn't read schematics (allowSchematics is false). */
    SCHEMATICS_DISABLED(1107, true, "schematics");

    private final int code;
    private final boolean own;
    private final String library;

    PaintError(int code, boolean own) {
        this(code, own, "cpaint");
    }

    PaintError(int code, boolean own, String library) {
        this.code = code;
        this.own = own;
        this.library = library;
    }

    /** The library whose constant the error is, e.g. "cpaint" or "schematics". */
    public String library() {
        return library;
    }

    /** The error with a code, or null. */
    public static PaintError of(int code) {
        for (PaintError error : values()) {
            if (error.code == code) {
                return error;
            }
        }
        return null;
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
