package de.joernott.c0depaint.core.canvas;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A block with its states, e.g. {@code minecraft:oak_stairs[facing=north,half=bottom]}: what a voxel of a canvas
 * holds. Air is a block too ({@link #AIR}).
 *
 * @param id the ID of the block, e.g. {@code minecraft:stone}
 * @param properties the block states by name, sorted
 */
public record Block(String id, SortedMap<String, String> properties) {
    /** Air, which removes the block in the world when a canvas is rendered. */
    public static final Block AIR = new Block("minecraft:air");

    public Block {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("A block needs an ID");
        }
        properties = java.util.Collections.unmodifiableSortedMap(new TreeMap<>(properties));
    }

    /** A block without states. */
    public Block(String id) {
        this(id, new TreeMap<>());
    }

    /** A block with states. */
    public static Block of(String id, Map<String, String> properties) {
        return new Block(id, new TreeMap<>(properties));
    }

    /** True for air. */
    public boolean isAir() {
        return id.equals(AIR.id) || id.equals("minecraft:cave_air") || id.equals("minecraft:void_air");
    }

    /** The same block with another value of a state. */
    public Block with(String property, String value) {
        TreeMap<String, String> changed = new TreeMap<>(properties);
        changed.put(property, value);
        return new Block(id, changed);
    }

    /** The text of the block, e.g. {@code minecraft:oak_stairs[facing=north,half=bottom]}. */
    public String text() {
        if (properties.isEmpty()) {
            return id;
        }
        StringBuilder text = new StringBuilder(id).append('[');
        properties.forEach((key, value) -> {
            if (text.charAt(text.length() - 1) != '[') {
                text.append(',');
            }
            text.append(key).append('=').append(value);
        });
        return text.append(']').toString();
    }

    /** The block of a text created by {@link #text()}. */
    public static Block parse(String text) {
        int open = text.indexOf('[');
        if (open < 0) {
            return new Block(text);
        }
        TreeMap<String, String> properties = new TreeMap<>();
        String states = text.substring(open + 1, text.length() - 1);
        if (!states.isEmpty()) {
            for (String state : states.split(",")) {
                int equals = state.indexOf('=');
                properties.put(state.substring(0, equals), state.substring(equals + 1));
            }
        }
        return new Block(text.substring(0, open), properties);
    }

    @Override
    public String toString() {
        return text();
    }
}
