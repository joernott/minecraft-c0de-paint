package de.joernott.c0depaint.core.library;

import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.PaintException;
import de.joernott.c0depaint.core.canvas.Block;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Limits;
import de.joernott.c0depaint.core.canvas.Replacements;
import de.joernott.c0depaint.core.config.PaintConfig;
import de.joernott.c0depaint.core.schematic.Schematic;
import de.joernott.c0depaint.core.schematic.SchematicNames;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The library {@code c0de_paint/schematics} (docs/concept.md#schematics-020, task 9.6): schematics of Litematica and
 * Create into canvases, the items to build them and the blocks the server doesn't have.
 */
public final class SchematicsLibrary implements Library {
    public static final String NAME = "c0de_paint/schematics";

    /** Where the files come from: the client of the player in the game, a folder in the runner library. */
    public interface Files {
        /**
         * Reads a file and gives its bytes to {@code then}; the process may wait meanwhile, then the function is
         * called again in the next steps until the file is there.
         *
         * @param name the checked name of the file
         * @param maxBytes the largest file
         */
        NativeResult read(NativeCall call, String name, long maxBytes, Function<byte[], NativeResult> then);

        /** Lists the schematics of a folder and its subfolders, as names relative to {@code schematics}. */
        NativeResult list(NativeCall call, String folder, Function<List<String>, NativeResult> then);
    }

    /** What only the server knows: the data fixer, its blocks and items. */
    public interface Server {
        /** Without Minecraft: no data fixer; every block of the namespace minecraft exists and is its own item. */
        Server NONE = new Server() {};

        default Schematic.Upgrader upgrader() {
            return Schematic.Upgrader.NONE;
        }

        /** True if the server has the block (in its registry). */
        default boolean available(String id) {
            return id.startsWith("minecraft:");
        }

        /**
         * The items that RENDER_CANVAS takes for the block, by item id; empty for blocks that no item places (water in
         * buckets counts as the bucket).
         */
        default Map<String, Long> items(Block block) {
            return block.isAir() ? Map.of() : Map.of(block.id(), 1L);
        }

        /** True if RENDER_CANVAS places nothing for the block itself (upper halves, bed heads, piston heads). */
        default boolean part(Block block) {
            return false;
        }

        /** The block if it can take its states, or null (for the rule of {@code replace}). */
        default Block valid(Block block) {
            return block;
        }
    }

    private final Supplier<PaintConfig> config;
    private final Files files;
    private final Server server;

    public SchematicsLibrary(Supplier<PaintConfig> config, Files files, Server server) {
        this.config = config;
        this.files = files;
        this.server = server;
    }

    @Override
    public String name() {
        return NAME;
    }

    /** The errors of the library as INTEGER constants, e.g. {@code c0de_paint/schematics/SCHEMATIC_NOT_FOUND}. */
    @Override
    public Map<String, Value> constants() {
        Map<String, Value> constants = new LinkedHashMap<>();
        for (PaintError error : PaintError.values()) {
            if (error.own() && error.library().equals("schematics")) {
                constants.put(error.name(), new IntegerValue(error.code()));
            }
        }
        return constants;
    }

    @Override
    public Map<String, NativeFunction> functions() {
        Map<String, NativeFunction> functions = new LinkedHashMap<>();
        Args.add(
                functions,
                Args.function(
                        "SCHEMATIC_LOAD",
                        List.of("result", "entities"),
                        call -> withSchematic(call, call.argument("region").text(), schematic -> {
                            Canvas canvas = load(schematic, replacements(call));
                            Map<String, Value> results = new LinkedHashMap<>();
                            results.put("result", new CanvasValue(canvas));
                            results.put("entities", new IntegerValue(schematic.entities()));
                            return NativeResult.of(results);
                        }),
                        Args.text("file"),
                        replaceParameter(),
                        Args.text("region", "")));
        Args.add(
                functions,
                Args.function(
                        "SCHEMATIC_MATERIALS",
                        List.of("result", "without_item"),
                        call -> withSchematic(call, "", schematic -> {
                            Replacements replace = replacements(call);
                            Map<String, Long> items = new TreeMap<>();
                            Map<String, Long> without = new TreeMap<>();
                            schematic.forEach(
                                    (x, y, z, block) -> replace.apply(block, server::valid)
                                            .ifPresent(placed -> {
                                                if (server.part(placed)) {
                                                    return;
                                                }
                                                Map<String, Long> needed = server.items(placed);
                                                if (needed.isEmpty()) {
                                                    without.merge(placed.id(), 1L, Long::sum);
                                                } else {
                                                    needed.forEach(
                                                            (item, count) -> items.merge(item, count, Long::sum));
                                                }
                                            }),
                                    server.upgrader());
                            Map<String, Value> results = new LinkedHashMap<>();
                            results.put("result", counts(items));
                            results.put("without_item", counts(without));
                            return NativeResult.of(results);
                        }),
                        Args.text("file"),
                        replaceParameter()));
        Args.add(
                functions,
                Args.function(
                        "SCHEMATIC_MISSING",
                        List.of("result"),
                        call -> withSchematic(call, "", schematic -> {
                            Replacements replace = replacements(call);
                            Map<String, Long> missing = new TreeMap<>();
                            schematic.forEach(
                                    (x, y, z, block) -> replace.apply(block, server::valid)
                                            .ifPresent(placed -> {
                                                if (!server.available(placed.id())) {
                                                    missing.merge(placed.id(), 1L, Long::sum);
                                                }
                                            }),
                                    server.upgrader());
                            return NativeResult.of(counts(missing));
                        }),
                        Args.text("file"),
                        replaceParameter()));
        Args.add(
                functions,
                Args.function(
                        "SCHEMATIC_LIST",
                        List.of("result"),
                        call -> {
                            String folder = SchematicNames.folder(((StringValue) call.argument("folder")).value());
                            return files.list(call, folder, names -> {
                                List<StringValue> values = names.stream()
                                        .sorted()
                                        .map(StringValue::new)
                                        .toList();
                                return NativeResult.of(new ArrayValue(Type.Str.UNLIMITED, values));
                            });
                        },
                        Args.text("folder", "")));
        return functions;
    }

    /** Reads the file of the call, then runs the function on its schematic. */
    private NativeResult withSchematic(NativeCall call, String region, Function<Schematic, NativeResult> then) {
        String name = SchematicNames.file(((StringValue) call.argument("file")).value());
        // The replacements are checked before the file is read, so a mistake doesn't wait for the client
        replacements(call);
        long max = config.get().get(PaintConfig.MAX_SCHEMATIC_SIZE);
        return files.read(call, name, max, bytes -> {
            if (bytes.length > max) {
                throw new PaintException(
                        PaintError.SCHEMATIC_TOO_LARGE, name + " has " + bytes.length + " bytes, more than " + max);
            }
            Schematic schematic;
            try {
                // Unpacked, a schematic may be much larger than its file; 64 times allows for the packing of air
                schematic = Schematic.read(name, bytes, Math.max(max, 1024L) * 64, region, server.upgrader());
            } catch (IllegalArgumentException e) {
                throw new PaintException(PaintError.SCHEMATIC_INVALID, e.getMessage());
            }
            return then.apply(schematic);
        });
    }

    /** The schematic as a canvas, its minimum corner at 0, 0, 0. */
    private Canvas load(Schematic schematic, Replacements replace) {
        PaintConfig settings = config.get();
        Limits limits = new Limits(settings.get(PaintConfig.CANVAS_MIN), settings.get(PaintConfig.CANVAS_MAX));
        if (!limits.contains(schematic.sizeX() - 1, schematic.sizeY() - 1, schematic.sizeZ() - 1)) {
            throw new PaintException(
                    PaintError.SCHEMATIC_TOO_LARGE,
                    "The schematic is " + schematic.sizeX() + "×" + schematic.sizeY() + "×" + schematic.sizeZ()
                            + "; a canvas reaches from " + limits.min() + " to " + limits.max());
        }
        Canvas canvas = new Canvas();
        schematic.forEach(
                (x, y, z, block) -> {
                    Optional<Block> placed = replace.apply(block, server::valid);
                    placed.ifPresent(b -> canvas.set(x, y, z, b));
                },
                server.upgrader());
        return canvas;
    }

    private static NativeFunction.Parameter replaceParameter() {
        return NativeFunction.Parameter.withDefault(
                "replace", "HASH STRING", value -> value instanceof HashValue, new HashValue(Type.Str.UNLIMITED));
    }

    private static Replacements replacements(NativeCall call) {
        Map<String, String> replace = new LinkedHashMap<>();
        ((HashValue) call.argument("replace")).elements().forEach((key, value) -> replace.put(key, value.text()));
        return Replacements.of(replace);
    }

    private static HashValue counts(Map<String, Long> counts) {
        HashValue hash = new HashValue(Type.Simple.INTEGER);
        counts.forEach((key, count) -> hash.elements().put(key, new IntegerValue(count)));
        return hash;
    }
}
