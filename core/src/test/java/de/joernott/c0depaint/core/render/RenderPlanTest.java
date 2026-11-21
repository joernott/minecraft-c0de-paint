package de.joernott.c0depaint.core.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.joernott.c0depaint.core.canvas.Block;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Point;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The plan of a rendering (task 7.1). */
class RenderPlanTest {
    private static final Block STONE = new Block("minecraft:stone");
    private static final Block TORCH = Block.of("minecraft:wall_torch", Map.of("facing", "north"));

    @Test
    void bottomToTopAndHangingBlocksLast() {
        Canvas canvas = new Canvas();
        canvas.set(0, 1, 0, STONE);
        canvas.set(0, 0, 1, TORCH);
        canvas.set(5, 0, 0, STONE);
        canvas.set(1, 0, 0, Block.AIR);
        RenderPlan plan = RenderPlan.of(canvas, block -> block.id().contains("torch"));
        List<Point> order = plan.steps().stream().map(RenderPlan.Step::voxel).toList();
        assertEquals(List.of(new Point(1, 0, 0), new Point(5, 0, 0), new Point(0, 0, 1), new Point(0, 1, 0)), order);
        assertEquals(Map.of(STONE, 2, TORCH, 1), plan.blocks());
    }
}
