package de.joernott.c0depaint.core.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The settings of c0de paint (docs/concept.md#configuration) in {@code config/c0de_paint.properties} on the server,
 * like {@code c0de.properties} of c0de.
 *
 * <p>A missing file is created with the default values. Invalid or unknown entries don't stop the mod: they are
 * reported as {@link #problems()} and the default value is used instead.
 */
public final class PaintConfig {
    public static final String SERVER_FILE = "c0de_paint.properties";

    /** The smallest coordinate of a canvas on every axis. */
    public static final IntSetting CANVAS_MIN =
            new IntSetting("canvasMin", -128, -1024, 0, "Smallest coordinate of a canvas on every axis (-1024 to 0).");

    /** The largest coordinate of a canvas on every axis. */
    public static final IntSetting CANVAS_MAX =
            new IntSetting("canvasMax", 255, 0, 1023, "Largest coordinate of a canvas on every axis (0 to 1023).");

    /** The blocks RENDER_CANVAS places per tick. */
    public static final IntSetting RENDER_BLOCKS_PER_TICK = new IntSetting(
            "renderBlocksPerTick", 64, 1, 1024, "Blocks that RENDER_CANVAS places per tick (1 to 1024).");

    /** How much faster than a player RENDER_CANVAS mines blocks with tools. */
    public static final IntSetting RENDER_MINING_SPEED = new IntSetting(
            "renderMiningSpeed",
            1,
            0,
            100,
            "How much faster than a player RENDER_CANVAS mines blocks with tools (0 to 100): 1 is the speed of a",
            "player, higher numbers are faster, 0 breaks blocks instantly, as in creative mode.");

    /** The voxels a canvas operation processes per tick. */
    public static final IntSetting CANVAS_VOXELS_PER_TICK = new IntSetting(
            "canvasVoxelsPerTick",
            65536,
            1024,
            1048576,
            "Voxels that a canvas operation processes per tick (1024 to 1048576). Larger operations are spread over",
            "several ticks, while the process waits.");

    /** The settings of the server file. */
    /** The largest schematic file that c0de_paint/schematics reads (0.2.0). */
    public static final IntSetting MAX_SCHEMATIC_SIZE = new IntSetting(
            "maxSchematicSize",
            4 * 1024 * 1024,
            1024,
            64 * 1024 * 1024,
            "The largest schematic file in bytes (1024 to 67108864) that the functions of c0de_paint/schematics read",
            "from the client of a player.");

    public static final List<Setting<?>> SERVER = List.of(
            CANVAS_MIN,
            CANVAS_MAX,
            RENDER_BLOCKS_PER_TICK,
            RENDER_MINING_SPEED,
            CANVAS_VOXELS_PER_TICK,
            MAX_SCHEMATIC_SIZE);

    /** The settings of the client, in {@code config/c0de_paint-client.properties} (0.2.0). */
    public static final String CLIENT_FILE = "c0de_paint-client.properties";

    /** Whether the client reads schematics for the processes of its player. */
    public static final BooleanSetting ALLOW_SCHEMATICS = new BooleanSetting(
            "allowSchematics",
            true,
            "Read the schematics in the folder schematics of this computer for the c0de processes you run. With",
            "false, the functions of c0de_paint/schematics raise SCHEMATICS_DISABLED.");

    public static final List<Setting<?>> CLIENT = List.of(ALLOW_SCHEMATICS);

    private final Map<Setting<?>, Object> values;
    private final List<String> problems;

    private PaintConfig(Map<Setting<?>, Object> values, List<String> problems) {
        this.values = values;
        this.problems = List.copyOf(problems);
    }

    /** The default values of some settings. */
    public static PaintConfig defaults(List<Setting<?>> settings) {
        Map<Setting<?>, Object> values = new LinkedHashMap<>();
        settings.forEach(setting -> values.put(setting, setting.defaultValue()));
        return new PaintConfig(values, List.of());
    }

    /**
     * Reads the settings from a file, or creates it with the default values if it doesn't exist.
     *
     * @throws IOException if the file can't be read or created
     */
    public static PaintConfig load(Path file, List<Setting<?>> settings) throws IOException {
        if (!Files.exists(file)) {
            Files.createDirectories(file.toAbsolutePath().getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                writer.write(defaultFile(settings));
            }
            return defaults(settings);
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return of(properties, settings, file.getFileName().toString());
    }

    /** The settings of some properties; {@code source} names them in the problems. */
    public static PaintConfig of(Properties properties, List<Setting<?>> settings, String source) {
        Map<Setting<?>, Object> values = new LinkedHashMap<>();
        List<String> problems = new ArrayList<>();
        for (Setting<?> setting : settings) {
            String text = properties.getProperty(setting.key());
            Object value = setting.defaultValue();
            if (text != null) {
                Object parsed = setting.parse(text.strip());
                if (parsed == null) {
                    problems.add(source + ": invalid value '" + text + "' for " + setting.key() + ", using "
                            + setting.defaultValue());
                } else {
                    value = parsed;
                }
            }
            values.put(setting, value);
        }
        for (String key : properties.stringPropertyNames()) {
            if (settings.stream().noneMatch(setting -> setting.key().equals(key))) {
                problems.add(source + ": unknown setting " + key);
            }
        }
        return new PaintConfig(values, problems);
    }

    /** The content of a new file: every setting with its description and default value. */
    static String defaultFile(List<Setting<?>> settings) {
        StringBuilder text = new StringBuilder("# Settings of c0de paint\n");
        for (Setting<?> setting : settings) {
            text.append('\n');
            for (String line : setting.description()) {
                text.append("# ").append(line).append('\n');
            }
            text.append(setting.key())
                    .append('=')
                    .append(setting.defaultValue())
                    .append('\n');
        }
        return text.toString();
    }

    /** The value of a setting of this file. */
    @SuppressWarnings("unchecked")
    public <T> T get(Setting<T> setting) {
        Object value = values.get(setting);
        if (value == null) {
            throw new IllegalArgumentException("Not a setting of this file: " + setting.key());
        }
        return (T) value;
    }

    /** The invalid and unknown entries of the file. */
    public List<String> problems() {
        return problems;
    }

    /** A setting: its key, default value and description. */
    public sealed interface Setting<T> permits IntSetting, BooleanSetting {
        String key();

        T defaultValue();

        List<String> description();

        /** The value of a text, or null if it is not valid. */
        T parse(String text);
    }

    /** An INTEGER setting with a range. */
    public record IntSetting(String key, Integer defaultValue, int min, int max, List<String> description)
            implements Setting<Integer> {
        public IntSetting(String key, int defaultValue, int min, int max, String... description) {
            this(key, defaultValue, min, max, List.of(description));
        }

        @Override
        public Integer parse(String text) {
            try {
                int value = Integer.parseInt(text);
                return value >= min && value <= max ? value : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    /** A BOOLEAN setting: true or false. */
    public record BooleanSetting(String key, Boolean defaultValue, List<String> description)
            implements Setting<Boolean> {
        public BooleanSetting(String key, boolean defaultValue, String... description) {
            this(key, defaultValue, List.of(description));
        }

        @Override
        public Boolean parse(String text) {
            return text.equalsIgnoreCase("true") ? Boolean.TRUE : text.equalsIgnoreCase("false") ? Boolean.FALSE : null;
        }
    }
}
