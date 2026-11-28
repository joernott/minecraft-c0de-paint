package de.joernott.c0depaint.core.library;

import static de.joernott.c0depaint.core.library.Args.function;

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
import de.joernott.c0depaint.core.canvas.Bounds;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Limits;
import de.joernott.c0depaint.core.canvas.Point;
import de.joernott.c0depaint.core.canvas.Replacements;
import de.joernott.c0depaint.core.draw.Painter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** DRAW_BLOCK and the canvas operations that don't transform the canvas (docs/concept.md#canvas-operations). */
final class CanvasFunctions {
    private static final List<String> RESULT = List.of("result");
    private static final List<String> NONE = List.of();

    private static final Predicate<Value> CANVASES = value -> value instanceof CanvasValue
            || (value instanceof ArrayValue array && array.elements().stream().allMatch(Args.A_CANVAS));

    private CanvasFunctions() {}

    static Map<String, NativeFunction> functions(Supplier<Limits> limits, PaintWorld world) {
        Map<String, NativeFunction> functions = new LinkedHashMap<>();
        add(
                functions,
                function(
                        "DRAW_BLOCK",
                        NONE,
                        call -> {
                            Painter.draw(
                                    Args.canvasReference(call, "canvas"),
                                    limits.get(),
                                    call.process().random(),
                                    List.of(new Painter.Part(
                                            List.of(Args.point(call, "point")), Args.style(call, "style"))));
                            return NativeResult.none();
                        },
                        Args.canvasReference("canvas"),
                        Args.point("point"),
                        Args.style("style")));
        add(
                functions,
                function(
                        "CANVAS_COUNT",
                        RESULT,
                        call -> NativeResult.of(
                                new IntegerValue(Args.canvas(call, "canvas").count())),
                        Args.canvas("canvas")));
        add(
                functions,
                function(
                        "CANVAS_MIN",
                        RESULT,
                        call -> NativeResult.of(Args.pointValue(
                                bounds(Args.canvas(call, "canvas")).min())),
                        Args.canvas("canvas")));
        add(
                functions,
                function(
                        "CANVAS_MAX",
                        RESULT,
                        call -> NativeResult.of(Args.pointValue(
                                bounds(Args.canvas(call, "canvas")).max())),
                        Args.canvas("canvas")));
        add(
                functions,
                function(
                        "CANVAS_GET",
                        RESULT,
                        call -> NativeResult.of(
                                Args.blockValue(Args.canvas(call, "canvas").get(Args.point(call, "point")), 1)),
                        Args.canvas("canvas"),
                        Args.point("point")));
        add(
                functions,
                function(
                        "CANVAS_CLEAR",
                        NONE,
                        call -> {
                            Canvas canvas = Args.canvasReference(call, "canvas");
                            Point from = Args.point(call, "from");
                            Point to = Args.point(call, "to");
                            if ((from == null) != (to == null)) {
                                throw new IllegalArgumentException("CANVAS_CLEAR needs both from and to, or neither");
                            }
                            if (from == null) {
                                canvas.clear();
                            } else {
                                Bounds box = Bounds.of(from, to);
                                keep(canvas, point -> !box.contains(point));
                            }
                            return NativeResult.none();
                        },
                        Args.canvasReference("canvas"),
                        Args.optionalPoint("from"),
                        Args.optionalPoint("to")));
        add(
                functions,
                function(
                        "CANVAS_CROP",
                        NONE,
                        call -> {
                            Bounds box = Bounds.of(Args.point(call, "from"), Args.point(call, "to"));
                            keep(Args.canvasReference(call, "canvas"), box::contains);
                            return NativeResult.none();
                        },
                        Args.canvasReference("canvas"),
                        Args.point("from"),
                        Args.point("to")));
        add(
                functions,
                function(
                        "CANVAS_MOVE",
                        NONE,
                        call -> {
                            Canvas canvas = Args.canvasReference(call, "canvas");
                            Point offset = Args.point(call, "offset");
                            Canvas moved = new Canvas();
                            merge(moved, canvas, true, offset, limits.get());
                            canvas.clear();
                            merge(canvas, moved, true, Point.ORIGIN, limits.get());
                            return NativeResult.none();
                        },
                        Args.canvasReference("canvas"),
                        Args.point("offset")));
        add(
                functions,
                function(
                        "CANVAS_REPLACE",
                        NONE,
                        call -> {
                            Canvas canvas = Args.canvasReference(call, "canvas");
                            String id = id(((StringValue) call.argument("id")).value());
                            List<Point> voxels = new ArrayList<>();
                            Map<Point, Block> old = new HashMap<>();
                            canvas.forEach((x, y, z, block) -> {
                                if (block.id().equals(id)) {
                                    Point point = new Point(x, y, z);
                                    voxels.add(point);
                                    old.put(point, block);
                                }
                            });
                            Painter.draw(
                                    canvas,
                                    limits.get(),
                                    call.process().random(),
                                    List.of(new Painter.Part(voxels, Args.style(call, "style"))));
                            // The new blocks keep the states of the old ones that they have (task 9.1.2)
                            old.forEach((point, before) -> {
                                Block drawn = canvas.get(point.x(), point.y(), point.z());
                                if (drawn != null && !drawn.isAir() && !drawn.equals(before)) {
                                    canvas.set(
                                            point.x(),
                                            point.y(),
                                            point.z(),
                                            Replacements.replace(before, drawn, world::valid));
                                }
                            });
                            return NativeResult.none();
                        },
                        Args.canvasReference("canvas"),
                        Args.text("id"),
                        Args.style("style")));
        add(
                functions,
                function(
                        "CANVAS_MERGE",
                        NONE,
                        call -> {
                            Canvas target = Args.canvasReference(call, "target");
                            String mode = ((StringValue) call.argument("mode")).value();
                            if (!mode.equals("OVERWRITE") && !mode.equals("KEEP")) {
                                throw new IllegalArgumentException("The mode is '" + mode + "', not OVERWRITE or KEEP");
                            }
                            Point offset = Args.point(call, "offset", Point.ORIGIN);
                            List<Canvas> sources = new ArrayList<>();
                            Value source = call.argument("source");
                            if (source instanceof CanvasValue canvas) {
                                sources.add(canvas.canvas());
                            } else {
                                ((ArrayValue) source)
                                        .elements()
                                        .forEach(element -> sources.add(((CanvasValue) element).canvas()));
                            }
                            for (Canvas canvas : sources) {
                                check(canvas, offset, limits.get());
                            }
                            for (Canvas canvas : sources) {
                                merge(target, canvas, mode.equals("OVERWRITE"), offset, limits.get());
                            }
                            return NativeResult.none();
                        },
                        Args.canvasReference("target"),
                        NativeFunction.Parameter.required(
                                "source", Args.CANVAS + "|ARRAY TYPE " + Args.CANVAS, CANVASES),
                        Args.text("mode", "OVERWRITE"),
                        Args.optionalPoint("offset")));
        add(
                functions,
                function(
                        "CANVAS_MATERIALS",
                        RESULT,
                        call -> {
                            Map<String, Long> counts = new java.util.TreeMap<>();
                            Args.canvas(call, "canvas").forEach((x, y, z, block) -> {
                                if (!block.isAir()) {
                                    counts.merge(block.id(), 1L, Long::sum);
                                }
                            });
                            HashValue materials = new HashValue(Type.Simple.INTEGER);
                            counts.forEach((id, count) -> materials.elements().put(id, new IntegerValue(count)));
                            return NativeResult.of(materials);
                        },
                        Args.canvas("canvas")));
        return functions;
    }

    private static void add(Map<String, NativeFunction> functions, NativeFunction function) {
        functions.put(function.name(), function);
    }

    /** The bounds of a canvas, or CANVAS_EMPTY. */
    static Bounds bounds(Canvas canvas) {
        Bounds bounds = canvas.bounds();
        if (bounds == null) {
            throw new PaintException(PaintError.CANVAS_EMPTY, "The canvas is empty");
        }
        return bounds;
    }

    /** A block ID with the namespace minecraft if it has none. */
    static String id(String id) {
        return id.contains(":") ? id : "minecraft:" + id;
    }

    /** Unsets the voxels that don't pass the test. */
    private static void keep(Canvas canvas, Predicate<Point> test) {
        List<Point> remove = new ArrayList<>();
        canvas.forEach((x, y, z, block) -> {
            Point point = new Point(x, y, z);
            if (!test.test(point)) {
                remove.add(point);
            }
        });
        remove.forEach(point -> canvas.set(point, null));
    }

    /** Raises CANVAS_LIMIT if the source moved by the offset leaves the limits. */
    private static void check(Canvas source, Point offset, Limits limits) {
        Bounds bounds = source.bounds();
        if (bounds != null) {
            limits.check(new Bounds(bounds.min().plus(offset), bounds.max().plus(offset)));
        }
    }

    /**
     * Draws the source into the target, moved by the offset. With overwrite, set voxels of the source replace those of
     * the target, otherwise they only fill voxels of the target that are not set.
     */
    static void merge(Canvas target, Canvas source, boolean overwrite, Point offset, Limits limits) {
        check(source, offset, limits);
        source.forEach((x, y, z, block) -> {
            int tx = x + offset.x();
            int ty = y + offset.y();
            int tz = z + offset.z();
            if (overwrite || target.get(tx, ty, tz) == null) {
                target.set(tx, ty, tz, block);
            }
        });
    }
}
