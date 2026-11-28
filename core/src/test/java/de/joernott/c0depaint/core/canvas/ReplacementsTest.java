package de.joernott.c0depaint.core.canvas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;

/** The rule for replacing blocks (task 9.1.2, docs/block_review.md, point 4). */
class ReplacementsTest {
    /** A world in which logs have only the state axis and stairs facing, half and shape. */
    private static final UnaryOperator<Block> WORLD = block -> {
        Set<String> states = block.id().endsWith("_log")
                ? Set.of("axis")
                : block.id().endsWith("_stairs") ? Set.of("facing", "half", "shape", "waterlogged") : Set.of();
        return states.containsAll(block.properties().keySet()) ? block : null;
    };

    private static final Block STAIRS = Block.of(
            "minecraft:oak_stairs", Map.of("facing", "east", "half", "top", "shape", "straight"));

    @Test
    void theNewBlockKeepsTheStatesItHas() {
        Replacements replace = Replacements.of(Map.of("oak_stairs", "spruce_stairs"));
        assertEquals(
                Optional.of(Block.of("minecraft:spruce_stairs", STAIRS.properties())), replace.apply(STAIRS, WORLD));
        // A log has no facing: only the states of the replacement remain
        Replacements toLogs = Replacements.of(Map.of("oak_stairs", "oak_log[axis=x]"));
        assertEquals(Optional.of(Block.of("minecraft:oak_log", Map.of("axis", "x"))), toLogs.apply(STAIRS, WORLD));
        // Other blocks stay as they are
        Block stone = new Block("minecraft:stone");
        assertEquals(Optional.of(stone), replace.apply(stone, WORLD));
    }

    @Test
    void statesOfTheReplacementWin() {
        Replacements replace = Replacements.of(Map.of("oak_stairs", "spruce_stairs[half=bottom]"));
        assertEquals("bottom", replace.apply(STAIRS, WORLD).orElseThrow().properties().get("half"));
        assertEquals("east", replace.apply(STAIRS, WORLD).orElseThrow().properties().get("facing"));
    }

    @Test
    void theKeyWithTheMostMatchingStatesWins() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("oak_stairs", "spruce_stairs");
        map.put("oak_stairs[half=top]", "birch_stairs");
        Replacements replace = Replacements.of(map);
        assertEquals("minecraft:birch_stairs", replace.apply(STAIRS, WORLD).orElseThrow().id());
        assertEquals(
                "minecraft:spruce_stairs", replace.apply(STAIRS.with("half", "bottom"), WORLD).orElseThrow().id());
    }

    @Test
    void anEmptyReplacementLeavesTheVoxelNotSet() {
        Replacements replace = Replacements.of(Map.of("create:shaft", ""));
        assertTrue(replace.apply(new Block("create:shaft"), WORLD).isEmpty());
    }

    @Test
    void dataIsKeptUnlessTheReplacementHasItsOwn() {
        Block chest = new Block("minecraft:chest").withData("{Items:[]}");
        assertEquals("{Items:[]}", Replacements.of(Map.of("chest", "barrel")).apply(chest, WORLD).orElseThrow().data());
        assertEquals(
                "{CustomName:'\"Box\"'}",
                Replacements.of(Map.of("chest", "barrel{CustomName:'\"Box\"'}"))
                        .apply(chest, WORLD)
                        .orElseThrow()
                        .data());
    }

    @Test
    void keysAreBlocksWithoutData() {
        assertThrows(IllegalArgumentException.class, () -> Replacements.of(Map.of("chest{Items:[]}", "barrel")));
        assertThrows(IllegalArgumentException.class, () -> Replacements.of(Map.of(" ", "barrel")));
        assertThrows(IllegalArgumentException.class, () -> Replacements.of(Map.of("oak_stairs[half", "barrel")));
    }
}
