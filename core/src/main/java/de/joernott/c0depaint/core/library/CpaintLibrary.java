package de.joernott.c0depaint.core.library;

import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.minecraft.MinecraftLibraryDefinition;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.LibraryType;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.canvas.Limits;
import de.joernott.c0depaint.core.config.PaintConfig;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The library {@code c0de_paint/cpaint} (docs/concept.md): its types, the errors as constants and its functions.
 */
public final class CpaintLibrary implements Library {
    public static final String NAME = PaintTypes.LIBRARY;

    private final Supplier<PaintConfig> config;
    private final PaintWorld world;

    /**
     * A library with the settings of the server and the world.
     *
     * @param config the current settings
     * @param world the access to the world
     */
    public CpaintLibrary(Supplier<PaintConfig> config, PaintWorld world) {
        this.config = config;
        this.world = world;
    }

    /** A library without a world, e.g. for the unit tests. */
    public CpaintLibrary(Supplier<PaintConfig> config) {
        this(config, PaintWorld.NONE);
    }

    /** The current settings. */
    public PaintConfig config() {
        return config.get();
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Map<String, StructDefinition> types() {
        return Map.of("POINT", PaintTypes.POINT, "BLOCK", PaintTypes.BLOCK);
    }

    @Override
    public Map<String, LibraryType> libraryTypes() {
        return Map.of("CANVAS", PaintTypes.CANVAS, "STYLE", PaintTypes.STYLE);
    }

    /** The errors of the library as INTEGER constants with their codes, e.g. {@code c0de_paint/cpaint/CANVAS_LIMIT}. */
    @Override
    public Map<String, Value> constants() {
        Map<String, Value> constants = new LinkedHashMap<>();
        for (PaintError error : PaintError.values()) {
            if (error.own()) {
                constants.put(error.name(), new IntegerValue(error.code()));
            }
        }
        return constants;
    }

    /** The limits of canvases from the current settings. */
    public Limits limits() {
        PaintConfig settings = config.get();
        return new Limits(settings.get(PaintConfig.CANVAS_MIN), settings.get(PaintConfig.CANVAS_MAX));
    }

    /** The voxels a canvas operation processes per tick. */
    public int budget() {
        return config.get().get(PaintConfig.CANVAS_VOXELS_PER_TICK);
    }

    @Override
    public Map<String, NativeFunction> functions() {
        Map<String, NativeFunction> functions = new LinkedHashMap<>();
        functions.putAll(StyleFunctions.functions());
        functions.putAll(CanvasFunctions.functions(this::limits, world));
        functions.putAll(DrawFunctions.functions(this::limits, this::budget));
        functions.putAll(TransformFunctions.functions(this::limits, this::budget, world));
        functions.put(
                "INVENTORY_BLOCKS",
                Args.function(
                        "INVENTORY_BLOCKS",
                        java.util.List.of("result"),
                        call -> de.joernott.c0de.core.library.NativeResult.of(Args.blocksValue(world.inventoryBlocks(
                                call, (de.joernott.c0de.core.value.StructValue) call.argument("position")))),
                        de.joernott.c0de.core.library.NativeFunction.Parameter.required(
                                "position",
                                MinecraftLibraryDefinition.NAME + "/POSITION",
                                value -> value instanceof de.joernott.c0de.core.value.StructValue struct
                                        && struct.definition().equals(MinecraftLibraryDefinition.POSITION))));
        Args.add(
                functions,
                Args.function(
                        "RENDER_CANVAS",
                        java.util.List.of(),
                        call -> Args.inParts(
                                call,
                                Integer.MAX_VALUE,
                                () -> world.render(
                                        call,
                                        Args.canvas(call, "canvas").copy(),
                                        (de.joernott.c0de.core.value.StructValue) call.argument("position"),
                                        config.get())),
                        Args.canvas("canvas"),
                        Args.position("position"),
                        Args.optionalPosition("input"),
                        Args.optionalPosition("tools"),
                        Args.optionalPosition("output")));
        Args.add(
                functions,
                Args.function(
                        "CANVAS_FROM_WORLD",
                        java.util.List.of("result"),
                        call -> de.joernott.c0de.core.library.NativeResult.of(new CanvasValue(world.fromWorld(
                                call,
                                (de.joernott.c0de.core.value.StructValue) call.argument("from"),
                                (de.joernott.c0de.core.value.StructValue) call.argument("to"),
                                ((de.joernott.c0de.core.value.BooleanValue) call.argument("air")).value(),
                                limits()))),
                        Args.position("from"),
                        Args.position("to"),
                        de.joernott.c0de.core.library.NativeFunction.Parameter.withDefault(
                                "air",
                                "BOOLEAN",
                                value -> value instanceof de.joernott.c0de.core.value.BooleanValue,
                                de.joernott.c0de.core.value.BooleanValue.FALSE)));
        return functions;
    }
}
