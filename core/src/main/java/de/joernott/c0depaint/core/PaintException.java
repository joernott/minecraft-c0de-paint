package de.joernott.c0depaint.core;

/** An error of a function of c0de_paint/cpaint, which the library raises at the CALL statement. */
public final class PaintException extends RuntimeException {
    private final PaintError error;

    public PaintException(PaintError error, String message) {
        super(message);
        this.error = error;
    }

    /** The error. */
    public PaintError error() {
        return error;
    }
}
