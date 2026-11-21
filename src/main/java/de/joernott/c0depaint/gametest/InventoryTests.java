package de.joernott.c0depaint.gametest;

import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Value;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/** INVENTORY_BLOCKS (task 3.7). */
public final class InventoryTests {
    private InventoryTests() {}

    /**
     * The blocks of a chest in the order of the slots, with the number of items as weight; a block in several slots is
     * listed once; items that aren't blocks are ignored.
     */
    public static void inventoryBlocksFollowTheSlots(GameTestHelper helper) {
        BlockPos chest = new BlockPos(0, 0, 0);
        helper.setBlock(chest, Blocks.CHEST);
        Container container = (Container) TestCompat.blockEntity(helper, chest);
        container.setItem(0, new ItemStack(Items.STONE, 10));
        container.setItem(1, new ItemStack(Items.STICK, 5));
        container.setItem(2, new ItemStack(Items.DIRT, 3));
        container.setItem(4, new ItemStack(Items.STONE, 4));
        Map<String, Value> results = Processes.run(
                helper,
                """
                PROGRAM inventory
                NOOP
                INCLUDE c0de/minecraft
                INCLUDE c0de_paint/cpaint
                NOOP
                FUNCTION main
                    PARAMETER pos STRUCT c0de/minecraft/POSITION
                    RESULT blocks ARRAY STRUCT c0de_paint/cpaint/BLOCK
                BEGIN
                    CALL c0de_paint/cpaint/INVENTORY_BLOCKS
                        PARAMETER position pos ;
                        RESULT result blocks
                END FUNCTION
                """,
                Map.of("pos", Processes.position(helper.absolutePos(chest))));
        List<Value> blocks = ((ArrayValue) results.get("blocks")).elements();
        helper.assertTrue(blocks.size() == 2, "Expected stone and dirt, got " + blocks);
        check(helper, blocks.get(0), "minecraft:stone", 14);
        check(helper, blocks.get(1), "minecraft:dirt", 3);
        helper.succeed();
    }

    private static void check(GameTestHelper helper, Value value, String id, long weight) {
        StructValue block = (StructValue) value;
        helper.assertTrue(
                block.get("id").equals(new StringValue(id)) && block.get("weight").equals(new IntegerValue(weight)),
                "Expected " + id + " with weight " + weight + ", got " + block);
    }
}
