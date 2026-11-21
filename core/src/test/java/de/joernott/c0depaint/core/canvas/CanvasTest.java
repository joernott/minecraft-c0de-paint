package de.joernott.c0depaint.core.canvas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** The canvas in sections with a palette (task 3.2). */
class CanvasTest {
    private static final Block STONE = new Block("minecraft:stone");
    private static final Block STAIRS = Block.of("minecraft:oak_stairs", Map.of("facing", "north", "half", "bottom"));

    @Test
    void voxelsAreNotSetOrHoldABlock() {
        Canvas canvas = new Canvas();
        assertTrue(canvas.isEmpty());
        assertNull(canvas.bounds());
        canvas.set(-3, 0, 17, STONE);
        canvas.set(5, -20, 2, Block.AIR);
        assertEquals(STONE, canvas.get(-3, 0, 17));
        assertEquals(Block.AIR, canvas.get(5, -20, 2));
        assertNull(canvas.get(0, 0, 0));
        assertEquals(2, canvas.count());
        assertEquals(new Bounds(new Point(-3, -20, 2), new Point(5, 0, 17)), canvas.bounds());
        canvas.set(-3, 0, 17, null);
        assertEquals(1, canvas.count());
        assertEquals(new Bounds(new Point(5, -20, 2), new Point(5, -20, 2)), canvas.bounds());
        canvas.set(5, -20, 2, null);
        assertTrue(canvas.isEmpty());
        assertNull(canvas.bounds());
    }

    @Test
    void negativeCoordinatesUseTheirOwnSections() {
        Canvas canvas = new Canvas();
        for (int x = -40; x <= 40; x += 8) {
            canvas.set(x, -x, x * 2, STONE);
        }
        int[] seen = {0};
        canvas.forEach((x, y, z, block) -> {
            assertEquals(-x, y);
            assertEquals(2 * x, z);
            seen[0]++;
        });
        assertEquals(11, seen[0]);
        assertEquals(new Bounds(new Point(-40, -40, -80), new Point(40, 40, 80)), canvas.bounds());
    }

    @Test
    void copiesDontChangeEachOther() {
        Canvas original = new Canvas();
        original.set(1, 1, 1, STONE);
        Canvas copy = original.copy();
        assertEquals(original, copy);
        copy.set(1, 1, 1, STAIRS);
        copy.set(2, 2, 2, STONE);
        assertEquals(STONE, original.get(1, 1, 1));
        assertNull(original.get(2, 2, 2));
        assertEquals(1, original.count());
        original.set(1, 1, 1, null);
        assertEquals(STAIRS, copy.get(1, 1, 1));
        assertNotEquals(original, copy);
    }

    @Test
    void savedCanvasesAreTheSameAfterLoading() {
        Canvas canvas = new Canvas();
        for (int x = 0; x < 40; x++) {
            for (int z = -5; z < 5; z++) {
                canvas.set(x, x % 3, z, (x + z) % 2 == 0 ? STONE : STAIRS);
            }
        }
        canvas.set(-128, 127, -128, Block.AIR);
        String saved = canvas.save();
        Canvas loaded = Canvas.load(saved);
        assertEquals(canvas, loaded);
        assertEquals(canvas.count(), loaded.count());
        assertEquals(canvas.bounds(), loaded.bounds());
        assertEquals(new Canvas(), Canvas.load(new Canvas().save()));
        assertThrows(IllegalArgumentException.class, () -> Canvas.load("not a canvas"));
    }

    @Test
    void aFullCanvasIsSavedCompactly() {
        Canvas canvas = new Canvas();
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                for (int z = 0; z < 64; z++) {
                    canvas.set(x, y, z, STONE);
                }
            }
        }
        assertEquals(262144, canvas.count());
        assertTrue(canvas.save().length() < 4000, "saved in " + canvas.save().length() + " characters");
    }

    @Test
    void theTextShowsSizeAndVoxels() {
        Canvas canvas = new Canvas();
        assertEquals("CANVAS empty", canvas.text());
        canvas.set(0, 0, 0, STONE);
        assertEquals("CANVAS 1×1×1, 1 voxel", canvas.text());
        canvas.set(19, 9, 7, STONE);
        assertEquals("CANVAS 20×10×8, 2 voxels", canvas.text());
    }

    @Test
    void blocksHaveATextWithTheirStates() {
        assertEquals("minecraft:oak_stairs[facing=north,half=bottom]", STAIRS.text());
        assertEquals(STAIRS, Block.parse(STAIRS.text()));
        assertEquals(STONE, Block.parse("minecraft:stone"));
        assertTrue(Block.AIR.isAir());
    }

    @Test
    void limitsRaiseCanvasLimit() {
        Limits limits = new Limits(-128, 127);
        limits.check(new Point(-128, 127, 0));
        assertThrows(de.joernott.c0depaint.core.PaintException.class, () -> limits.check(new Point(128, 0, 0)));
    }
}
