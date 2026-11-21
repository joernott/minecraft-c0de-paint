package de.joernott.c0depaint.core.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0depaint.core.PaintException;
import de.joernott.c0depaint.core.canvas.Block;
import de.joernott.c0depaint.core.canvas.Bounds;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Limits;
import de.joernott.c0depaint.core.canvas.Point;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/** CANVAS_SCALE, CANVAS_ROTATE, CANVAS_MIRROR and the states of blocks (tasks 6.1 to 6.4). */
class TransformTest {
    private static final Block STONE = new Block("minecraft:stone");
    private static final Limits LIMITS = new Limits(-128, 127);
    private static final Function<Block, Block> ALL_VALID = block -> block;

    private static void run(Job job) {
        while (!job.step(1000, new Random(1))) {
            // The next part
        }
    }

    private static Canvas square(int size) {
        Canvas canvas = new Canvas();
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                canvas.set(x, 0, z, STONE);
            }
        }
        return canvas;
    }

    @Test
    void scalingTakesTheNearestVoxel() {
        Canvas one = new Canvas();
        one.set(0, 0, 0, STONE);
        run(Transform.scale(one, 200, 200, 200, Point.ORIGIN, LIMITS));
        assertEquals(8, one.count());
        Canvas line = new Canvas();
        for (int x = 0; x < 8; x++) {
            line.set(x, 0, 0, STONE);
        }
        run(Transform.scale(line, 50, 100, 100, Point.ORIGIN, LIMITS));
        assertEquals(4, line.count());
        assertThrows(IllegalArgumentException.class, () -> Transform.scale(line, 0, 100, 100, Point.ORIGIN, LIMITS));
        Canvas big = square(100);
        assertThrows(PaintException.class, () -> run(Transform.scale(big, 300, 100, 300, Point.ORIGIN, LIMITS)));
        assertEquals(10000, big.count(), "an error leaves the canvas unchanged");
    }

    @Test
    void quarterTurnsAreExact() {
        Canvas canvas = new Canvas();
        canvas.set(1, 0, 0, STONE);
        canvas.set(2, 3, 0, Block.AIR);
        run(Transform.rotate(canvas, Point.ORIGIN, 1, 90, ALL_VALID, LIMITS));
        assertEquals(STONE, canvas.get(0, 0, -1));
        assertEquals(Block.AIR, canvas.get(0, 3, -2));
        assertEquals(2, canvas.count());
        Canvas square = square(10);
        Canvas turned = square.copy();
        run(Transform.rotate(turned, new Point(4, 0, 4), 1, 360, ALL_VALID, LIMITS));
        assertEquals(square, turned);
    }

    @Test
    void anyAngleLeavesNoHoles() {
        Canvas canvas = square(20);
        run(Transform.rotate(canvas, new Point(10, 0, 10), 1, 30, ALL_VALID, LIMITS));
        // Every voxel well inside the turned square is set
        double cos = Math.cos(Math.toRadians(30));
        double sin = Math.sin(Math.toRadians(30));
        for (int x = -10; x < 30; x++) {
            for (int z = -10; z < 30; z++) {
                double dx = x - 10;
                double dz = z - 10;
                double u = dx * cos - dz * sin + 10;
                double v = dx * sin + dz * cos + 10;
                if (u > 1 && u < 18 && v > 1 && v < 18) {
                    assertEquals(STONE, canvas.get(x, 0, z), "hole at " + x + ", " + z);
                }
            }
        }
        assertEquals(400, canvas.count(), 25);
    }

    @Test
    void mirrorsKeepTheBoundsByDefault() {
        Canvas canvas = new Canvas();
        canvas.set(0, 0, 0, STONE);
        canvas.set(5, 1, 0, Block.AIR);
        Bounds before = canvas.bounds();
        run(Transform.mirror(canvas, 0, 5, ALL_VALID, LIMITS));
        assertEquals(before, canvas.bounds());
        assertEquals(STONE, canvas.get(5, 0, 0));
        assertEquals(Block.AIR, canvas.get(0, 1, 0));
        // At the plane x = 10
        run(Transform.mirror(canvas, 0, 20, ALL_VALID, LIMITS));
        assertEquals(STONE, canvas.get(15, 0, 0));
    }

    @Test
    void statesTurnWithTheBlocks() {
        Block stairs =
                Block.of("minecraft:oak_stairs", Map.of("facing", "east", "half", "bottom", "shape", "inner_left"));
        assertEquals("north", BlockStates.rotate(stairs, 1, 1).properties().get("facing"));
        assertEquals("inner_left", BlockStates.rotate(stairs, 1, 1).properties().get("shape"));
        assertEquals("west", BlockStates.rotate(stairs, 1, 2).properties().get("facing"));
        // Upside down about x
        assertEquals("top", BlockStates.rotate(stairs, 0, 2).properties().get("half"));
        Block log = Block.of("minecraft:oak_log", Map.of("axis", "x"));
        assertEquals("z", BlockStates.rotate(log, 1, 1).properties().get("axis"));
        assertEquals("x", BlockStates.rotate(log, 0, 1).properties().get("axis"));
        assertEquals("y", BlockStates.rotate(log, 2, 1).properties().get("axis"));
        Block sign = Block.of("minecraft:oak_sign", Map.of("rotation", "0"));
        assertEquals("12", BlockStates.rotate(sign, 1, 1).properties().get("rotation"));
        Block rail = Block.of("minecraft:rail", Map.of("shape", "north_south"));
        assertEquals("east_west", BlockStates.rotate(rail, 1, 1).properties().get("shape"));
        Block curve = Block.of("minecraft:rail", Map.of("shape", "south_east"));
        assertEquals("north_east", BlockStates.rotate(curve, 1, 1).properties().get("shape"));
        assertEquals(
                "ascending_north",
                BlockStates.rotate(Block.of("minecraft:rail", Map.of("shape", "ascending_east")), 1, 1)
                        .properties()
                        .get("shape"));
        assertNull(BlockStates.rotate(STONE, 1, 1));
        Block fence = Block.of(
                "minecraft:oak_fence", Map.of("east", "true", "north", "false", "south", "false", "west", "false"));
        assertEquals("true", BlockStates.rotate(fence, 1, 1).properties().get("north"));
        assertEquals("false", BlockStates.rotate(fence, 1, 1).properties().get("east"));
    }

    @Test
    void statesAreMirrored() {
        Block stairs =
                Block.of("minecraft:oak_stairs", Map.of("facing", "east", "half", "bottom", "shape", "inner_left"));
        Block mirrored = BlockStates.mirror(stairs, 0);
        assertEquals("west", mirrored.properties().get("facing"));
        assertEquals("inner_right", mirrored.properties().get("shape"));
        assertEquals("east", BlockStates.mirror(stairs, 2).properties().get("facing"));
        assertEquals("top", BlockStates.mirror(stairs, 1).properties().get("half"));
        Block door = Block.of("minecraft:oak_door", Map.of("facing", "north", "hinge", "left", "half", "lower"));
        assertEquals("right", BlockStates.mirror(door, 0).properties().get("hinge"));
        assertEquals("south", BlockStates.mirror(door, 2).properties().get("facing"));
        assertEquals("lower", BlockStates.mirror(door, 1).properties().get("half"));
        Block slab = Block.of("minecraft:oak_slab", Map.of("type", "bottom"));
        assertEquals("top", BlockStates.mirror(slab, 1).properties().get("type"));
        Block sign = Block.of("minecraft:oak_sign", Map.of("rotation", "4"));
        assertEquals("12", BlockStates.mirror(sign, 0).properties().get("rotation"));
        assertEquals("4", BlockStates.mirror(sign, 2).properties().get("rotation"));
    }

    @Test
    void statesTheBlockCantTakeStayUnchanged() {
        Canvas canvas = new Canvas();
        Block stairs =
                Block.of("minecraft:oak_stairs", Map.of("facing", "east", "half", "bottom", "shape", "straight"));
        canvas.set(0, 0, 0, stairs);
        // Stairs can't face up
        Function<Block, Block> noUp = block -> "up".equals(block.properties().get("facing")) ? null : block;
        run(Transform.rotate(canvas, Point.ORIGIN, 2, 90, noUp, LIMITS));
        assertEquals(stairs, canvas.get(0, 0, 0));
        assertTrue(canvas.count() == 1);
    }
}
