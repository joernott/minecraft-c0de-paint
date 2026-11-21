package de.joernott.c0depaint.core.library;

import static de.joernott.c0depaint.core.library.Args.function;

import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0depaint.core.canvas.Bounds;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Limits;
import de.joernott.c0depaint.core.canvas.Point;
import de.joernott.c0depaint.core.transform.Transform;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** CANVAS_SCALE, CANVAS_ROTATE and CANVAS_MIRROR (docs/concept.md#canvas-operations), done in parts. */
final class TransformFunctions {
    private static final List<String> NONE = List.of();
    private static final Predicate<Value> NUMBER =
            value -> value instanceof IntegerValue || value instanceof FloatValue;

    private TransformFunctions() {}

    static Map<String, NativeFunction> functions(Supplier<Limits> limits, IntSupplier budget, PaintWorld world) {
        Map<String, NativeFunction> functions = new LinkedHashMap<>();
        DrawFunctions.add(
                functions,
                function(
                        "CANVAS_SCALE",
                        NONE,
                        call -> Args.inParts(
                                call,
                                budget.getAsInt(),
                                () -> Transform.scale(
                                        Args.canvasReference(call, "canvas"),
                                        percent(call, "x"),
                                        percent(call, "y"),
                                        percent(call, "z"),
                                        Args.point(call, "center", Point.ORIGIN),
                                        limits.get())),
                        Args.canvasReference("canvas"),
                        Args.integer("x"),
                        Args.integer("y"),
                        Args.integer("z"),
                        Args.optionalPoint("center")));
        DrawFunctions.add(
                functions,
                function(
                        "CANVAS_ROTATE",
                        NONE,
                        call -> Args.inParts(
                                call,
                                budget.getAsInt(),
                                () -> Transform.rotate(
                                        Args.canvasReference(call, "canvas"),
                                        Args.point(call, "center"),
                                        axis(call),
                                        DrawFunctions.number(call, "degrees"),
                                        world::valid,
                                        limits.get())),
                        Args.canvasReference("canvas"),
                        Args.point("center"),
                        Args.text("axis"),
                        NativeFunction.Parameter.required("degrees", "FLOAT|INTEGER", NUMBER)));
        DrawFunctions.add(
                functions,
                function(
                        "CANVAS_MIRROR",
                        NONE,
                        call -> Args.inParts(call, budget.getAsInt(), () -> {
                            Canvas canvas = Args.canvasReference(call, "canvas");
                            int axis = axis(call);
                            long twice;
                            if (call.provided("position")) {
                                twice = Math.round(2 * DrawFunctions.number(call, "position"));
                            } else {
                                // The middle of the bounds, so the bounds stay the same
                                Bounds bounds = canvas.bounds();
                                twice = bounds == null
                                        ? 0
                                        : (long) bounds.min().get(axis)
                                                + bounds.max().get(axis);
                            }
                            return Transform.mirror(canvas, axis, twice, world::valid, limits.get());
                        }),
                        Args.canvasReference("canvas"),
                        Args.text("axis"),
                        NativeFunction.Parameter.optional("position", "FLOAT|INTEGER", NUMBER)));
        return functions;
    }

    private static int axis(NativeCall call) {
        String axis = ((StringValue) call.argument("axis")).value();
        return switch (axis) {
            case "x", "X" -> 0;
            case "y", "Y" -> 1;
            case "z", "Z" -> 2;
            default -> throw new IllegalArgumentException("The axis is '" + axis + "', not x, y or z");
        };
    }

    private static int percent(NativeCall call, String name) {
        long value = ((IntegerValue) call.argument(name)).value();
        if (value < 1 || value > 100_000) {
            throw new IllegalArgumentException(name + " is " + value + "%, not 1 to 100000");
        }
        return (int) value;
    }
}
