package de.joernott.c0depaint.world;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tools and inventories as in MINE_BLOCK and PLACE_BLOCK of c0de/minecraft, whose helpers are not public: the tool a
 * player would choose, the ticks a player needs, and putting items into and taking them out of inventories.
 */
final class Mining {
    private Mining() {}

    /**
     * The tool a player would choose, the first of the best group in the inventory: tools that obtain the block and
     * break it faster than bare hands; items that obtain it when bare hands don't; tools that break it faster without
     * obtaining it; bare hands.
     */
    static ItemStack bestTool(ServerLevel level, BlockPos pos, BlockState state, BlockEntity blockEntity, Container tools) {
        if (tools == null) {
            return ItemStack.EMPTY;
        }
        boolean handObtains = obtains(level, pos, state, blockEntity, ItemStack.EMPTY);
        ItemStack best = ItemStack.EMPTY;
        int bestGroup = handObtains ? 2 : 4;
        for (int i = 0; i < tools.getContainerSize() && bestGroup > 1; i++) {
            ItemStack stack = tools.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            boolean faster = stack.getDestroySpeed(state) > 1.0F;
            boolean obtains = obtains(level, pos, state, blockEntity, stack);
            int group;
            if (obtains && faster) {
                group = 1;
            } else if (obtains && !handObtains) {
                group = 2;
            } else if (faster && !handObtains) {
                group = 3;
            } else {
                continue;
            }
            if (group < bestGroup) {
                best = stack;
                bestGroup = group;
            }
        }
        return best;
    }

    /** True if mining the block with the tool obtains it. */
    static boolean obtains(ServerLevel level, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        if (state.requiresCorrectToolForDrops()) {
            return tool.isCorrectToolForDrops(state);
        }
        Item own = state.getBlock().asItem();
        return Block.getDrops(state, level, pos, blockEntity, null, tool).stream().anyMatch(drop -> drop.is(own));
    }

    /**
     * True if the tool fits the block: blocks that need the correct tool for drops (stone, ores, obsidian) need it,
     * all other blocks can be mined by hand.
     */
    static boolean fits(BlockState state, ItemStack tool) {
        return !state.requiresCorrectToolForDrops() || tool.isCorrectToolForDrops(state);
    }

    /** The ticks a player on the ground needs to break the block with the tool. */
    static int ticks(BlockState state, float hardness, ItemStack tool) {
        if (hardness == 0) {
            return 0;
        }
        float speed = tool.getDestroySpeed(state);
        if (speed > 1.0F) {
            double[] efficiency = {0};
            net.minecraft.world.item.enchantment.EnchantmentHelper.forEachModifier(
                    tool, net.minecraft.world.entity.EquipmentSlot.MAINHAND, (attribute, modifier) -> {
                        if (attribute.value()
                                == net.minecraft.world.entity.ai.attributes.Attributes.MINING_EFFICIENCY.value()) {
                            efficiency[0] += modifier.amount();
                        }
                    });
            speed += (float) efficiency[0];
        }
        boolean harvest = fits(state, tool);
        float progress = speed / hardness / (harvest ? 30 : 100);
        return progress >= 1.0F ? 0 : (int) Math.ceil(1.0F / progress);
    }

    /** True if all stacks fit into the inventory. */
    static boolean fits(Container inventory, List<ItemStack> stacks) {
        SimpleContainer copy = new SimpleContainer(inventory.getContainerSize()) {
            @Override
            public int getMaxStackSize() {
                return inventory.getMaxStackSize();
            }

            @Override
            public boolean canPlaceItem(int slot, ItemStack stack) {
                return inventory.canPlaceItem(slot, stack);
            }
        };
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            copy.setItem(i, inventory.getItem(i).copy());
        }
        for (ItemStack stack : stacks) {
            if (!insert(copy, stack.copy()).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** Puts a stack into an inventory, first onto stacks of the same item; returns what doesn't fit. */
    static ItemStack insert(Container inventory, ItemStack stack) {
        for (int pass = 0; pass < 2 && !stack.isEmpty(); pass++) {
            for (int i = 0; i < inventory.getContainerSize() && !stack.isEmpty(); i++) {
                ItemStack target = inventory.getItem(i);
                if (pass == 0 && !target.isEmpty() && ItemStack.isSameItemSameComponents(stack, target)) {
                    int amount = Math.min(
                            stack.getCount(),
                            Math.min(target.getMaxStackSize(), inventory.getMaxStackSize()) - target.getCount());
                    if (amount > 0) {
                        target.grow(amount);
                        stack.shrink(amount);
                    }
                } else if (pass == 1 && target.isEmpty() && inventory.canPlaceItem(i, stack)) {
                    int amount =
                            Math.min(stack.getCount(), Math.min(stack.getMaxStackSize(), inventory.getMaxStackSize()));
                    inventory.setItem(i, stack.split(amount));
                }
            }
        }
        inventory.setChanged();
        return stack;
    }

    /** The number of items of a kind in an inventory, or 0 without inventory. */
    static int count(Container inventory, Item item) {
        if (inventory == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /** Takes one item from the first stack of it in the inventory; false if there is none. */
    static boolean take(Container inventory, Item item) {
        if (inventory == null) {
            return false;
        }
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(item)) {
                stack.shrink(1);
                inventory.setChanged();
                return true;
            }
        }
        return false;
    }
}
