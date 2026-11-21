package de.joernott.c0depaint.core.style;

import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Point;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * How a line, a face or the inside of a shape is drawn (docs/concept.md#styles). Styles are immutable.
 */
public sealed interface Style permits Style.None {
    /** Don't draw: the voxels stay as they are. */
    Style NONE = new None();

    /**
     * Draws the voxels of one part of a shape into the canvas.
     *
     * @param canvas the canvas
     * @param voxels the voxels of the part, each once
     * @param random the random number generator of the process
     */
    void paint(Canvas canvas, List<Point> voxels, RandomGenerator random);

    /** A short description for SAY, displays and the debugger, e.g. "STYLE SOLID minecraft:stone". */
    String text();

    /** A compact text of the style, saved with the cursor. */
    String save();

    /** True for STYLE_NONE, which the parts of a shape replace with the style of the next part inward. */
    default boolean isNone() {
        return false;
    }

    /**
     * The style of a text created by {@link #save()}.
     *
     * @throws IllegalArgumentException if the text is not a saved style
     */
    static Style load(String data) {
        if (data.equals("NONE")) {
            return NONE;
        }
        throw new IllegalArgumentException("Not a saved style: " + data);
    }

    /** STYLE_NONE. */
    record None() implements Style {
        @Override
        public void paint(Canvas canvas, List<Point> voxels, RandomGenerator random) {}

        @Override
        public String text() {
            return "STYLE NONE";
        }

        @Override
        public String save() {
            return "NONE";
        }

        @Override
        public boolean isNone() {
            return true;
        }
    }
}
