package de.joernott.c0depaint.core.library;

import de.joernott.c0de.core.value.LibraryType;
import de.joernott.c0de.core.value.LibraryValue;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.style.Style;
import java.util.List;
import java.util.Map;

/** The types of c0de_paint/cpaint (docs/concept.md#types): the library types CANVAS and STYLE, the structs POINT and BLOCK. */
public final class PaintTypes {
    /** The name of the library. */
    public static final String LIBRARY = "c0de_paint/cpaint";

    private PaintTypes() {}

    /** A canvas: voxels that are not set or hold a block. A new variable holds an empty canvas. */
    public static final LibraryType CANVAS = new LibraryType() {
        @Override
        public String name() {
            return LIBRARY + "/CANVAS";
        }

        @Override
        public LibraryValue defaultValue() {
            return new CanvasValue();
        }

        @Override
        public String save(LibraryValue value) {
            return ((CanvasValue) value).canvas().save();
        }

        @Override
        public LibraryValue load(String data) {
            return new CanvasValue(Canvas.load(data));
        }

        @Override
        public String toString() {
            return name();
        }
    };

    /** A style. A new variable holds STYLE_NONE. */
    public static final LibraryType STYLE = new LibraryType() {
        @Override
        public String name() {
            return LIBRARY + "/STYLE";
        }

        @Override
        public LibraryValue defaultValue() {
            return StyleValue.NONE;
        }

        @Override
        public String save(LibraryValue value) {
            return ((StyleValue) value).style().save();
        }

        @Override
        public LibraryValue load(String data) {
            return new StyleValue(Style.load(data));
        }

        @Override
        public String toString() {
            return name();
        }
    };

    /** A coordinate in a canvas. */
    public static final StructDefinition POINT = new StructDefinition(
            LIBRARY + "/POINT",
            List.of(
                    element("x", Type.Simple.INTEGER),
                    element("y", Type.Simple.INTEGER),
                    element("z", Type.Simple.INTEGER)));

    /**
     * A block of a style or of a canvas, with block states as in PLACE_BLOCK and the data of its block entity as SNBT
     * (task 9.2; added at the end, so the programs of 0.1.0 keep working).
     */
    public static final StructDefinition BLOCK = new StructDefinition(
            LIBRARY + "/BLOCK",
            List.of(
                    element("id", Type.Str.UNLIMITED),
                    element("properties", new Type.Hash(Type.Str.UNLIMITED)),
                    element("weight", Type.Simple.INTEGER),
                    element("data", Type.Str.UNLIMITED)));

    private static StructDefinition.Element element(String name, Type type) {
        return new StructDefinition.Element(name, type, Map.of());
    }
}
