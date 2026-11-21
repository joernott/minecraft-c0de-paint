package de.joernott.c0depaint.core.draw;

import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Limits;
import de.joernott.c0depaint.core.canvas.Point;
import de.joernott.c0depaint.core.style.Style;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Draws the parts of a shape into a canvas (docs/concept.md#rules-for-all-shapes): first the inside, then the faces,
 * then the lines, so that the outer parts win where they meet. Before the first voxel, every voxel that will be drawn
 * is checked against the limits; a voxel outside raises CANVAS_LIMIT and nothing is drawn.
 */
public final class Painter {
    private Painter() {}

    /**
     * A part of a shape with its style.
     *
     * @param voxels the voxels of the part
     * @param style the style, after the fallback of STYLE_NONE
     */
    public record Part(Collection<Point> voxels, Style style) {}

    /**
     * The styles of the parts after the fallback of STYLE_NONE: a part whose style is STYLE_NONE takes the style of
     * the next part inward (lines take the side style, or the fill style of 2D shapes; faces take the fill style).
     *
     * @param line the style of the lines, or null if the shape has none
     * @param side the style of the faces, or null if the shape has none (2D shapes)
     * @param fill the style of the inside
     * @return the styles of line, side and fill
     */
    public static Style[] resolve(Style line, Style side, Style fill) {
        Style resolvedSide = side == null ? null : side.isNone() ? fill : side;
        Style inward = resolvedSide == null ? fill : resolvedSide;
        Style resolvedLine = line == null ? null : line.isNone() ? inward : line;
        return new Style[] {resolvedLine, resolvedSide, fill};
    }

    /** Draws the parts, from the inside out. */
    public static void draw(Canvas canvas, Limits limits, RandomGenerator random, List<Part> parts) {
        List<List<Point>> voxels = new ArrayList<>();
        for (Part part : parts) {
            List<Point> unique = new ArrayList<>(new LinkedHashSet<>(part.voxels()));
            if (!part.style().isNone()) {
                unique.forEach(limits::check);
            }
            voxels.add(unique);
        }
        for (int i = 0; i < parts.size(); i++) {
            parts.get(i).style().paint(canvas, voxels.get(i), random);
        }
    }
}
