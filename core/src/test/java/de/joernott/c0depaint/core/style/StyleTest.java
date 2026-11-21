package de.joernott.c0depaint.core.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0depaint.core.canvas.Block;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

/** The styles (tasks 3.3 to 3.5). */
class StyleTest {
    private static final Block BLACK = new Block("minecraft:black_concrete");
    private static final Block GRAY = new Block("minecraft:gray_concrete");
    private static final Block WHITE = new Block("minecraft:white_concrete");
    private static final List<Weighted> CONCRETE =
            List.of(new Weighted(BLACK, 5), new Weighted(GRAY, 3), new Weighted(WHITE, 2));

    private static RandomGenerator random() {
        return new java.util.Random(42);
    }

    private static List<Point> line(int length) {
        List<Point> line = new ArrayList<>();
        for (int x = 0; x < length; x++) {
            line.add(new Point(x, 0, 0));
        }
        return line;
    }

    private static String drawn(Style style, int length) {
        Canvas canvas = new Canvas();
        style.paint(canvas, line(length), random());
        StringBuilder text = new StringBuilder();
        for (int x = 0; x < length; x++) {
            Block block = canvas.get(x, 0, 0);
            text.append(block == null ? '.' : block.id().charAt(10));
        }
        return text.toString();
    }

    @Test
    void noneEmptyAndSolid() {
        assertEquals("..........", drawn(Style.NONE, 10));
        assertEquals(Block.AIR, Style.EMPTY.block(Point.ORIGIN, random()));
        assertEquals("bbb", drawn(new Style.Solid(BLACK), 3));
    }

    @Test
    void weightsAreOneOrMore() {
        assertThrows(IllegalArgumentException.class, () -> new Weighted(BLACK, 0));
        assertThrows(IllegalArgumentException.class, () -> new Style.Random(List.of()));
    }

    @Test
    void randomBlocksHaveTheChanceOfTheirWeight() {
        Style style = new Style.Random(List.of(new Weighted(BLACK, 3), new Weighted(WHITE, 1)));
        Map<Block, Integer> counts = new java.util.HashMap<>();
        RandomGenerator random = random();
        for (int i = 0; i < 40000; i++) {
            counts.merge(style.block(Point.ORIGIN, random), 1, Integer::sum);
        }
        assertEquals(30000, counts.get(BLACK), 600);
        assertEquals(10000, counts.get(WHITE), 600);
    }

    @Test
    void theSameSeedGivesTheSameDrawing() {
        Style style = new Style.Random(CONCRETE);
        assertEquals(drawn(style, 50), drawn(style, 50));
    }

    @Test
    void gradientsDivideTheLengthByTheWeights() {
        Style style = new Style.Gradient(CONCRETE, Point.ORIGIN, null, "xyz", 0);
        // The examples of the concept: 10 voxels get 5 black, 3 gray and 2 white, 20 voxels 10, 6 and 4
        assertEquals("bbbbbgggww", drawn(style, 10));
        assertEquals("bbbbbbbbbbggggggwwww", drawn(style, 20));
    }

    @Test
    void voxelsBeyondTheEndGetTheLastBlock() {
        Style style = new Style.Gradient(CONCRETE, Point.ORIGIN, new Point(9, 0, 0), "x", 0);
        assertEquals("bbbbbgggwwwwww", drawn(style, 14));
    }

    @Test
    void theAxesMeasureFromAPlaneOrALine() {
        Style layers = new Style.Gradient(CONCRETE, Point.ORIGIN, new Point(0, 9, 0), "y", 0);
        // Along y, the distance ignores x and z
        assertEquals(BLACK, layers.block(new Point(50, 0, -30), random()));
        assertEquals(WHITE, layers.block(new Point(-7, 9, 12), random()));
        assertThrows(IllegalArgumentException.class, () -> new Style.Gradient(CONCRETE, Point.ORIGIN, null, "xw", 0));
        assertThrows(IllegalArgumentException.class, () -> new Style.Gradient(CONCRETE, Point.ORIGIN, null, "xyz", 4));
    }

    @Test
    void fuzzyBordersMixTheNeighbouringBlocks() {
        Style sharp = new Style.Gradient(CONCRETE, Point.ORIGIN, null, "x", 0);
        Style fuzzy = new Style.Gradient(CONCRETE, Point.ORIGIN, null, "x", 1);
        int changed = 0;
        int changedAwayFromBorders = 0;
        RandomGenerator random = random();
        for (int run = 0; run < 200; run++) {
            Canvas a = new Canvas();
            Canvas b = new Canvas();
            sharp.paint(a, line(20), random);
            fuzzy.paint(b, line(20), random);
            for (int x = 0; x < 20; x++) {
                if (!a.get(x, 0, 0).equals(b.get(x, 0, 0))) {
                    changed++;
                    // The borders lie between 9|10 and 15|16
                    if (x != 9 && x != 10 && x != 15 && x != 16) {
                        changedAwayFromBorders++;
                    }
                }
            }
        }
        // Four voxels next to borders, each with a chance of 50 %
        assertEquals(400, changed, 60);
        assertEquals(0, changedAwayFromBorders);
    }

    @Test
    void patternsRepeatAlongAllAxes() {
        Canvas pattern = new Canvas();
        pattern.set(10, 10, 10, BLACK);
        pattern.set(11, 10, 10, WHITE);
        Style style = new Style.Pattern(pattern, Point.ORIGIN);
        assertEquals("bwbwbw", drawn(style, 6));
        Style shifted = new Style.Pattern(pattern, new Point(1, 0, 0));
        assertEquals("wbwbwb", drawn(shifted, 6));
        // Gaps are not drawn
        pattern.set(13, 10, 10, GRAY);
        Style gaps = new Style.Pattern(pattern, Point.ORIGIN);
        assertEquals("bw.gbw.g", drawn(gaps, 8));
        assertNull(gaps.block(new Point(2, 0, 0), random()));
    }

    @Test
    void savedStylesAreTheSameAfterLoading() {
        Canvas pattern = new Canvas();
        pattern.set(0, 0, 0, BLACK);
        List<Style> styles = List.of(
                Style.NONE,
                Style.EMPTY,
                new Style.Solid(Block.of("minecraft:oak_log", Map.of("axis", "x"))),
                new Style.Random(CONCRETE),
                new Style.Gradient(CONCRETE, new Point(1, 2, 3), null, "xz", 2),
                new Style.Gradient(CONCRETE, Point.ORIGIN, new Point(-4, 5, 6), "y", 0),
                new Style.Pattern(pattern, new Point(1, 1, 1)));
        for (Style style : styles) {
            assertEquals(style, Style.load(style.save()), style.text());
        }
        assertTrue(new Style.Random(CONCRETE).text().contains("3 blocks"));
    }
}
