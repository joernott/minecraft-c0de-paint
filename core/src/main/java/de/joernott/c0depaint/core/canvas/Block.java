package de.joernott.c0depaint.core.canvas;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A block with its states and the data of its block entity, e.g.
 * {@code minecraft:oak_sign[rotation=4]{front_text:{messages:['"Hi"','""','""','""']}}}: what a voxel of a canvas
 * holds. Air is a block too ({@link #AIR}).
 *
 * @param id the ID of the block, e.g. {@code minecraft:stone}
 * @param properties the block states by name, sorted
 * @param data the data of the block entity as SNBT, as {@code /data get block} shows it, or "" for none (task 9.2)
 */
public record Block(String id, SortedMap<String, String> properties, String data) {
    /** Air, which removes the block in the world when a canvas is rendered. */
    public static final Block AIR = new Block("minecraft:air");

    public Block {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("A block needs an ID");
        }
        properties = java.util.Collections.unmodifiableSortedMap(new TreeMap<>(properties));
        data = data == null ? "" : data.strip();
        if (!data.isEmpty() && !data.startsWith("{")) {
            throw new IllegalArgumentException("The data of " + id + " is not a compound like {key:value}: " + data);
        }
        if (data.indexOf('\n') >= 0 || data.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("The data of " + id + " has a line break; write it on one line");
        }
    }

    /** A block with states, without data. */
    public Block(String id, SortedMap<String, String> properties) {
        this(id, properties, "");
    }

    /** A block without states and data. */
    public Block(String id) {
        this(id, new TreeMap<>(), "");
    }

    /** A block with states. */
    public static Block of(String id, Map<String, String> properties) {
        return new Block(id, new TreeMap<>(properties), "");
    }

    /** A block with states and data. */
    public static Block of(String id, Map<String, String> properties, String data) {
        return new Block(id, new TreeMap<>(properties), data);
    }

    /** True for air. */
    public boolean isAir() {
        return id.equals(AIR.id) || id.equals("minecraft:cave_air") || id.equals("minecraft:void_air");
    }

    /** True if the block has data of a block entity. */
    public boolean hasData() {
        return !data.isEmpty();
    }

    /** The same block with another value of a state. */
    public Block with(String property, String value) {
        TreeMap<String, String> changed = new TreeMap<>(properties);
        changed.put(property, value);
        return new Block(id, changed, data);
    }

    /** The same block with other data. */
    public Block withData(String data) {
        return new Block(id, properties, data);
    }

    /** The same block with other states, keeping the data. */
    public Block withProperties(Map<String, String> properties) {
        return new Block(id, new TreeMap<>(properties), data);
    }

    /**
     * The text of the block without its data, e.g. {@code minecraft:oak_stairs[facing=north,half=bottom]}: the block
     * state as Minecraft writes it.
     */
    public String state() {
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

    /** The text of the block with its data, as {@code /setblock} takes it. */
    public String text() {
        return state() + data;
    }

    /** The block of a text created by {@link #text()} or {@link #state()}. */
    public static Block parse(String text) {
        int brace = text.indexOf('{');
        int bracket = text.indexOf('[');
        String data = "";
        String state = text;
        if (brace >= 0 && (bracket < 0 || brace < bracket)) {
            // No states: the data starts right after the id
            data = text.substring(brace);
            state = text.substring(0, brace);
        } else if (bracket >= 0) {
            int close = text.indexOf(']', bracket);
            if (close < 0) {
                throw new IllegalArgumentException("The states of " + text + " have no ]");
            }
            data = text.substring(close + 1);
            state = text.substring(0, close + 1);
        }
        int open = state.indexOf('[');
        if (open < 0) {
            return new Block(state, new TreeMap<>(), data);
        }
        TreeMap<String, String> properties = new TreeMap<>();
        String states = state.substring(open + 1, state.length() - 1);
        if (!states.isEmpty()) {
            for (String entry : states.split(",")) {
                int equals = entry.indexOf('=');
                if (equals < 0) {
                    throw new IllegalArgumentException("The state " + entry + " of " + text + " has no =");
                }
                properties.put(
                        entry.substring(0, equals).strip(),
                        entry.substring(equals + 1).strip());
            }
        }
        return new Block(state.substring(0, open), properties, data);
    }

    @Override
    public String toString() {
        return text();
    }
}
