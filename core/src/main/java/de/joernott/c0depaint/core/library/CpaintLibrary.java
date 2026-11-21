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

    @Override
    public Map<String, NativeFunction> functions() {
        Map<String, NativeFunction> functions = new LinkedHashMap<>();
        functions.putAll(StyleFunctions.functions());
        functions.putAll(CanvasFunctions.functions(this::limits));
        functions.putAll(DrawFunctions.functions(this::limits));
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
        return functions;
    }
}
