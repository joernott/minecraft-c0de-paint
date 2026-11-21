package de.joernott.c0depaint.core.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.Value;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The library and its types (task 2.3). */
class CpaintLibraryTest {
    @Test
    void newVariablesHoldAnEmptyCanvasAndStyleNone() {
        Map<String, Value> results = Programs.results("    RESULT canvas STRING\n    RESULT style STRING\n", """
                VARIABLE c TYPE c0de_paint/cpaint/CANVAS
                VARIABLE s TYPE c0de_paint/cpaint/STYLE
                ASSIGN canvas "" ⊕ c ;
                ASSIGN style "" ⊕ s ;
                """);
        assertEquals(new StringValue("CANVAS empty"), results.get("canvas"));
        assertEquals(new StringValue("STYLE NONE"), results.get("style"));
    }

    @Test
    void structsAreDeclared() {
        Map<String, Value> results = Programs.results("    RESULT sum INTEGER\n", """
                VARIABLE p STRUCT c0de_paint/cpaint/POINT
                VARIABLE b STRUCT c0de_paint/cpaint/BLOCK
                ASSIGN p.x 1 ;
                ASSIGN p.y 2 ;
                ASSIGN p.z 3 ;
                ASSIGN b.weight 4 ;
                ASSIGN sum p.x + p.y + p.z + b.weight ;
                """);
        assertEquals(new IntegerValue(10), results.get("sum"));
    }

    @Test
    void errorsAreConstants() {
        Map<String, Value> results =
                Programs.results("    RESULT limit INTEGER\n    RESULT empty INTEGER\n    RESULT plane INTEGER\n", """
                ASSIGN limit c0de_paint/cpaint/CANVAS_LIMIT ;
                ASSIGN empty c0de_paint/cpaint/CANVAS_EMPTY ;
                ASSIGN plane c0de_paint/cpaint/NOT_A_PLANE ;
                """);
        assertEquals(new IntegerValue(1100), results.get("limit"));
        assertEquals(new IntegerValue(1101), results.get("empty"));
        assertEquals(new IntegerValue(1102), results.get("plane"));
    }

    @Test
    void typesHaveTheirFullNames() {
        assertEquals("c0de_paint/cpaint/CANVAS", PaintTypes.CANVAS.name());
        assertEquals("c0de_paint/cpaint/STYLE", PaintTypes.STYLE.name());
        assertInstanceOf(CanvasValue.class, PaintTypes.CANVAS.defaultValue());
        assertTrue(new CpaintLibrary(() -> Programs.config)
                .functions().values().stream()
                        .allMatch(function -> !function.name().isEmpty()));
    }
}
