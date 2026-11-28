package de.joernott.c0depaint.core.library;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.schematic.Nbt;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** The functions of c0de_paint/schematics in programs (task 9.6). */
class SchematicsLibraryTest {
    @BeforeEach
    void folder() {
        Programs.SCHEMATICS.clear();
        Programs.SCHEMATICS.put("gate.nbt", gate());
        Programs.SCHEMATICS.put("castles/keep.nbt", gate());
    }

    @AfterEach
    void empty() {
        Programs.SCHEMATICS.clear();
    }

    private static Map<String, Object> compound(Object... keysAndValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }

    /** A gate of Create: two casings, a shaft, a door of two halves and a sign. */
    private static byte[] gate() {
        return Nbt.write(compound(
                "DataVersion", 3955,
                "size", List.of(3, 2, 1),
                "palette", List.of(
                        compound("Name", "create:andesite_casing"),
                        compound("Name", "create:shaft", "Properties", compound("axis", "x")),
                        compound("Name", "minecraft:oak_door", "Properties", compound("half", "lower", "facing", "south")),
                        compound("Name", "minecraft:oak_door", "Properties", compound("half", "upper", "facing", "south")),
                        compound("Name", "minecraft:oak_sign", "Properties", compound("rotation", "0"))),
                "blocks", List.of(
                        compound("pos", List.of(0, 0, 0), "state", 0),
                        compound("pos", List.of(2, 0, 0), "state", 0),
                        compound("pos", List.of(0, 1, 0), "state", 1),
                        compound("pos", List.of(1, 0, 0), "state", 2),
                        compound("pos", List.of(1, 1, 0), "state", 3),
                        compound("pos", List.of(2, 1, 0), "state", 4, "nbt", compound(
                                "id", "minecraft:sign", "is_waxed", (byte) 1))),
                "entities", List.of(compound("pos", List.of(0.5, 0.5, 0.5)))));
    }

    @Test
    void schematicsBecomeCanvasesWithReplacedBlocks() {
        Map<String, Value> results = Programs.results(
                "    RESULT count INTEGER\n    RESULT entities INTEGER\n    RESULT corner STRING\n"
                        + "    RESULT axis STRING\n    RESULT data STRING\n",
                """
                VARIABLE c TYPE c0de_paint/cpaint/CANVAS
                VARIABLE swap HASH STRING
                VARIABLE p STRUCT c0de_paint/cpaint/POINT
                VARIABLE b STRUCT c0de_paint/cpaint/BLOCK
                ASSIGN swap["create:andesite_casing"] "stripped_spruce_log" ;
                ASSIGN swap["create:shaft"] "oak_log" ;
                CALL c0de_paint/schematics/SCHEMATIC_LOAD
                    PARAMETER file "gate.nbt" ;
                    PARAMETER replace swap ;
                    RESULT result c
                    RESULT entities entities
                CALL c0de_paint/cpaint/CANVAS_COUNT
                    PARAMETER canvas c ;
                    RESULT result count
                CALL c0de_paint/cpaint/CANVAS_GET
                    PARAMETER canvas c ;
                    PARAMETER point p ;
                    RESULT result b
                ASSIGN corner b.id ;
                ASSIGN p.y 1 ;
                CALL c0de_paint/cpaint/CANVAS_GET
                    PARAMETER canvas c ;
                    PARAMETER point p ;
                    RESULT result b
                ASSIGN axis b.properties["axis"] ;
                ASSIGN p.x 2 ;
                CALL c0de_paint/cpaint/CANVAS_GET
                    PARAMETER canvas c ;
                    PARAMETER point p ;
                    RESULT result b
                ASSIGN data b.data ;
                """);
        assertEquals(new IntegerValue(6), results.get("count"));
        assertEquals(new IntegerValue(1), results.get("entities"));
        assertEquals(new StringValue("minecraft:stripped_spruce_log"), results.get("corner"));
        // The log takes the axis of the shaft
        assertEquals(new StringValue("x"), results.get("axis"));
        assertEquals(new StringValue("{is_waxed:1b}"), results.get("data"));
    }

    @Test
    void materialsAndMissingBlocksAreCounted() {
        Map<String, Value> results = Programs.results(
                "    RESULT items HASH INTEGER\n    RESULT without HASH INTEGER\n    RESULT missing HASH INTEGER\n"
                        + "    RESULT replaced HASH INTEGER\n",
                """
                VARIABLE swap HASH STRING
                CALL c0de_paint/schematics/SCHEMATIC_MATERIALS
                    PARAMETER file "castles/keep.nbt" ;
                    RESULT result items
                    RESULT without_item without
                CALL c0de_paint/schematics/SCHEMATIC_MISSING
                    PARAMETER file "gate.nbt" ;
                    RESULT result missing
                ASSIGN swap["create:andesite_casing"] "stone" ;
                ASSIGN swap["create:shaft"] "" ;
                CALL c0de_paint/schematics/SCHEMATIC_MISSING
                    PARAMETER file "gate.nbt" ;
                    PARAMETER replace swap ;
                    RESULT result replaced
                """);
        // Without Minecraft every block is its own item; the server of the mod counts doors once (task 9.3)
        HashValue items = (HashValue) results.get("items");
        assertEquals(new IntegerValue(2), items.elements().get("create:andesite_casing"));
        assertEquals(new IntegerValue(2), items.elements().get("minecraft:oak_door"));
        assertEquals(0, ((HashValue) results.get("without")).elements().size());
        HashValue missing = (HashValue) results.get("missing");
        assertEquals(new IntegerValue(2), missing.elements().get("create:andesite_casing"));
        assertEquals(new IntegerValue(1), missing.elements().get("create:shaft"));
        assertEquals(0, ((HashValue) results.get("replaced")).elements().size());
    }

    @Test
    void theFolderIsListed() {
        Map<String, Value> results = Programs.results(
                "    RESULT all ARRAY STRING\n    RESULT castles ARRAY STRING\n",
                """
                CALL c0de_paint/schematics/SCHEMATIC_LIST
                    RESULT result all
                CALL c0de_paint/schematics/SCHEMATIC_LIST
                    PARAMETER folder "castles" ;
                    RESULT result castles
                """);
        assertEquals(
                List.of(new StringValue("castles/keep.nbt"), new StringValue("gate.nbt")),
                ((ArrayValue) results.get("all")).elements());
        assertEquals(List.of(new StringValue("castles/keep.nbt")), ((ArrayValue) results.get("castles")).elements());
    }

    @Test
    void errorsHaveTheirCodes() {
        Programs.crashes(PaintError.SCHEMATIC_NOT_FOUND.code(), """
                CALL c0de_paint/schematics/SCHEMATIC_MISSING
                    PARAMETER file "nothing.nbt" ;
                """);
        Programs.crashes(PaintError.INVALID_ARGUMENT.code(), """
                CALL c0de_paint/schematics/SCHEMATIC_MISSING
                    PARAMETER file "../secret.nbt" ;
                """);
        Programs.crashes(PaintError.INVALID_ARGUMENT.code(), """
                CALL c0de_paint/schematics/SCHEMATIC_MISSING
                    PARAMETER file "notes.txt" ;
                """);
        Programs.SCHEMATICS.put("broken.nbt", new byte[] {1, 2, 3});
        Programs.crashes(PaintError.SCHEMATIC_INVALID.code(), """
                CALL c0de_paint/schematics/SCHEMATIC_MISSING
                    PARAMETER file "broken.nbt" ;
                """);
        Programs.crashes(PaintError.SCHEMATIC_INVALID.code(), """
                VARIABLE c TYPE c0de_paint/cpaint/CANVAS
                CALL c0de_paint/schematics/SCHEMATIC_LOAD
                    PARAMETER file "gate.nbt" ;
                    PARAMETER region "tower" ;
                    RESULT result c
                """);
    }

    @Test
    void schematicsLargerThanACanvasAreRefused() {
        Programs.SCHEMATICS.put("huge.nbt", Nbt.write(compound(
                "DataVersion", 3955,
                "size", List.of(300, 1, 1),
                "palette", List.of(compound("Name", "minecraft:stone")),
                "blocks", List.of(compound("pos", List.of(299, 0, 0), "state", 0)))));
        Programs.crashes(PaintError.SCHEMATIC_TOO_LARGE.code(), """
                VARIABLE c TYPE c0de_paint/cpaint/CANVAS
                CALL c0de_paint/schematics/SCHEMATIC_LOAD
                    PARAMETER file "huge.nbt" ;
                    RESULT result c
                """);
        // The lists work for schematics of any size
        Map<String, Value> results = Programs.results(
                "    RESULT missing HASH INTEGER\n",
                """
                CALL c0de_paint/schematics/SCHEMATIC_MISSING
                    PARAMETER file "huge.nbt" ;
                    RESULT result missing
                """);
        assertEquals(0, ((HashValue) results.get("missing")).elements().size());
    }
}
