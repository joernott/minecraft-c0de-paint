package de.joernott.c0depaint.core.library;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.Value;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The drawing functions in programs (sprints 4 and 5). */
class DrawFunctionsTest {
    static final String POINTS = """
            VARIABLE a STRUCT c0de_paint/cpaint/POINT
            VARIABLE b STRUCT c0de_paint/cpaint/POINT
            ASSIGN a.x 9 ;
            ASSIGN b.z 4 ;
            """;

    private static long count(String body) {
        Map<String, Value> results =
                Programs.results("    RESULT count INTEGER\n", CanvasFunctionsTest.SETUP + POINTS + body + """
                        CALL c0de_paint/cpaint/CANVAS_COUNT
                            PARAMETER canvas c ;
                            RESULT result count
                        """);
        return ((IntegerValue) results.get("count")).value();
    }

    private static String rectangle(String styles) {
        return "CALL c0de_paint/cpaint/DRAW_RECTANGLE\n    PARAMETER canvas c ;\n    PARAMETER a p ;\n"
                + "    PARAMETER b a ;\n    PARAMETER c b ;\n" + styles;
    }

    @Test
    void stylesFallBackInward() {
        // Only a fill style: a full rectangle; only a line style: a frame
        assertEquals(50, count(rectangle("    PARAMETER fill_style solid ;\n")));
        assertEquals(26, count(rectangle("    PARAMETER line_style solid ;\n")));
        // An empty inside with a frame: the inside is air, so all voxels are set
        assertEquals(50, count(rectangle("    PARAMETER line_style solid ;\n    PARAMETER fill_style clear ;\n")));
        // No style at all draws nothing
        assertEquals(0, count(rectangle("")));
    }

    @Test
    void linesNeedAStyle() {
        assertEquals(10, count("""
                        CALL c0de_paint/cpaint/DRAW_LINE
                            PARAMETER canvas c ;
                            PARAMETER from p ;
                            PARAMETER to a ;
                            PARAMETER style solid ;
                        """));
    }

    @Test
    void pointsOnALineRaiseNotAPlane() {
        Programs.crashes(
                1102, CanvasFunctionsTest.SETUP + POINTS + """
                        ASSIGN b.x 20 ;
                        ASSIGN b.z 0 ;
                        """ + rectangle("    PARAMETER fill_style solid ;\n"));
    }

    @Test
    void ellipsesAndArcs() {
        // The pie slice of an ellipse with the semi-axes 4 (along z) and 9 (along x), with its arc
        var arc = de.joernott.c0depaint.core.draw.Shapes.arc(
                new de.joernott.c0depaint.core.canvas.Point(0, 0, 0),
                new de.joernott.c0depaint.core.canvas.Point(0, 0, 4),
                new de.joernott.c0depaint.core.canvas.Point(9, 0, 0),
                0,
                90);
        var all = new java.util.HashSet<>(arc.inside());
        all.addAll(arc.lines());
        assertEquals(all.size(), count("""
                        CALL c0de_paint/cpaint/DRAW_ARC
                            PARAMETER canvas c ;
                            PARAMETER center p ;
                            PARAMETER a b ;
                            PARAMETER b a ;
                            PARAMETER start 0 ;
                            PARAMETER end 90.0 ;
                            PARAMETER fill_style solid ;
                        """));
    }
}
