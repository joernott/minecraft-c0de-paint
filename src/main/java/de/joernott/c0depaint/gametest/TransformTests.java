package de.joernott.c0depaint.gametest;

import de.joernott.c0depaint.core.canvas.Block;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Limits;
import de.joernott.c0depaint.core.canvas.Point;
import de.joernott.c0depaint.core.transform.Job;
import de.joernott.c0depaint.core.transform.Transform;
import de.joernott.c0depaint.world.MinecraftWorld;
import java.util.Map;
import java.util.Random;
import net.minecraft.gametest.framework.GameTestHelper;

/** The states of blocks in transformations, checked by Minecraft (task 6.4). */
public final class TransformTests {
    private static final Limits LIMITS = new Limits(-128, 127);
    private static final Block STAIRS =
            Block.of("minecraft:oak_stairs", Map.of("facing", "east", "half", "bottom", "shape", "straight", "waterlogged", "false"));
    private static final Block LOG = Block.of("minecraft:oak_log", Map.of("axis", "x"));
    private static final Block DOOR = Block.of(
            "minecraft:oak_door",
            Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "false", "powered", "false"));

    private TransformTests() {}

    private static void run(Job job) {
        while (!job.step(4096, new Random(1))) {
            // The next part
        }
    }

    private static Canvas canvas() {
        Canvas canvas = new Canvas();
        canvas.set(0, 0, 0, STAIRS);
        canvas.set(2, 0, 0, LOG);
        canvas.set(4, 0, 0, DOOR);
        return canvas;
    }

    /** Stairs, logs and doors turn with the canvas; stairs can't face up and stay as they are. */
    public static void statesTurnAndMirror(GameTestHelper helper) {
        MinecraftWorld world = new MinecraftWorld();
        Canvas turned = canvas();
        run(Transform.rotate(turned, Point.ORIGIN, 1, 90, world::valid, LIMITS));
        helper.assertTrue(
                "north".equals(turned.get(0, 0, 0).properties().get("facing")), "stairs: " + turned.get(0, 0, 0));
        helper.assertTrue("z".equals(turned.get(0, 0, -2).properties().get("axis")), "log: " + turned.get(0, 0, -2));
        helper.assertTrue("west".equals(turned.get(0, 0, -4).properties().get("facing")), "door: " + turned.get(0, 0, -4));

        Canvas upright = canvas();
        run(Transform.rotate(upright, Point.ORIGIN, 2, 90, world::valid, LIMITS));
        helper.assertTrue(STAIRS.equals(upright.get(0, 0, 0)), "stairs can't face up: " + upright.get(0, 0, 0));
        helper.assertTrue("y".equals(upright.get(0, 2, 0).properties().get("axis")), "log: " + upright.get(0, 2, 0));

        Canvas mirrored = canvas();
        run(Transform.mirror(mirrored, 0, 4, world::valid, LIMITS));
        helper.assertTrue(
                "west".equals(mirrored.get(4, 0, 0).properties().get("facing")), "stairs: " + mirrored.get(4, 0, 0));
        helper.assertTrue("right".equals(mirrored.get(0, 0, 0).properties().get("hinge")), "door: " + mirrored.get(0, 0, 0));

        helper.assertTrue(world.valid(Block.of("minecraft:oak_stairs", Map.of("facing", "up"))) == null, "facing=up");
        helper.assertTrue(world.valid(Block.of("minecraft:stone", Map.of("color", "red"))) == null, "unknown state");
        helper.succeed();
    }
}
