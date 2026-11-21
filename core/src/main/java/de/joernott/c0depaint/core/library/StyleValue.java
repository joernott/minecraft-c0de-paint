package de.joernott.c0depaint.core.library;

import de.joernott.c0de.core.value.LibraryType;
import de.joernott.c0de.core.value.LibraryValue;
import de.joernott.c0depaint.core.style.Style;

/** A value of the type {@code c0de_paint/cpaint/STYLE}. Styles are immutable, so copies are the value itself. */
public record StyleValue(Style style) implements LibraryValue {
    /** STYLE_NONE, the value of a new variable. */
    public static final StyleValue NONE = new StyleValue(Style.NONE);

    @Override
    public LibraryType libraryType() {
        return PaintTypes.STYLE;
    }

    @Override
    public StyleValue copy() {
        return this;
    }

    @Override
    public String text() {
        return style.text();
    }

    @Override
    public String toString() {
        return text();
    }
}
