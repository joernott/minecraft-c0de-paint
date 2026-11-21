package de.joernott.c0depaint.core.draw;

import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Limits;
import de.joernott.c0depaint.core.canvas.Point;
import de.joernott.c0depaint.core.style.Style;
import de.joernott.c0depaint.core.transform.Job;
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

    /**
     * The drawing of the parts as a job that draws at most a number of voxels per tick. The limits are checked before
     * the first voxel; the styles are prepared with all voxels of their part, e.g. the length of a gradient.
     */
    public static Job job(Canvas canvas, Limits limits, List<Part> parts) {
        List<List<Point>> voxels = new ArrayList<>();
        List<Style.Prepared> styles = new ArrayList<>();
        for (Part part : parts) {
            List<Point> unique = new ArrayList<>(new LinkedHashSet<>(part.voxels()));
            if (!part.style().isNone()) {
                unique.forEach(limits::check);
            }
            voxels.add(unique);
            styles.add(part.style().isNone() ? null : part.style().prepare(unique));
        }
        return new Job() {
            private int part;
            private int next;

            @Override
            public boolean step(int budget, RandomGenerator random) {
                int done = 0;
                while (part < voxels.size() && done < budget) {
                    Style.Prepared style = styles.get(part);
                    List<Point> list = voxels.get(part);
                    if (style == null) {
                        part++;
                        next = 0;
                        continue;
                    }
                    for (; next < list.size() && done < budget; next++, done++) {
                        Point voxel = list.get(next);
                        de.joernott.c0depaint.core.canvas.Block block = style.block(voxel, random);
                        if (block != null) {
                            canvas.set(voxel, block);
                        }
                    }
                    if (next >= list.size()) {
                        part++;
                        next = 0;
                    }
                }
                return part >= voxels.size();
            }
        };
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
