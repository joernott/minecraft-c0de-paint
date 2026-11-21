package de.joernott.c0depaint.core.library;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.eval.Variable;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.PaintException;
import de.joernott.c0depaint.core.canvas.Block;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Point;
import de.joernott.c0depaint.core.style.Style;
import de.joernott.c0depaint.core.style.Weighted;
import de.joernott.c0depaint.core.transform.Job;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

/** The parameters of the functions of c0de_paint/cpaint and the conversions between values and the model. */
final class Args {
    private Args() {}

    static final Predicate<Value> A_CANVAS = value -> value instanceof CanvasValue;
    static final Predicate<Value> A_STYLE = value -> value instanceof StyleValue;
    static final Predicate<Value> A_POINT =
            value -> value instanceof StructValue struct && struct.definition().equals(PaintTypes.POINT);
    static final Predicate<Value> A_BLOCK =
            value -> value instanceof StructValue struct && struct.definition().equals(PaintTypes.BLOCK);
    static final Predicate<Value> BLOCKS = value ->
            value instanceof ArrayValue array && array.elements().stream().allMatch(A_BLOCK);
    static final Predicate<Value> POINTS = value ->
            value instanceof ArrayValue array && array.elements().stream().allMatch(A_POINT);
    static final Predicate<Value> INTEGER = value -> value instanceof IntegerValue;
    static final Predicate<Value> TEXT = value -> value instanceof StringValue;

    static final String CANVAS = PaintTypes.CANVAS.name();
    static final String STYLE = PaintTypes.STYLE.name();
    static final String POINT = PaintTypes.POINT.name();
    static final String BLOCK = PaintTypes.BLOCK.name();

    // ------------------------------------------------------------------------------------------------- parameters

    static NativeFunction.Parameter canvasReference(String name) {
        return NativeFunction.Parameter.reference(name, CANVAS);
    }

    static NativeFunction.Parameter canvas(String name) {
        return NativeFunction.Parameter.required(name, CANVAS, A_CANVAS);
    }

    static NativeFunction.Parameter style(String name) {
        return NativeFunction.Parameter.required(name, STYLE, A_STYLE);
    }

    static NativeFunction.Parameter optionalStyle(String name) {
        return NativeFunction.Parameter.optional(name, STYLE, A_STYLE);
    }

    static NativeFunction.Parameter point(String name) {
        return NativeFunction.Parameter.required(name, POINT, A_POINT);
    }

    static NativeFunction.Parameter optionalPoint(String name) {
        return NativeFunction.Parameter.optional(name, POINT, A_POINT);
    }

    static NativeFunction.Parameter block(String name) {
        return NativeFunction.Parameter.required(name, BLOCK, A_BLOCK);
    }

    static NativeFunction.Parameter blocks(String name) {
        return NativeFunction.Parameter.required(name, "ARRAY STRUCT " + BLOCK, BLOCKS);
    }

    static NativeFunction.Parameter integer(String name) {
        return NativeFunction.Parameter.required(name, "INTEGER", INTEGER);
    }

    static NativeFunction.Parameter text(String name) {
        return NativeFunction.Parameter.required(name, "STRING", TEXT);
    }

    static NativeFunction.Parameter text(String name, String defaultValue) {
        return NativeFunction.Parameter.withDefault(name, "STRING", TEXT, new StringValue(defaultValue));
    }

    static NativeFunction.Parameter integer(String name, long defaultValue) {
        return NativeFunction.Parameter.withDefault(name, "INTEGER", INTEGER, new IntegerValue(defaultValue));
    }

    static final Predicate<Value> A_POSITION = value -> value instanceof StructValue struct
            && struct.definition().equals(de.joernott.c0de.core.library.minecraft.MinecraftLibraryDefinition.POSITION);
    static final String POSITION =
            de.joernott.c0de.core.library.minecraft.MinecraftLibraryDefinition.NAME + "/POSITION";

    static NativeFunction.Parameter position(String name) {
        return NativeFunction.Parameter.required(name, POSITION, A_POSITION);
    }

    static NativeFunction.Parameter optionalPosition(String name) {
        return NativeFunction.Parameter.optional(name, POSITION, A_POSITION);
    }

    static void add(java.util.Map<String, NativeFunction> functions, NativeFunction function) {
        functions.put(function.name(), function);
    }

    /** A function whose model errors become errors of the CALL statement. */
    static NativeFunction function(
            String name,
            List<String> results,
            NativeFunction.Implementation implementation,
            NativeFunction.Parameter... parameters) {
        return new NativeFunction(name, List.of(parameters), results, call -> {
            try {
                return implementation.call(call);
            } catch (PaintException e) {
                return NativeResult.error(e.error().code(), e.getMessage());
            } catch (IllegalArgumentException e) {
                return NativeResult.error(PaintError.INVALID_ARGUMENT.code(), e.getMessage());
            }
        });
    }

    /**
     * Runs a job in parts (docs/concept.md#configuration, canvasVoxelsPerTick): the first call starts it, and while it
     * isn't done, the process waits on the CALL line and the function does the next part in the next tick.
     */
    static NativeResult inParts(NativeCall call, int budget, java.util.function.Supplier<Job> start) {
        Job job = call.state() instanceof Job running ? running : start.get();
        if (job.step(budget, call.process().random())) {
            call.setState(null);
            return NativeResult.none();
        }
        call.setState(job);
        return NativeResult.waitForValue();
    }

    // ------------------------------------------------------------------------------------------------- values

    /** The canvas in the variable of a reference parameter, which the function changes. */
    static Canvas canvasReference(NativeCall call, String name) {
        Variable variable = call.reference(name).orElseThrow();
        if (!(variable.value() instanceof CanvasValue value)) {
            throw C0deException.of(
                    ErrorCode.TYPE_MISMATCH,
                    name + " must be a variable of the type " + CANVAS + ", not " + variable.type());
        }
        return value.canvas();
    }

    static Canvas canvas(NativeCall call, String name) {
        return ((CanvasValue) call.argument(name)).canvas();
    }

    static Style style(NativeCall call, String name) {
        Value value = call.argument(name);
        return value == null ? Style.NONE : ((StyleValue) value).style();
    }

    static Point point(NativeCall call, String name) {
        Value value = call.argument(name);
        return value == null ? null : point(value);
    }

    static Point point(NativeCall call, String name, Point defaultValue) {
        Point point = point(call, name);
        return point == null ? defaultValue : point;
    }

    static Point point(Value value) {
        StructValue struct = (StructValue) value;
        return new Point(coordinate(struct, "x"), coordinate(struct, "y"), coordinate(struct, "z"));
    }

    private static int coordinate(StructValue struct, String element) {
        long value = ((IntegerValue) struct.get(element)).value();
        if (value < Integer.MIN_VALUE / 2 || value > Integer.MAX_VALUE / 2) {
            throw new PaintException(
                    PaintError.CANVAS_LIMIT, "The coordinate " + element + " " + value + " is too large");
        }
        return (int) value;
    }

    static List<Point> points(NativeCall call, String name) {
        List<Point> points = new ArrayList<>();
        for (Value element : ((ArrayValue) call.argument(name)).elements()) {
            points.add(point(element));
        }
        return points;
    }

    static StructValue pointValue(Point point) {
        StructValue value = new StructValue(PaintTypes.POINT);
        value.set("x", new IntegerValue(point.x()));
        value.set("y", new IntegerValue(point.y()));
        value.set("z", new IntegerValue(point.z()));
        return value;
    }

    /** The block of a BLOCK, without its weight. */
    static Block block(Value value) {
        StructValue struct = (StructValue) value;
        String id = ((StringValue) struct.get("id")).value();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("The block has no id");
        }
        Map<String, String> properties = new TreeMap<>();
        ((HashValue) struct.get("properties")).elements().forEach((key, state) -> properties.put(key, state.text()));
        return Block.of(id.contains(":") ? id : "minecraft:" + id, properties);
    }

    static Block block(NativeCall call, String name) {
        return block(call.argument(name));
    }

    /** The blocks of an ARRAY OF BLOCK with their weights, which must be 1 or more. */
    static List<Weighted> blocks(NativeCall call, String name) {
        List<Weighted> blocks = new ArrayList<>();
        for (Value element : ((ArrayValue) call.argument(name)).elements()) {
            long weight = ((IntegerValue) ((StructValue) element).get("weight")).value();
            Block block = block(element);
            if (weight < 1 || weight > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "The weight of " + block.text() + " is " + weight + ", not 1 or more");
            }
            blocks.add(new Weighted(block, (int) weight));
        }
        if (blocks.isEmpty()) {
            throw new IllegalArgumentException(name + " has no blocks");
        }
        return blocks;
    }

    /** A BLOCK with its weight; null gives the id "" of a voxel that is not set. */
    static StructValue blockValue(Block block, long weight) {
        StructValue value = new StructValue(PaintTypes.BLOCK);
        HashValue properties = new HashValue(Type.Str.UNLIMITED);
        if (block != null) {
            value.set("id", new StringValue(block.id()));
            block.properties().forEach((key, state) -> properties.elements().put(key, new StringValue(state)));
        }
        value.set("properties", properties);
        value.set("weight", new IntegerValue(weight));
        return value;
    }

    /** An ARRAY OF BLOCK. */
    static ArrayValue blocksValue(List<Weighted> blocks) {
        List<Value> elements = new ArrayList<>();
        blocks.forEach(block -> elements.add(blockValue(block.block(), block.weight())));
        return new ArrayValue(new Type.Struct(PaintTypes.BLOCK), elements);
    }
}
