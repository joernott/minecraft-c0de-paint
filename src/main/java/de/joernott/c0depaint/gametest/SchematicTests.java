package de.joernott.c0depaint.gametest;

import de.joernott.c0de.core.cursor.Cursor;
import de.joernott.c0de.core.cursor.CursorState;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0depaint.core.PaintError;
import de.joernott.c0depaint.core.schematic.Nbt;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;

/** GameTests of c0de_paint/schematics with Minecraft: the data fixer, the items, the clients (tasks 9.3 to 9.6). */
public final class SchematicTests {
    private SchematicTests() {}

    private static Map<String, Object> compound(Object... keysAndValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }

    /**
     * A structure of Minecraft 1.16.5 (data version 2586), as Create would have saved it: grass (renamed to short_grass
     * in 1.20.3), a sign with the text of 1.16, a double slab, a door, water, and a shaft of Create, which this server
     * doesn't have.
     */
    private static byte[] oldStructure() {
        return Nbt.write(compound(
                "DataVersion", 2586,
                "size", List.of(6, 2, 1),
                "palette", List.of(
                        compound("Name", "minecraft:grass"),
                        compound("Name", "minecraft:oak_sign", "Properties", compound("rotation", "0", "waterlogged", "false")),
                        compound("Name", "minecraft:oak_slab", "Properties", compound("type", "double", "waterlogged", "false")),
                        compound("Name", "minecraft:oak_door", "Properties", compound(
                                "half", "lower", "facing", "north", "hinge", "left", "open", "false", "powered", "false")),
                        compound("Name", "minecraft:oak_door", "Properties", compound(
                                "half", "upper", "facing", "north", "hinge", "left", "open", "false", "powered", "false")),
                        compound("Name", "minecraft:water", "Properties", compound("level", "0")),
                        compound("Name", "create:shaft", "Properties", compound("axis", "y"))),
                "blocks", List.of(
                        compound("pos", List.of(0, 0, 0), "state", 0),
                        compound("pos", List.of(1, 0, 0), "state", 1, "nbt", compound(
                                "id", "minecraft:sign",
                                "Text1", "{\"text\":\"Hi\"}",
                                "Text2", "{\"text\":\"\"}",
                                "Text3", "{\"text\":\"\"}",
                                "Text4", "{\"text\":\"\"}",
                                "Color", "black")),
                        compound("pos", List.of(2, 0, 0), "state", 2),
                        compound("pos", List.of(3, 0, 0), "state", 3),
                        compound("pos", List.of(3, 1, 0), "state", 4),
                        compound("pos", List.of(4, 0, 0), "state", 5),
                        compound("pos", List.of(5, 0, 0), "state", 6)),
                "entities", List.of()));
    }

    private static Path folder() {
        try {
            Path folder = Files.createTempDirectory("c0de-paint-schematics");
            Files.createDirectories(folder.resolve("old"));
            Files.write(folder.resolve("old/garden.nbt"), oldStructure());
            return folder;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final String PROGRAM = """
            PROGRAM schematics
            NOOP
            INCLUDE c0de_paint/cpaint
            INCLUDE c0de_paint/schematics
            NOOP
            FUNCTION main
                RESULT names ARRAY STRING
                RESULT grass STRING
                RESULT sign STRING
                RESULT items HASH INTEGER
                RESULT missing HASH INTEGER
            BEGIN
                VARIABLE c TYPE c0de_paint/cpaint/CANVAS
                VARIABLE p STRUCT c0de_paint/cpaint/POINT
                VARIABLE b STRUCT c0de_paint/cpaint/BLOCK
                CALL c0de_paint/schematics/SCHEMATIC_LIST
                    RESULT result names
                CALL c0de_paint/schematics/SCHEMATIC_LOAD
                    PARAMETER file "old/garden.nbt" ;
                    RESULT result c
                CALL c0de_paint/cpaint/CANVAS_GET
                    PARAMETER canvas c ;
                    PARAMETER point p ;
                    RESULT result b
                ASSIGN grass b.id ;
                ASSIGN p.x 1 ;
                CALL c0de_paint/cpaint/CANVAS_GET
                    PARAMETER canvas c ;
                    PARAMETER point p ;
                    RESULT result b
                ASSIGN sign b.data ;
                CALL c0de_paint/schematics/SCHEMATIC_MATERIALS
                    PARAMETER file "old/garden.nbt" ;
                    RESULT result items
                CALL c0de_paint/schematics/SCHEMATIC_MISSING
                    PARAMETER file "old/garden.nbt" ;
                    RESULT result missing
            END FUNCTION
            """;

    /**
     * A schematic of an older Minecraft comes from the client of the player, is upgraded by the data fixer and lists
     * the items that build it and the blocks the server lacks.
     */
    public static void schematicsAreReadUpgradedAndCounted(GameTestHelper helper) {
        LocalSchematics.install();
        UUID player = UUID.randomUUID();
        LocalSchematics.add(player, folder());
        Cursor cursor = Processes.cursor(helper, PROGRAM, Map.of(), player);
        Processes.finish(helper, cursor, () -> {
            LocalSchematics.remove(player);
            helper.assertTrue(
                    cursor.state() == CursorState.FINISHED, "The program didn't finish: " + cursor.error());
            Map<String, Value> results = cursor.results();
            helper.assertTrue(
                    ((ArrayValue) results.get("names")).elements().equals(List.of(new StringValue("old/garden.nbt"))),
                    "The list is " + results.get("names"));
            helper.assertTrue(
                    results.get("grass").equals(new StringValue("minecraft:short_grass")),
                    "The grass of 1.16 is " + results.get("grass"));
            helper.assertTrue(
                    ((StringValue) results.get("sign")).value().contains("front_text"),
                    "The sign of 1.16 has the data " + results.get("sign"));
            Map<String, Value> items = ((HashValue) results.get("items")).elements();
            helper.assertTrue(new IntegerValue(2).equals(items.get("minecraft:oak_slab")), "items: " + items);
            helper.assertTrue(new IntegerValue(1).equals(items.get("minecraft:oak_door")), "items: " + items);
            helper.assertTrue(new IntegerValue(1).equals(items.get("minecraft:water_bucket")), "items: " + items);
            helper.assertTrue(new IntegerValue(1).equals(items.get("minecraft:oak_sign")), "items: " + items);
            Map<String, Value> missing = ((HashValue) results.get("missing")).elements();
            helper.assertTrue(
                    missing.size() == 1 && new IntegerValue(1).equals(missing.get("create:shaft")),
                    "missing: " + missing);
            helper.succeed();
        });
    }

    /** A process whose player isn't online can't read schematics. */
    public static void schematicsNeedThePlayerOnline(GameTestHelper helper) {
        LocalSchematics.install();
        Cursor cursor = Processes.cursor(helper, PROGRAM, Map.of(), UUID.randomUUID());
        Processes.finish(helper, cursor, () -> {
            helper.assertTrue(cursor.state() == CursorState.CRASHED, "The program didn't crash");
            helper.assertTrue(
                    new IntegerValue(PaintError.CLIENT_UNAVAILABLE.code()).equals(cursor.error().get("code")),
                    "The error is " + cursor.error());
            helper.succeed();
        });
    }
}
