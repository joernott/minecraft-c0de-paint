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

    @Test
    void spirals() {
        // A spiral of 12 layers around the y axis: 12 voxels, one per layer, with the default elevation
        assertEquals(12, count("""
                        CALL c0de_paint/cpaint/DRAW_SPIRAL
                            PARAMETER canvas c ;
                            PARAMETER center p ;
                            PARAMETER a a ;
                            PARAMETER b b ;
                            PARAMETER direction "+y" ;
                            PARAMETER h 12 ;
                            PARAMETER line_style solid ;
                        """));
        // Every second voxel rises: 24 voxels
        assertEquals(24, count("""
                        CALL c0de_paint/cpaint/DRAW_SPIRAL
                            PARAMETER canvas c ;
                            PARAMETER center p ;
                            PARAMETER a a ;
                            PARAMETER b b ;
                            PARAMETER direction "+y" ;
                            PARAMETER elevation 0.5 ;
                            PARAMETER h 12 ;
                            PARAMETER line_style solid ;
                        """));
        Programs.crashes(309, CanvasFunctionsTest.SETUP + POINTS + """
                        CALL c0de_paint/cpaint/DRAW_SPIRAL
                            PARAMETER canvas c ;
                            PARAMETER center p ;
                            PARAMETER a a ;
                            PARAMETER b b ;
                            PARAMETER direction "+y" ;
                            PARAMETER elevation 0.0 ;
                            PARAMETER h 12 ;
                            PARAMETER line_style solid ;
                        """);
    }

    @Test
    void hollowBoxesAndSolids() {
        // A box with stone sides and an inside of air: all 150 voxels are set
        assertEquals(150, count("""
                        ASSIGN b.x 9 ;
                        ASSIGN b.y 4 ;
                        ASSIGN b.z 2 ;
                        CALL c0de_paint/cpaint/DRAW_BOX
                            PARAMETER canvas c ;
                            PARAMETER from p ;
                            PARAMETER to b ;
                            PARAMETER side_style solid ;
                            PARAMETER fill_style clear ;
                        """));
        long icosahedron = count("""
                CALL c0de_paint/cpaint/DRAW_ICOSAHEDRON
                    PARAMETER canvas c ;
                    PARAMETER center p ;
                    PARAMETER r 8 ;
                    PARAMETER line_style solid ;
                """);
        assertEquals(
                de.joernott.c0depaint.core.draw.Bodies.solid(
                                de.joernott.c0depaint.core.draw.Bodies.Solid.ICOSAHEDRON,
                                new de.joernott.c0depaint.core.canvas.Point(0, 0, 0),
                                8)
                        .lines()
                        .size(),
                icosahedron);
        Programs.crashes(309, CanvasFunctionsTest.SETUP + """
                        CALL c0de_paint/cpaint/DRAW_CONE
                            PARAMETER canvas c ;
                            PARAMETER center p ;
                            PARAMETER r 3 ;
                            PARAMETER h 4 ;
                            PARAMETER direction "up" ;
                            PARAMETER fill_style solid ;
                        """);
    }

    @Test
    void largeDrawingsAreSpreadOverSeveralTicks() {
        String program = Programs.program("", CanvasFunctionsTest.SETUP + """
                        ASSIGN p.x 19 ;
                        ASSIGN p.y 19 ;
                        ASSIGN p.z 19 ;
                        CALL c0de_paint/cpaint/DRAW_BOX
                            PARAMETER canvas c ;
                            PARAMETER from q ;
                            PARAMETER to p ;
                            PARAMETER fill_style solid ;
                        """);
        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("canvasVoxelsPerTick", "1024");
        de.joernott.c0depaint.core.config.PaintConfig saved = Programs.config;
        try {
            int small = steps(program, properties);
            int large = steps(program, new java.util.Properties());
            // The inside (8000 voxels), the faces (2168) and the edges (224) take the fill style: 10392 voxels in
            // parts of 1024, so the CALL line runs in 11 ticks instead of 1
            assertEquals(10, small - large);
        } finally {
            Programs.config = saved;
        }
    }

    private static int steps(String text, java.util.Properties properties) {
        Programs.config = de.joernott.c0depaint.core.config.PaintConfig.of(
                properties, de.joernott.c0depaint.core.config.PaintConfig.SERVER, "test");
        var cursor = new de.joernott.c0de.core.cursor.Cursor(
                de.joernott.c0de.core.program.ProgramParser.parse(de.joernott.c0de.core.line.Notation.parse(text)),
                "main",
                Map.of(),
                Programs.settings());
        int steps = 0;
        while (!cursor.ended() && steps < 100_000) {
            cursor.step();
            steps++;
        }
        assertEquals(de.joernott.c0de.core.cursor.CursorState.FINISHED, cursor.state(), () -> "" + cursor.error());
        return steps;
    }
}
