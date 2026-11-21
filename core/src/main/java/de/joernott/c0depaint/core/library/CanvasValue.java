package de.joernott.c0depaint.core.library;

import de.joernott.c0de.core.value.LibraryType;
import de.joernott.c0de.core.value.LibraryValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0depaint.core.canvas.Canvas;

/**
 * A value of the type {@code c0de_paint/cpaint/CANVAS}. The drawing functions change its canvas in place, through a
 * reference parameter; {@link #copy()} gives a copy that shares the sections of the canvas until one of them changes.
 */
public final class CanvasValue implements LibraryValue {
    private final Canvas canvas;

    public CanvasValue(Canvas canvas) {
        this.canvas = canvas;
    }

    /** An empty canvas. */
    public CanvasValue() {
        this(new Canvas());
    }

    /** The canvas, which the functions of the library change. */
    public Canvas canvas() {
        return canvas;
    }

    @Override
    public LibraryType libraryType() {
        return PaintTypes.CANVAS;
    }

    @Override
    public Value copy() {
        return new CanvasValue(canvas.copy());
    }

    @Override
    public String text() {
        return canvas.text();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CanvasValue value && value.canvas.equals(canvas);
    }

    @Override
    public int hashCode() {
        return canvas.hashCode();
    }

    @Override
    public String toString() {
        return text();
    }
}
