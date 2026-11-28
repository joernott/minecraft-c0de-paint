package de.joernott.c0depaint.gametest;

import de.joernott.c0de.core.cursor.Cursor;
import de.joernott.c0de.core.cursor.CursorState;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.Value;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** RENDER_CANVAS and CANVAS_FROM_WORLD (tasks 7.2 to 7.4). */
public final class RenderTests {
    private RenderTests() {}

    /**
     * A program that draws a box of planks from 0,0,0 to 2,0,2 and renders it at {@code at} with the inventories; it
     * returns the code of the error of RENDER_CANVAS in {@code code}.
     */
    private static String program(String inventories) {
        return """
                PROGRAM rendering
                NOOP
                INCLUDE c0de/minecraft
                INCLUDE c0de_paint/cpaint
                NOOP
                FUNCTION main
                    PARAMETER at STRUCT c0de/minecraft/POSITION
                    PARAMETER input STRUCT c0de/minecraft/POSITION
                    PARAMETER tools STRUCT c0de/minecraft/POSITION
                    PARAMETER output STRUCT c0de/minecraft/POSITION
                    RESULT code INTEGER
                    RESULT message STRING
                BEGIN
                    VARIABLE c TYPE c0de_paint/cpaint/CANVAS
                    VARIABLE a STRUCT c0de_paint/cpaint/POINT
                    VARIABLE b STRUCT c0de_paint/cpaint/POINT
                    VARIABLE planks STRUCT c0de_paint/cpaint/BLOCK
                    VARIABLE s TYPE c0de_paint/cpaint/STYLE
                    VARIABLE problem ERROR
                    ASSIGN b.x 2 ;
                    ASSIGN b.z 2 ;
                    ASSIGN planks.id "oak_planks" ;
                    CALL c0de_paint/cpaint/STYLE_SOLID
                        PARAMETER block planks ;
                        RESULT result s
                    CALL c0de_paint/cpaint/DRAW_BOX
                        PARAMETER canvas c ;
                        PARAMETER from a ;
                        PARAMETER to b ;
                        PARAMETER fill_style s ;
                    CALL c0de_paint/cpaint/RENDER_CANVAS problem
                        PARAMETER canvas c ;
                        PARAMETER position at ;
                """
                + inventories
                + """
                    ASSIGN code problem.code ;
                    ASSIGN message problem.message ;
                END FUNCTION
                """;
    }

    private static final String WITH_INPUT = "        PARAMETER input input ;\n";
    private static final String WITH_ALL =
            "        PARAMETER input input ;\n        PARAMETER tools tools ;\n        PARAMETER output output ;\n";

    private static Container chest(GameTestHelper helper, BlockPos pos, ItemStack... stacks) {
        helper.setBlock(pos, Blocks.CHEST);
        Container chest = (Container) TestCompat.blockEntity(helper, pos);
        for (int i = 0; i < stacks.length; i++) {
            chest.setItem(i, stacks[i]);
        }
        return chest;
    }

    private static Map<String, Value> inputs(GameTestHelper helper) {
        return Map.of(
                "at", Processes.position(helper.absolutePos(new BlockPos(0, 3, 0))),
                "input", Processes.position(helper.absolutePos(new BlockPos(0, 1, 0))),
                "tools", Processes.position(helper.absolutePos(new BlockPos(1, 1, 0))),
                "output", Processes.position(helper.absolutePos(new BlockPos(2, 1, 0))));
    }

    private static long code(Cursor cursor) {
        return ((IntegerValue) cursor.results().get("code")).value();
    }

    private static void clear(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(0, 1, 0), new BlockPos(2, 3, 2))) {
            helper.setBlock(pos, Blocks.AIR);
        }
    }

    /** Without an input inventory, an owner who isn't an administrator needs allowBlockChanges (off by default). */
    public static void renderingWithoutInputNeedsRights(GameTestHelper helper) {
        Cursor cursor = Processes.cursor(helper, program(""), inputs(helper));
        Processes.finish(helper, cursor, () -> {
            helper.assertTrue(cursor.state() == CursorState.FINISHED, "The program crashed: " + cursor.error());
            helper.assertTrue(code(cursor) == 311, "Expected PERMISSION_DENIED, got " + cursor.results());
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(1, 3, 1));
            helper.succeed();
        });
    }

    /** The blocks come from the input; with too few, nothing is placed (ITEM_MISSING). */
    public static void renderingTakesTheBlocksFromTheInput(GameTestHelper helper) {
        clear(helper);
        Container input = chest(helper, new BlockPos(0, 1, 0), new ItemStack(Items.OAK_PLANKS, 8));
        Cursor missing = Processes.cursor(helper, program(WITH_INPUT), inputs(helper));
        Processes.finish(helper, missing, () -> {
            helper.assertTrue(code(missing) == 313, "Expected ITEM_MISSING, got " + missing.results());
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(0, 3, 0));
            input.setItem(1, new ItemStack(Items.OAK_PLANKS, 1));
            Cursor cursor = Processes.cursor(helper, program(WITH_INPUT), inputs(helper));
            Processes.finish(helper, cursor, () -> {
                helper.assertTrue(code(cursor) == 0, "The rendering failed: " + cursor.results());
                for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(0, 3, 0), new BlockPos(2, 3, 2))) {
                    helper.assertBlockPresent(Blocks.OAK_PLANKS, pos.immutable());
                }
                helper.assertTrue(input.isEmpty(), "The input still has items");
                helper.succeed();
            });
        });
    }

    private static final String CONTENTS_PROGRAM = """
            PROGRAM barrel
            NOOP
            INCLUDE c0de/minecraft
            INCLUDE c0de_paint/cpaint
            NOOP
            FUNCTION main
                PARAMETER at STRUCT c0de/minecraft/POSITION
                PARAMETER input STRUCT c0de/minecraft/POSITION
                PARAMETER contents STRING
                PARAMETER blocks STRING
                RESULT code INTEGER
                RESULT missing HASH INTEGER
            BEGIN
                VARIABLE c TYPE c0de_paint/cpaint/CANVAS
                VARIABLE p STRUCT c0de_paint/cpaint/POINT
                VARIABLE b STRUCT c0de_paint/cpaint/BLOCK
                VARIABLE s TYPE c0de_paint/cpaint/STYLE
                VARIABLE problem ERROR
                ASSIGN b.id "barrel" ;
                ASSIGN b.data "{Items:[{Slot:0b,id:'minecraft:stone',count:3}]}" ;
                CALL c0de_paint/cpaint/STYLE_SOLID
                    PARAMETER block b ;
                    RESULT result s
                CALL c0de_paint/cpaint/DRAW_BLOCK
                    PARAMETER canvas c ;
                    PARAMETER point p ;
                    PARAMETER style s ;
                ASSIGN b.id "gold_block" ;
                ASSIGN b.data "" ;
                CALL c0de_paint/cpaint/STYLE_SOLID
                    PARAMETER block b ;
                    RESULT result s
                ASSIGN p.x 1 ;
                CALL c0de_paint/cpaint/DRAW_BLOCK
                    PARAMETER canvas c ;
                    PARAMETER point p ;
                    PARAMETER style s ;
                CALL c0de_paint/cpaint/RENDER_CANVAS problem
                    PARAMETER canvas c ;
                    PARAMETER position at ;
                    PARAMETER input input ;
                    PARAMETER contents contents ;
                    PARAMETER blocks blocks ;
                    RESULT missing missing
                ASSIGN code problem.code ;
            END FUNCTION
            """;

    private static Map<String, Value> contentsInputs(GameTestHelper helper, String contents, String blocks) {
        return Map.of(
                "at", Processes.position(helper.absolutePos(new BlockPos(0, 3, 0))),
                "input", Processes.position(helper.absolutePos(new BlockPos(0, 1, 0))),
                "contents", new de.joernott.c0de.core.value.StringValue(contents),
                "blocks", new de.joernott.c0de.core.value.StringValue(blocks));
    }

    /**
     * A canvas far away from all players is built: its chunks are loaded for the rendering, like the parts of long
     * programs in c0de (task 9.9).
     */
    public static void farAreasAreLoadedForRendering(GameTestHelper helper) {
        clear(helper);
        chest(helper, new BlockPos(0, 1, 0), new ItemStack(Items.OAK_PLANKS, 9));
        BlockPos far = helper.absolutePos(new BlockPos(0, 3, 0)).offset(4000, 0, 4000);
        helper.assertTrue(!helper.getLevel().isLoaded(far), "The far area is loaded before the test");
        Map<String, Value> inputs = new java.util.HashMap<>(inputs(helper));
        inputs.put("at", Processes.position(far));
        Cursor cursor = Processes.cursor(helper, program(WITH_INPUT), inputs);
        Processes.finish(helper, cursor, () -> {
            helper.assertTrue(code(cursor) == 0, "The rendering failed: " + cursor.results());
            for (BlockPos pos : BlockPos.betweenClosed(far, far.offset(2, 0, 2))) {
                helper.assertTrue(
                        helper.getLevel().getBlockState(pos).is(Blocks.OAK_PLANKS),
                        "No planks at " + pos.toShortString());
            }
            helper.succeed();
        });
    }

    /**
     * A barrel keeps its contents, which come from the input (task 9.2): with "REQUIRED", too few items place nothing;
     * with "AVAILABLE", the barrel gets what there is, a missing block is skipped and both are listed as missing.
     */
    public static void inventoriesAreFilledFromTheInput(GameTestHelper helper) {
        clear(helper);
        Container input = chest(
                helper, new BlockPos(0, 1, 0), new ItemStack(Items.BARREL, 1), new ItemStack(Items.STONE, 2));
        Cursor required = Processes.cursor(helper, CONTENTS_PROGRAM, contentsInputs(helper, "REQUIRED", "REQUIRED"));
        Processes.finish(helper, required, () -> {
            helper.assertTrue(code(required) == 313, "Expected ITEM_MISSING, got " + required.results());
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(0, 3, 0));
            Cursor available =
                    Processes.cursor(helper, CONTENTS_PROGRAM, contentsInputs(helper, "AVAILABLE", "AVAILABLE"));
            Processes.finish(helper, available, () -> {
                helper.assertTrue(code(available) == 0, "The rendering failed: " + available.results());
                helper.assertBlockPresent(Blocks.BARREL, new BlockPos(0, 3, 0));
                helper.assertBlockPresent(Blocks.AIR, new BlockPos(1, 3, 0));
                Container barrel = (Container) TestCompat.blockEntity(helper, new BlockPos(0, 3, 0));
                helper.assertTrue(
                        barrel.getItem(0).is(Items.STONE) && barrel.getItem(0).getCount() == 2,
                        "The barrel holds " + barrel.getItem(0));
                helper.assertTrue(input.isEmpty(), "The input still has items");
                Map<String, Value> missing =
                        ((de.joernott.c0de.core.value.HashValue) available.results().get("missing")).elements();
                helper.assertTrue(
                        new IntegerValue(1).equals(missing.get("minecraft:stone"))
                                && new IntegerValue(1).equals(missing.get("minecraft:gold_block")),
                        "missing: " + missing);
                helper.succeed();
            });
        });
    }

    /**
     * Blocks in the way are mined with the tools: dirt by hand, stone only with a pickaxe, which takes damage. The drops
     * go to the output.
     */
    public static void blocksInTheWayAreMined(GameTestHelper helper) {
        clear(helper);
        chest(helper, new BlockPos(0, 1, 0), new ItemStack(Items.OAK_PLANKS, 9));
        Container tools = chest(helper, new BlockPos(1, 1, 0));
        Container output = chest(helper, new BlockPos(2, 1, 0));
        helper.setBlock(new BlockPos(1, 3, 1), Blocks.DIRT);
        helper.setBlock(new BlockPos(2, 3, 2), Blocks.STONE);
        Cursor withoutPickaxe = Processes.cursor(helper, program(WITH_ALL), inputs(helper));
        Processes.finish(helper, withoutPickaxe, () -> {
            helper.assertTrue(code(withoutPickaxe) == 313, "Expected ITEM_MISSING, got " + withoutPickaxe.results());
            helper.assertBlockPresent(Blocks.DIRT, new BlockPos(1, 3, 1));
            tools.setItem(0, new ItemStack(Items.IRON_PICKAXE));
            Cursor cursor = Processes.cursor(helper, program(WITH_ALL), inputs(helper));
            Processes.finish(helper, cursor, () -> {
                helper.assertTrue(code(cursor) == 0, "The rendering failed: " + cursor.results());
                helper.assertBlockPresent(Blocks.OAK_PLANKS, new BlockPos(1, 3, 1));
                helper.assertBlockPresent(Blocks.OAK_PLANKS, new BlockPos(2, 3, 2));
                helper.assertTrue(tools.getItem(0).getDamageValue() == 1, "The pickaxe wasn't used");
                boolean dirt = false;
                boolean cobblestone = false;
                for (int i = 0; i < output.getContainerSize(); i++) {
                    dirt |= output.getItem(i).is(Items.DIRT);
                    cobblestone |= output.getItem(i).is(Items.COBBLESTONE);
                }
                helper.assertTrue(dirt && cobblestone, "The drops are missing in the output");
                helper.succeed();
            });
        });
    }

    /**
     * The first program of the handbook, as in the handbook of the mod, with an input inventory: a hut of stone bricks
     * with two glass windows and an oak door. The upper half of the door takes no item.
     */
    public static void theFirstProgramOfTheHandbookBuildsAHut(GameTestHelper helper) {
        String entry;
        try (java.io.InputStream in = RenderTests.class.getResourceAsStream(
                "/assets/c0de_paint/handbook/entries/paint/getting_started/first_drawing.md")) {
            helper.assertTrue(in != null, "The handbook entry is missing");
            entry = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).replace("\r\n", "\n");
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        int start = entry.indexOf("```c0de\n") + "```c0de\n".length();
        // The handbook indents by two spaces per level, programs by four
        String text = entry.substring(start, entry.indexOf("```", start))
                .lines()
                .map(line -> " ".repeat(line.length() - line.stripLeading().length()) + line)
                .collect(java.util.stream.Collectors.joining("\n", "", "\n"))
                .replaceFirst(
                        "( *)PARAMETER at STRUCT c0de/minecraft/POSITION\n",
                        "$0$1PARAMETER input STRUCT c0de/minecraft/POSITION\n")
                .replaceFirst("( *)PARAMETER position at ;\n", "$0$1PARAMETER input input ;\n");
        helper.assertTrue(text.contains("PARAMETER input input ;"), "The program has no RENDER_CANVAS");
        Container input = chest(
                helper,
                new BlockPos(7, 1, 7),
                new ItemStack(Items.STONE_BRICKS, 64),
                new ItemStack(Items.STONE_BRICKS, 64),
                new ItemStack(Items.STONE_BRICKS, 28),
                new ItemStack(Items.GLASS, 12),
                new ItemStack(Items.OAK_DOOR, 1));
        Cursor cursor = Processes.cursor(
                helper,
                text,
                Map.of(
                        "at", Processes.position(helper.absolutePos(new BlockPos(0, 1, 0))),
                        "input", Processes.position(helper.absolutePos(new BlockPos(7, 1, 7)))));
        Processes.finish(helper, cursor, () -> {
            helper.assertTrue(cursor.state() == CursorState.FINISHED, "The program crashed: " + cursor.error());
            helper.assertBlockPresent(Blocks.STONE_BRICKS, new BlockPos(0, 1, 0));
            helper.assertBlockPresent(Blocks.STONE_BRICKS, new BlockPos(6, 5, 6));
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(3, 3, 3));
            for (int y = 3; y <= 4; y++) {
                for (int z = 2; z <= 4; z++) {
                    helper.assertBlockPresent(Blocks.GLASS, new BlockPos(0, y, z));
                    helper.assertBlockPresent(Blocks.GLASS, new BlockPos(6, y, z));
                }
            }
            helper.assertBlockPresent(Blocks.OAK_DOOR, new BlockPos(3, 2, 0));
            helper.assertBlockPresent(Blocks.OAK_DOOR, new BlockPos(3, 3, 0));
            helper.assertTrue(
                    helper.getBlockState(new BlockPos(3, 3, 0)).getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)
                            == DoubleBlockHalf.UPPER,
                    "The upper half of the door is missing");
            helper.assertTrue(input.isEmpty(), "The input still has items");
            helper.succeed();
        });
    }

    /** CANVAS_FROM_WORLD copies the blocks with their states; air becomes not set unless air is TRUE. */
    public static void theWorldIsCopiedIntoACanvas(GameTestHelper helper) {
        clear(helper);
        helper.setBlock(
                new BlockPos(0, 2, 0),
                Blocks.OAK_STAIRS
                        .defaultBlockState()
                        .setValue(net.minecraft.world.level.block.StairBlock.FACING, net.minecraft.core.Direction.EAST));
        helper.setBlock(new BlockPos(2, 2, 0), Blocks.STONE);
        Map<String, Value> results = Processes.run(
                helper,
                """
                PROGRAM copying
                NOOP
                INCLUDE c0de/minecraft
                INCLUDE c0de_paint/cpaint
                NOOP
                FUNCTION main
                    PARAMETER from STRUCT c0de/minecraft/POSITION
                    PARAMETER to STRUCT c0de/minecraft/POSITION
                    RESULT count INTEGER
                    RESULT all INTEGER
                    RESULT facing STRING
                BEGIN
                    VARIABLE c TYPE c0de_paint/cpaint/CANVAS
                    VARIABLE p STRUCT c0de_paint/cpaint/POINT
                    VARIABLE b STRUCT c0de_paint/cpaint/BLOCK
                    CALL c0de_paint/cpaint/CANVAS_FROM_WORLD
                        PARAMETER from from ;
                        PARAMETER to to ;
                        RESULT result c
                    CALL c0de_paint/cpaint/CANVAS_COUNT
                        PARAMETER canvas c ;
                        RESULT result count
                    CALL c0de_paint/cpaint/CANVAS_GET
                        PARAMETER canvas c ;
                        PARAMETER point p ;
                        RESULT result b
                    ASSIGN facing b.properties["facing"] ;
                    CALL c0de_paint/cpaint/CANVAS_FROM_WORLD
                        PARAMETER from from ;
                        PARAMETER to to ;
                        PARAMETER air TRUE ;
                        RESULT result c
                    CALL c0de_paint/cpaint/CANVAS_COUNT
                        PARAMETER canvas c ;
                        RESULT result all
                END FUNCTION
                """,
                Map.of(
                        "from", Processes.position(helper.absolutePos(new BlockPos(0, 2, 0))),
                        "to", Processes.position(helper.absolutePos(new BlockPos(2, 2, 0)))));
        helper.assertTrue(results.get("count").equals(new IntegerValue(2)), "count: " + results);
        helper.assertTrue(results.get("all").equals(new IntegerValue(3)), "with air: " + results);
        helper.assertTrue(results.get("facing").equals(new StringValue("east")), "facing: " + results);
        helper.succeed();
    }
}
