package de.joernott.c0depaint.core.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.cursor.Cursor;
import de.joernott.c0de.core.cursor.CursorSnapshot;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.ProgramParser;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.Value;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** DRAW_BLOCK, the styles and the canvas operations in programs (tasks 3.1 to 3.6). */
class CanvasFunctionsTest {
    /** Declarations of the tests: a canvas, points, blocks and styles. */
    static final String SETUP = """
            VARIABLE c TYPE c0de_paint/cpaint/CANVAS
            VARIABLE p STRUCT c0de_paint/cpaint/POINT
            VARIABLE q STRUCT c0de_paint/cpaint/POINT
            VARIABLE stone STRUCT c0de_paint/cpaint/BLOCK
            VARIABLE glass STRUCT c0de_paint/cpaint/BLOCK
            VARIABLE solid TYPE c0de_paint/cpaint/STYLE
            VARIABLE clear TYPE c0de_paint/cpaint/STYLE
            ASSIGN stone.id "minecraft:stone" ;
            ASSIGN stone.weight 1 ;
            ASSIGN glass.id "glass" ;
            ASSIGN glass.weight 1 ;
            CALL c0de_paint/cpaint/STYLE_SOLID
                PARAMETER block stone ;
                RESULT result solid
            CALL c0de_paint/cpaint/STYLE_EMPTY
                RESULT result clear
            """;

    /** Draws a block of the style at x, y, z. */
    static String draw(int x, int y, int z, String style) {
        return "ASSIGN p.x " + x + " ;\nASSIGN p.y " + y + " ;\nASSIGN p.z " + z + " ;\n"
                + "CALL c0de_paint/cpaint/DRAW_BLOCK\n    PARAMETER canvas c ;\n    PARAMETER point p ;\n"
                + "    PARAMETER style " + style + " ;\n";
    }

    private static Map<String, Value> run(String results, String body) {
        return Programs.results(results, SETUP + body);
    }

    @Test
    void drawnBlocksAreCountedAndRead() {
        Map<String, Value> results = run(
                "    RESULT count INTEGER\n    RESULT id STRING\n    RESULT unset STRING\n    RESULT text STRING\n",
                draw(1, 2, 3, "solid") + draw(4, 2, 0, "clear") + draw(1, 2, 3, "solid") + """
                        CALL c0de_paint/cpaint/CANVAS_COUNT
                            PARAMETER canvas c ;
                            RESULT result count
                        VARIABLE b STRUCT c0de_paint/cpaint/BLOCK
                        ASSIGN p.x 1 ;
                        ASSIGN p.z 3 ;
                        CALL c0de_paint/cpaint/CANVAS_GET
                            PARAMETER canvas c ;
                            PARAMETER point p ;
                            RESULT result b
                        ASSIGN id b.id ;
                        ASSIGN p.x 0 ;
                        CALL c0de_paint/cpaint/CANVAS_GET
                            PARAMETER canvas c ;
                            PARAMETER point p ;
                            RESULT result b
                        ASSIGN unset b.id ;
                        ASSIGN text "" ⊕ c ;
                        """);
        assertEquals(new IntegerValue(2), results.get("count"));
        assertEquals(new StringValue("minecraft:stone"), results.get("id"));
        assertEquals(new StringValue(""), results.get("unset"));
        assertEquals(new StringValue("CANVAS 4×1×4, 2 voxels"), results.get("text"));
    }

    @Test
    void boundsOfACanvas() {
        Map<String, Value> results = run(
                "    RESULT low STRUCT c0de_paint/cpaint/POINT\n    RESULT high STRUCT c0de_paint/cpaint/POINT\n",
                draw(-5, 2, 7, "solid") + draw(3, -1, 0, "solid") + """
                        CALL c0de_paint/cpaint/CANVAS_MIN
                            PARAMETER canvas c ;
                            RESULT result low
                        CALL c0de_paint/cpaint/CANVAS_MAX
                            PARAMETER canvas c ;
                            RESULT result high
                        """);
        assertEquals(Args.pointValue(new de.joernott.c0depaint.core.canvas.Point(-5, -1, 0)), results.get("low"));
        assertEquals(Args.pointValue(new de.joernott.c0depaint.core.canvas.Point(3, 2, 7)), results.get("high"));
    }

    @Test
    void anEmptyCanvasHasNoBounds() {
        Programs.crashes(1101, SETUP + """
                        CALL c0de_paint/cpaint/CANVAS_MIN
                            PARAMETER canvas c ;
                            RESULT result p
                        """);
    }

    @Test
    void voxelsOutsideTheLimitsDrawNothing() {
        Programs.crashes(1100, SETUP + draw(128, 0, 0, "solid"));
        Map<String, Value> results = run("    RESULT count INTEGER\n    RESULT code INTEGER\n", """
                VARIABLE problem ERROR
                ASSIGN p.y -129 ;
                CALL c0de_paint/cpaint/DRAW_BLOCK problem
                    PARAMETER canvas c ;
                    PARAMETER point p ;
                    PARAMETER style solid ;
                ASSIGN code problem.code ;
                CALL c0de_paint/cpaint/CANVAS_COUNT
                    PARAMETER canvas c ;
                    RESULT result count
                """);
        assertEquals(new IntegerValue(0), results.get("count"));
        assertEquals(new IntegerValue(1100), results.get("code"));
    }

    @Test
    void weightsBelowOneAreInvalid() {
        Programs.crashes(309, SETUP + """
                        VARIABLE list ARRAY STRUCT c0de_paint/cpaint/BLOCK
                        ASSIGN stone.weight 0 ;
                        ASSIGN list[0] stone ;
                        CALL c0de_paint/cpaint/STYLE_RANDOM
                            PARAMETER blocks list ;
                            RESULT result solid
                        """);
    }

    @Test
    void clearCropMoveAndReplace() {
        String body = draw(0, 0, 0, "solid") + draw(5, 0, 0, "solid") + draw(10, 0, 0, "solid") + """
                ASSIGN p.x 4 ;
                ASSIGN q.x 6 ;
                ASSIGN q.y 0 ;
                ASSIGN q.z 0 ;
                CALL c0de_paint/cpaint/CANVAS_CLEAR
                    PARAMETER canvas c ;
                    PARAMETER from p ;
                    PARAMETER to q ;
                CALL c0de_paint/cpaint/CANVAS_COUNT
                    PARAMETER canvas c ;
                    RESULT result afterClear
                ASSIGN p.x 0 ;
                ASSIGN q.x 9 ;
                CALL c0de_paint/cpaint/CANVAS_CROP
                    PARAMETER canvas c ;
                    PARAMETER from p ;
                    PARAMETER to q ;
                CALL c0de_paint/cpaint/CANVAS_COUNT
                    PARAMETER canvas c ;
                    RESULT result afterCrop
                ASSIGN q.x 3 ;
                ASSIGN q.y -2 ;
                CALL c0de_paint/cpaint/CANVAS_MOVE
                    PARAMETER canvas c ;
                    PARAMETER offset q ;
                CALL c0de_paint/cpaint/CANVAS_MIN
                    PARAMETER canvas c ;
                    RESULT result low
                VARIABLE glassy TYPE c0de_paint/cpaint/STYLE
                CALL c0de_paint/cpaint/STYLE_SOLID
                    PARAMETER block glass ;
                    RESULT result glassy
                CALL c0de_paint/cpaint/CANVAS_REPLACE
                    PARAMETER canvas c ;
                    PARAMETER id "stone" ;
                    PARAMETER style glassy ;
                VARIABLE b STRUCT c0de_paint/cpaint/BLOCK
                CALL c0de_paint/cpaint/CANVAS_GET
                    PARAMETER canvas c ;
                    PARAMETER point low ;
                    RESULT result b
                ASSIGN id b.id ;
                CALL c0de_paint/cpaint/CANVAS_CLEAR
                    PARAMETER canvas c ;
                CALL c0de_paint/cpaint/CANVAS_COUNT
                    PARAMETER canvas c ;
                    RESULT result afterAll
                """;
        Map<String, Value> results = run(
                "    RESULT afterClear INTEGER\n    RESULT afterCrop INTEGER\n    RESULT low STRUCT c0de_paint/cpaint/POINT\n"
                        + "    RESULT id STRING\n    RESULT afterAll INTEGER\n",
                body);
        assertEquals(new IntegerValue(2), results.get("afterClear"));
        assertEquals(new IntegerValue(1), results.get("afterCrop"));
        assertEquals(Args.pointValue(new de.joernott.c0depaint.core.canvas.Point(3, -2, 0)), results.get("low"));
        assertEquals(new StringValue("minecraft:glass"), results.get("id"));
        assertEquals(new IntegerValue(0), results.get("afterAll"));
    }

    @Test
    void mergeOverwritesOrKeeps() {
        String body = draw(0, 0, 0, "solid") + draw(1, 0, 0, "solid") + """
                VARIABLE other TYPE c0de_paint/cpaint/CANVAS
                VARIABLE glassy TYPE c0de_paint/cpaint/STYLE
                CALL c0de_paint/cpaint/STYLE_SOLID
                    PARAMETER block glass ;
                    RESULT result glassy
                ASSIGN p.x 1 ;
                CALL c0de_paint/cpaint/DRAW_BLOCK
                    PARAMETER canvas other ;
                    PARAMETER point p ;
                    PARAMETER style glassy ;
                ASSIGN p.x 2 ;
                CALL c0de_paint/cpaint/DRAW_BLOCK
                    PARAMETER canvas other ;
                    PARAMETER point p ;
                    PARAMETER style clear ;
                VARIABLE kept TYPE c0de_paint/cpaint/CANVAS
                ASSIGN kept c ;
                CALL c0de_paint/cpaint/CANVAS_MERGE
                    PARAMETER target c ;
                    PARAMETER source other ;
                CALL c0de_paint/cpaint/CANVAS_MERGE
                    PARAMETER target kept ;
                    PARAMETER source other ;
                    PARAMETER mode "KEEP" ;
                VARIABLE b STRUCT c0de_paint/cpaint/BLOCK
                ASSIGN p.x 1 ;
                CALL c0de_paint/cpaint/CANVAS_GET
                    PARAMETER canvas c ;
                    PARAMETER point p ;
                    RESULT result b
                ASSIGN overwritten b.id ;
                CALL c0de_paint/cpaint/CANVAS_GET
                    PARAMETER canvas kept ;
                    PARAMETER point p ;
                    RESULT result b
                ASSIGN keep b.id ;
                CALL c0de_paint/cpaint/CANVAS_COUNT
                    PARAMETER canvas kept ;
                    RESULT result count
                VARIABLE parts ARRAY TYPE c0de_paint/cpaint/CANVAS
                ASSIGN parts[0] other ;
                ASSIGN parts[1] other ;
                ASSIGN q.x 10 ;
                ASSIGN q.y 0 ;
                ASSIGN q.z 0 ;
                CALL c0de_paint/cpaint/CANVAS_MERGE
                    PARAMETER target kept ;
                    PARAMETER source parts ;
                    PARAMETER offset q ;
                CALL c0de_paint/cpaint/CANVAS_MATERIALS
                    PARAMETER canvas kept ;
                    RESULT result materials
                """;
        Map<String, Value> results = run(
                "    RESULT overwritten STRING\n    RESULT keep STRING\n    RESULT count INTEGER\n"
                        + "    RESULT materials HASH INTEGER\n",
                body);
        assertEquals(new StringValue("minecraft:glass"), results.get("overwritten"));
        assertEquals(new StringValue("minecraft:stone"), results.get("keep"));
        assertEquals(new IntegerValue(3), results.get("count"));
        HashValue materials = (HashValue) results.get("materials");
        assertEquals(new IntegerValue(2), materials.elements().get("minecraft:stone"));
        assertEquals(new IntegerValue(1), materials.elements().get("minecraft:glass"));
        assertTrue(!materials.elements().containsKey("minecraft:air"));
    }

    @Test
    void assignmentsCopyCanvases() {
        Map<String, Value> results = run(
                "    RESULT first INTEGER\n    RESULT second INTEGER\n",
                draw(0, 0, 0, "solid") + """
                        VARIABLE copy TYPE c0de_paint/cpaint/CANVAS
                        ASSIGN copy c ;
                        """ + draw(1, 0, 0, "solid") + """
                        CALL c0de_paint/cpaint/CANVAS_COUNT
                            PARAMETER canvas c ;
                            RESULT result first
                        CALL c0de_paint/cpaint/CANVAS_COUNT
                            PARAMETER canvas copy ;
                            RESULT result second
                        """);
        assertEquals(new IntegerValue(2), results.get("first"));
        assertEquals(new IntegerValue(1), results.get("second"));
    }

    @Test
    void gradientsAndPatternsInPrograms() {
        Map<String, Value> results =
                run("    RESULT id STRING\n", """
                VARIABLE list ARRAY STRUCT c0de_paint/cpaint/BLOCK
                ASSIGN list[0] stone ;
                ASSIGN list[1] glass ;
                VARIABLE fade TYPE c0de_paint/cpaint/STYLE
                CALL c0de_paint/cpaint/STYLE_GRADIENT
                    PARAMETER blocks list ;
                    PARAMETER from p ;
                    PARAMETER axes "x" ;
                    RESULT result fade
                VARIABLE tiles TYPE c0de_paint/cpaint/STYLE
                """ + draw(0, 0, 0, "solid") + """
                        CALL c0de_paint/cpaint/STYLE_PATTERN
                            PARAMETER pattern c ;
                            RESULT result tiles
                        """ + draw(7, 0, 0, "tiles") + """
                        VARIABLE b STRUCT c0de_paint/cpaint/BLOCK
                        CALL c0de_paint/cpaint/CANVAS_GET
                            PARAMETER canvas c ;
                            PARAMETER point p ;
                            RESULT result b
                        ASSIGN id b.id ;
                        """);
        assertEquals(new StringValue("minecraft:stone"), results.get("id"));
    }

    @Test
    void canvasesAndStylesSurviveSavingTheCursor() {
        String text = Programs.program(
                "    RESULT count INTEGER\n    RESULT style STRING\n",
                SETUP + draw(1, 2, 3, "solid") + draw(4, 5, 6, "clear") + """
                        NOOP
                        NOOP
                        NOOP
                        CALL c0de_paint/cpaint/CANVAS_COUNT
                            PARAMETER canvas c ;
                            RESULT result count
                        ASSIGN style "" ⊕ solid ;
                        """);
        Program program = ProgramParser.parse(Notation.parse(text));
        Cursor cursor = new Cursor(program, "main", Map.of(), Programs.settings());
        // Stops after the drawing, before CANVAS_COUNT
        int steps = 0;
        while (!cursor.ended()
                && steps < 10_000
                && cursor.line() < text.lines().toList().indexOf("    NOOP")) {
            cursor.step();
            steps++;
        }
        assertTrue(!cursor.ended() && steps > 0, "The program should still run");
        Map<String, Object> saved = CursorSnapshot.save(cursor);
        Cursor restored = CursorSnapshot.restore(saved, program, Programs.settings());
        restored.run(10_000);
        assertEquals(new IntegerValue(2), restored.results().get("count"), () -> "error: " + restored.error());
        assertEquals(
                new StringValue("STYLE SOLID minecraft:stone"),
                restored.results().get("style"));
    }
}
