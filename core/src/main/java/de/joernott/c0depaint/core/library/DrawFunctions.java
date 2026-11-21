package de.joernott.c0depaint.core.library;

import static de.joernott.c0depaint.core.library.Args.function;

import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0depaint.core.canvas.Limits;
import de.joernott.c0depaint.core.draw.Painter;
import de.joernott.c0depaint.core.draw.Raster;
import de.joernott.c0depaint.core.draw.Shapes;
import de.joernott.c0depaint.core.style.Style;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** The drawing functions (docs/concept.md#drawing). They draw into a canvas given as a reference parameter. */
final class DrawFunctions {
    private static final List<String> NONE = List.of();
    private static final Predicate<Value> NUMBER =
            value -> value instanceof IntegerValue || value instanceof FloatValue;

    private DrawFunctions() {}

    static Map<String, NativeFunction> functions(Supplier<Limits> limits) {
        Map<String, NativeFunction> functions = new LinkedHashMap<>();
        add(
                functions,
                function(
                        "DRAW_LINE",
                        NONE,
                        call -> {
                            Painter.draw(
                                    Args.canvasReference(call, "canvas"),
                                    limits.get(),
                                    call.process().random(),
                                    List.of(new Painter.Part(
                                            Raster.line(Args.point(call, "from"), Args.point(call, "to")),
                                            Args.style(call, "style"))));
                            return NativeResult.none();
                        },
                        Args.canvasReference("canvas"),
                        Args.point("from"),
                        Args.point("to"),
                        Args.style("style")));
        flat(
                functions,
                limits,
                "DRAW_RECTANGLE",
                call -> Shapes.rectangle(Args.point(call, "a"), Args.point(call, "b"), Args.point(call, "c")),
                Args.point("a"),
                Args.point("b"),
                Args.point("c"));
        flat(
                functions,
                limits,
                "DRAW_POLYGON",
                call -> Shapes.polygon(Args.points(call, "points")),
                NativeFunction.Parameter.required("points", "ARRAY STRUCT " + Args.POINT, Args.POINTS));
        flat(
                functions,
                limits,
                "DRAW_ELLIPSE",
                call -> Shapes.ellipse(Args.point(call, "center"), Args.point(call, "a"), Args.point(call, "b")),
                Args.point("center"),
                Args.point("a"),
                Args.point("b"));
        flat(
                functions,
                limits,
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
        return functions;
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
                        call -> {
                            Style[] styles = Painter.resolve(
                                    Args.style(call, "line_style"), null, Args.style(call, "fill_style"));
                            draw(call, limits, shape.apply(call), styles);
                            return NativeResult.none();
                        },
                        all.toArray(NativeFunction.Parameter[]::new)));
    }

    /** Draws the parts of a shape with the styles of line, side and fill (after the fallback of STYLE_NONE). */
    static void draw(NativeCall call, Supplier<Limits> limits, Shapes.Voxels voxels, Style[] styles) {
        List<Painter.Part> parts = new ArrayList<>();
        parts.add(new Painter.Part(voxels.inside(), styles[2]));
        if (styles[1] != null) {
            parts.add(new Painter.Part(voxels.faces(), styles[1]));
        }
        if (styles[0] != null) {
            parts.add(new Painter.Part(voxels.lines(), styles[0]));
        }
        Painter.draw(
                Args.canvasReference(call, "canvas"),
                limits.get(),
                call.process().random(),
                parts);
    }
}
