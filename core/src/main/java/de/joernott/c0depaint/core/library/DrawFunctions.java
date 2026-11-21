package de.joernott.c0depaint.core.library;

import static de.joernott.c0depaint.core.library.Args.function;

import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0depaint.core.canvas.Limits;
import de.joernott.c0depaint.core.draw.Bodies;
import de.joernott.c0depaint.core.draw.Painter;
import de.joernott.c0depaint.core.draw.Raster;
import de.joernott.c0depaint.core.draw.Shapes;
import de.joernott.c0depaint.core.style.Style;
import de.joernott.c0depaint.core.transform.Job;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** The drawing functions (docs/concept.md#drawing). They draw into a canvas given as a reference parameter. */
final class DrawFunctions {
    private static final List<String> NONE = List.of();
    private static final Predicate<Value> NUMBER =
            value -> value instanceof IntegerValue || value instanceof FloatValue;

    private DrawFunctions() {}

    static Map<String, NativeFunction> functions(Supplier<Limits> limits, IntSupplier budget) {
        Map<String, NativeFunction> functions = new LinkedHashMap<>();
        add(
                functions,
                function(
                        "DRAW_LINE",
                        NONE,
                        call -> Args.inParts(
                                call,
                                budget.getAsInt(),
                                () -> Painter.job(
                                        Args.canvasReference(call, "canvas"),
                                        limits.get(),
                                        List.of(new Painter.Part(
                                                Raster.line(Args.point(call, "from"), Args.point(call, "to")),
                                                Args.style(call, "style"))))),
                        Args.canvasReference("canvas"),
                        Args.point("from"),
                        Args.point("to"),
                        Args.style("style")));
        flat(
                functions,
                limits,
                budget,
                "DRAW_RECTANGLE",
                call -> Shapes.rectangle(Args.point(call, "a"), Args.point(call, "b"), Args.point(call, "c")),
                Args.point("a"),
                Args.point("b"),
                Args.point("c"));
        flat(
                functions,
                limits,
                budget,
                "DRAW_POLYGON",
                call -> Shapes.polygon(Args.points(call, "points")),
                NativeFunction.Parameter.required("points", "ARRAY STRUCT " + Args.POINT, Args.POINTS));
        flat(
                functions,
                limits,
                budget,
                "DRAW_ELLIPSE",
                call -> Shapes.ellipse(Args.point(call, "center"), Args.point(call, "a"), Args.point(call, "b")),
                Args.point("center"),
                Args.point("a"),
                Args.point("b"));
        flat(
                functions,
                limits,
                budget,
                "DRAW_ARC",
                call -> Shapes.arc(
                        Args.point(call, "center"),
                        Args.point(call, "a"),
                        Args.point(call, "b"),
                        number(call, "start"),
                        number(call, "end")),
                Args.point("center"),
                Args.point("a"),
                Args.point("b"),
                NativeFunction.Parameter.required("start", "FLOAT|INTEGER", NUMBER),
                NativeFunction.Parameter.required("end", "FLOAT|INTEGER", NUMBER));
        body(
                functions,
                limits,
                budget,
                "DRAW_BOX",
                true,
                call -> Bodies.box(Args.point(call, "from"), Args.point(call, "to")),
                Args.point("from"),
                Args.point("to"));
        body(
                functions,
                limits,
                budget,
                "DRAW_ELLIPSOID",
                false,
                call -> Bodies.ellipsoid(
                        Args.point(call, "center"), size(call, "rx"), size(call, "ry"), size(call, "rz")),
                Args.point("center"),
                Args.integer("rx"),
                Args.integer("ry"),
                Args.integer("rz"));
        body(
                functions,
                limits,
                budget,
                "DRAW_CYLINDER",
                true,
                call -> Bodies.cylinder(Args.point(call, "center"), size(call, "r"), size(call, "h"), direction(call)),
                directed());
        body(
                functions,
                limits,
                budget,
                "DRAW_CONE",
                true,
                call -> Bodies.cone(Args.point(call, "center"), size(call, "r"), size(call, "h"), direction(call)),
                directed());
        body(
                functions,
                limits,
                budget,
                "DRAW_PYRAMID",
                true,
                call -> Bodies.pyramid(Args.point(call, "center"), size(call, "r"), size(call, "h"), direction(call)),
                directed());
        for (Bodies.Solid solid : Bodies.Solid.values()) {
            body(
                    functions,
                    limits,
                    budget,
                    "DRAW_" + solid.name(),
                    true,
                    call -> Bodies.solid(solid, Args.point(call, "center"), size(call, "r")),
                    Args.point("center"),
                    Args.integer("r"));
        }
        return functions;
    }

    private static NativeFunction.Parameter[] directed() {
        return new NativeFunction.Parameter[] {
            Args.point("center"), Args.integer("r"), Args.integer("h"), Args.text("direction")
        };
    }

    private static Bodies.Direction direction(NativeCall call) {
        return Bodies.Direction.of(((de.joernott.c0de.core.value.StringValue) call.argument("direction")).value());
    }

    /** A radius or length; values beyond the limits of any canvas raise CANVAS_LIMIT. */
    private static int size(NativeCall call, String name) {
        long value = ((IntegerValue) call.argument(name)).value();
        if (value > 4096) {
            throw new de.joernott.c0depaint.core.PaintException(
                    de.joernott.c0depaint.core.PaintError.CANVAS_LIMIT,
                    name + " is " + value + ", too large for a canvas");
        }
        return (int) Math.max(value, Integer.MIN_VALUE);
    }

    /** A body with the parameters line_style (if it has lines), side_style and fill_style after its own. */
    private static void body(
            Map<String, NativeFunction> functions,
            Supplier<Limits> limits,
            IntSupplier budget,
            String name,
            boolean lines,
            Function<NativeCall, Shapes.Voxels> shape,
            NativeFunction.Parameter... parameters) {
        List<NativeFunction.Parameter> all = new ArrayList<>();
        all.add(Args.canvasReference("canvas"));
        all.addAll(List.of(parameters));
        if (lines) {
            all.add(Args.optionalStyle("line_style"));
        }
        all.add(Args.optionalStyle("side_style"));
        all.add(Args.optionalStyle("fill_style"));
        add(
                functions,
                function(
                        name,
                        NONE,
                        call -> Args.inParts(
                                call,
                                budget.getAsInt(),
                                () -> job(
                                        call,
                                        limits,
                                        shape.apply(call),
                                        Painter.resolve(
                                                lines ? Args.style(call, "line_style") : null,
                                                Args.style(call, "side_style"),
                                                Args.style(call, "fill_style")))),
                        all.toArray(NativeFunction.Parameter[]::new)));
    }

    static void add(Map<String, NativeFunction> functions, NativeFunction function) {
        functions.put(function.name(), function);
    }

    static double number(NativeCall call, String name) {
        Value value = call.argument(name);
        return value instanceof IntegerValue integer ? integer.value() : ((FloatValue) value).value();
    }

    /** A flat shape with the parameters line_style and fill_style after its own. */
    private static void flat(
            Map<String, NativeFunction> functions,
            Supplier<Limits> limits,
            IntSupplier budget,
            String name,
            Function<NativeCall, Shapes.Voxels> shape,
            NativeFunction.Parameter... parameters) {
        List<NativeFunction.Parameter> all = new ArrayList<>();
        all.add(Args.canvasReference("canvas"));
        all.addAll(List.of(parameters));
        all.add(Args.optionalStyle("line_style"));
        all.add(Args.optionalStyle("fill_style"));
        add(
                functions,
                function(
                        name,
                        NONE,
                        call -> Args.inParts(
                                call,
                                budget.getAsInt(),
                                () -> job(
                                        call,
                                        limits,
                                        shape.apply(call),
                                        Painter.resolve(
                                                Args.style(call, "line_style"), null, Args.style(call, "fill_style")))),
                        all.toArray(NativeFunction.Parameter[]::new)));
    }

    /** Draws the parts of a shape with the styles of line, side and fill (after the fallback of STYLE_NONE). */
    static Job job(NativeCall call, Supplier<Limits> limits, Shapes.Voxels voxels, Style[] styles) {
        List<Painter.Part> parts = new ArrayList<>();
        parts.add(new Painter.Part(voxels.inside(), styles[2]));
        if (styles[1] != null) {
            parts.add(new Painter.Part(voxels.faces(), styles[1]));
        }
        if (styles[0] != null) {
            parts.add(new Painter.Part(voxels.lines(), styles[0]));
        }
        return Painter.job(Args.canvasReference(call, "canvas"), limits.get(), parts);
    }
}
