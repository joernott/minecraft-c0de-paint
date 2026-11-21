package de.joernott.c0depaint.core.render;

import de.joernott.c0depaint.core.canvas.Block;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * The plan of a rendering (docs/concept.md#order-and-speed): the voxels of a canvas from bottom to top, layer by layer;
 * in every layer the blocks that hang on others (torches, ladders, buttons, signs on walls) after the other blocks.
 */
public final class RenderPlan {
    /**
     * A voxel to render.
     *
     * @param voxel the voxel of the canvas
     * @param block its block; air removes the block of the world
     */
    public record Step(Point voxel, Block block) {}

    private final List<Step> steps;

    private RenderPlan(List<Step> steps) {
        this.steps = List.copyOf(steps);
    }

    /**
     * The plan of a canvas.
     *
     * @param canvas the canvas
     * @param hangs true for blocks that hang on others
     */
    public static RenderPlan of(Canvas canvas, Predicate<Block> hangs) {
        List<Step> steps = new ArrayList<>(canvas.count());
        canvas.forEach((x, y, z, block) -> steps.add(new Step(new Point(x, y, z), block)));
        Map<Block, Boolean> hanging = new LinkedHashMap<>();
        steps.sort(Comparator.<Step>comparingInt(step -> step.voxel().y())
                .thenComparing(step -> hanging.computeIfAbsent(step.block(), hangs::test))
                .thenComparingInt(step -> step.voxel().x())
                .thenComparingInt(step -> step.voxel().z()));
        return new RenderPlan(steps);
    }

    /** The voxels in their order. */
    public List<Step> steps() {
        return steps;
    }

    /** The blocks to place with their numbers, without air, in the order of their first use. */
    public Map<Block, Integer> blocks() {
        Map<Block, Integer> blocks = new LinkedHashMap<>();
        for (Step step : steps) {
            if (!step.block().isAir()) {
                blocks.merge(step.block(), 1, Integer::sum);
            }
        }
        return blocks;
    }
}
