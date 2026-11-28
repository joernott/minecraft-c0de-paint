package de.joernott.c0depaint.world;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * The items that build a block (task 9.3, docs/block_review.md, point 5): what RENDER_CANVAS takes from the
 * inventories and what SCHEMATIC_MATERIALS lists.
 */
public final class BlockItems {
    private BlockItems() {}

    /**
     * True for the parts of blocks that the other part places: upper halves of doors and plants, heads of beds,
     * piston heads, flowing water and lava.
     */
    public static boolean part(BlockState state) {
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
            return true;
        }
        if (state.hasProperty(BlockStateProperties.BED_PART)
                && state.getValue(BlockStateProperties.BED_PART) == BedPart.HEAD) {
            return true;
        }
        Block block = state.getBlock();
        if (block instanceof LiquidBlock && state.getValue(LiquidBlock.LEVEL) != 0) {
            return true;
        }
        String id = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return id.equals("piston_head") || id.equals("moving_piston");
    }

    /**
     * The items that place the block, with their numbers; empty for air, parts and blocks that no item places (fire,
     * portals). Water and lava sources take buckets, waterlogged blocks a water bucket more.
     */
    public static Map<Item, Integer> items(BlockState state) {
        Map<Item, Integer> items = new LinkedHashMap<>();
        if (state.isAir() || part(state)) {
            return items;
        }
        Block block = state.getBlock();
        if (block instanceof LiquidBlock) {
            Item bucket = block.defaultBlockState().getFluidState().getType().getBucket();
            if (bucket != Items.AIR) {
                items.put(bucket, 1);
            }
            return items;
        }
        if (block instanceof FlowerPotBlock pot && pot.getPotted() != net.minecraft.world.level.block.Blocks.AIR) {
            items.put(Items.FLOWER_POT, 1);
            add(items, pot.getPotted().asItem(), 1);
            return items;
        }
        Item item = block.asItem();
        if (item == Items.AIR) {
            return items;
        }
        add(items, item, count(state));
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
            add(items, Items.WATER_BUCKET, 1);
        }
        return items;
    }

    /** How many of its item a block takes: double slabs, candles, sea pickles, turtle eggs, snow layers. */
    private static int count(BlockState state) {
        if (state.hasProperty(BlockStateProperties.SLAB_TYPE)
                && state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE) {
            return 2;
        }
        if (state.hasProperty(BlockStateProperties.CANDLES)) {
            return state.getValue(BlockStateProperties.CANDLES);
        }
        if (state.hasProperty(BlockStateProperties.PICKLES)) {
            return state.getValue(BlockStateProperties.PICKLES);
        }
        if (state.hasProperty(BlockStateProperties.EGGS)) {
            return state.getValue(BlockStateProperties.EGGS);
        }
        if (state.hasProperty(BlockStateProperties.LAYERS)
                && BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().equals("snow")) {
            return state.getValue(BlockStateProperties.LAYERS);
        }
        return 1;
    }

    private static void add(Map<Item, Integer> items, Item item, int count) {
        if (item != Items.AIR) {
            items.merge(item, count, Integer::sum);
        }
    }
}
