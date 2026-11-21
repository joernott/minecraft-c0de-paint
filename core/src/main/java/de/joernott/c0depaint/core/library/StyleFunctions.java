package de.joernott.c0depaint.core.library;

import static de.joernott.c0depaint.core.library.Args.function;

import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.PaintException;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Point;
import de.joernott.c0depaint.core.style.Style;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The functions that create styles (docs/concept.md#styles). c0de has no constants of library types, so STYLE_NONE
 * and STYLE_EMPTY are functions too.
 */
final class StyleFunctions {
    private static final List<String> RESULT = List.of("result");

    private StyleFunctions() {}

    static Map<String, NativeFunction> functions() {
        Map<String, NativeFunction> functions = new LinkedHashMap<>();
        add(functions, function("STYLE_NONE", RESULT, call -> style(Style.NONE)));
        add(functions, function("STYLE_EMPTY", RESULT, call -> style(Style.EMPTY)));
        add(
                functions,
                function(
                        "STYLE_SOLID",
                        RESULT,
                        call -> style(new Style.Solid(Args.block(call, "block"))),
                        Args.block("block")));
        add(
                functions,
                function(
                        "STYLE_RANDOM",
                        RESULT,
                        call -> style(new Style.Random(Args.blocks(call, "blocks"))),
                        Args.blocks("blocks")));
        add(
                functions,
                function(
                        "STYLE_GRADIENT",
                        RESULT,
                        call -> style(new Style.Gradient(
                                Args.blocks(call, "blocks"),
                                Args.point(call, "from"),
                                Args.point(call, "to"),
                                ((de.joernott.c0de.core.value.StringValue) call.argument("axes")).value(),
                                Math.toIntExact(Math.clamp(
                                        ((de.joernott.c0de.core.value.IntegerValue) call.argument("fuzzy")).value(),
                                        -1,
                                        4)))),
                        Args.blocks("blocks"),
                        Args.point("from"),
                        Args.optionalPoint("to"),
                        Args.text("axes", "xyz"),
                        Args.integer("fuzzy", 0)));
        add(
                functions,
                function(
                        "STYLE_PATTERN",
                        RESULT,
                        call -> {
                            Canvas pattern = Args.canvas(call, "pattern");
                            if (pattern.isEmpty()) {
                                throw new PaintException(PaintError.CANVAS_EMPTY, "The pattern is empty");
                            }
                            return style(new Style.Pattern(pattern, Args.point(call, "from", Point.ORIGIN)));
                        },
                        Args.canvas("pattern"),
                        Args.optionalPoint("from")));
        return functions;
    }

    private static void add(Map<String, NativeFunction> functions, NativeFunction function) {
        functions.put(function.name(), function);
    }

    private static NativeResult style(Style style) {
        return NativeResult.of(new StyleValue(style));
    }
}
