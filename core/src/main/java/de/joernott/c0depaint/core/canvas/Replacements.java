package de.joernott.c0depaint.core.canvas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

/**
 * The rule for replacing blocks (docs/block_review.md, point 4), used by CANVAS_REPLACE and by the parameter
 * {@code replace} of the schematics (task 9.1.2):
 *
 * <ul>
 *   <li>A key matches a block by id ({@code create:andesite_casing}) or by id and states ({@code oak_stairs[half=top]});
 *       the key with the most states that match wins.
 *   <li>The new block keeps every state of the old one that it also has, then the states of the replacement apply.
 *   <li>The data of the block entity is kept unless the replacement has data of its own.
 *   <li>A replacement "" leaves the voxel not set.
 * </ul>
 */
public final class Replacements {
    /** No replacements. */
    public static final Replacements NONE = new Replacements(List.of());

    private record Rule(Block match, Block replacement) {}

    private final List<Rule> rules;

    private Replacements(List<Rule> rules) {
        this.rules = rules;
    }

    /**
     * The replacements of a HASH STRING: keys and values are blocks like {@code oak_stairs[half=top]}; ids without a
     * namespace get {@code minecraft:}.
     *
     * @throws IllegalArgumentException if a key or value isn't a block
     */
    public static Replacements of(Map<String, String> replace) {
        List<Rule> rules = new ArrayList<>();
        replace.forEach((key, value) -> {
            if (key.isBlank()) {
                throw new IllegalArgumentException("A key of replace is empty");
            }
            Block match = block(key);
            if (match.hasData()) {
                throw new IllegalArgumentException("The key " + key + " of replace has data; only id and states match");
            }
            rules.add(new Rule(match, value.isBlank() ? null : block(value)));
        });
        // The most specific key first; the same number of states: sorted by text, so the result doesn't depend on
        // the order of the HASH
        rules.sort(
                Comparator.comparingInt((Rule rule) -> -rule.match.properties().size())
                        .thenComparing(rule -> rule.match.text()));
        return new Replacements(rules);
    }

    /** A block written by a player: the namespace {@code minecraft:} is added where it is missing. */
    public static Block block(String text) {
        Block block = Block.parse(text.strip());
        String id = block.id();
        if (!id.contains(":")) {
            block = new Block("minecraft:" + id, block.properties(), block.data());
        }
        return block;
    }

    public boolean isEmpty() {
        return rules.isEmpty();
    }

    /**
     * The block after the replacements.
     *
     * @param block the block
     * @param valid the block if it can take its states, or null ({@code PaintWorld.valid})
     * @return the new block, the same block if no key matches, or empty if the voxel is to be left not set
     */
    public Optional<Block> apply(Block block, UnaryOperator<Block> valid) {
        for (Rule rule : rules) {
            if (matches(rule.match, block)) {
                return rule.replacement == null
                        ? Optional.empty()
                        : Optional.of(replace(block, rule.replacement, valid));
            }
        }
        return Optional.of(block);
    }

    /** True if the block has the id of the key and every state of the key. */
    public static boolean matches(Block key, Block block) {
        if (!key.id().equals(block.id())) {
            return false;
        }
        for (Map.Entry<String, String> state : key.properties().entrySet()) {
            if (!state.getValue().equals(block.properties().get(state.getKey()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * The new block for an old one: the states of the old block that the new one has, then the states of the new
     * block; the data of the new block, or else that of the old one.
     */
    public static Block replace(Block old, Block replacement, UnaryOperator<Block> valid) {
        TreeMap<String, String> states = new TreeMap<>();
        Block bare = new Block(replacement.id());
        old.properties().forEach((name, value) -> {
            if (!replacement.properties().containsKey(name) && valid.apply(bare.with(name, value)) != null) {
                states.put(name, value);
            }
        });
        states.putAll(replacement.properties());
        return new Block(replacement.id(), states, replacement.hasData() ? replacement.data() : old.data());
    }
}
