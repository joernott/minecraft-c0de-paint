package de.joernott.c0depaint.world;

import com.mojang.authlib.GameProfile;
import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.library.ProcessHost;
import de.joernott.c0de.sheet.LevelSheet;
import de.joernott.c0depaint.core.canvas.Block;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.config.PaintConfig;
import de.joernott.c0depaint.core.render.RenderPlan;
import de.joernott.c0depaint.core.transform.Job;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import java.util.random.RandomGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * RENDER_CANVAS (docs/concept.md#rendering): checks everything it can before the first block, then places up to
 * renderBlocksPerTick blocks per tick from bottom to top while the process waits. Blocks in the way are replaced
 * (without input) or mined with the tools, which take damage and the time of a player divided by renderMiningSpeed.
 */
final class Renderer implements Job {
    /**
     * A block to place or remove: the items it takes (none without input, null if no item places it) and the data of
     * its block entity (task 9.2).
     */
    private record Work(BlockPos pos, BlockState target, Map<Item, Integer> items, CompoundTag data) {}

    /** The keys of block entity data that would create things out of nothing in survival (docs/concept.md). */
    private static final List<String> FREE_THINGS =
            List.of("LootTable", "LootTableSeed", "Bees", "bees", "flower_pos", "FlowerPos", "item", "Book", "RecordItem");

    /** Modes of the parameters {@code contents} and {@code blocks}. */
    private static final String REQUIRED = "REQUIRED";

    private static final String AVAILABLE = "AVAILABLE";
    private static final String NONE = "NONE";

    private final ServerLevel level;
    private final Container input;
    private final Container tools;
    private final Container output;
    private final List<Work> work;
    private final int perTick;
    private final int miningSpeed;
    private final String contents;
    private final String blocks;
    private final Map<String, Long> missing = new java.util.TreeMap<>();
    private int next;
    private int placed;
    private int wait;

    private Renderer(
            ServerLevel level,
            Container input,
            Container tools,
            Container output,
            List<Work> work,
            PaintConfig settings,
            String contents,
            String blocks) {
        this.level = level;
        this.contents = contents;
        this.blocks = blocks;
        this.input = input;
        this.tools = tools;
        this.output = output;
        this.work = work;
        this.perTick = settings.get(PaintConfig.RENDER_BLOCKS_PER_TICK);
        this.miningSpeed = settings.get(PaintConfig.RENDER_MINING_SPEED);
    }

    /** Checks the rendering and creates its job. */
    static Renderer start(NativeCall call, Canvas canvas, StructValue position, PaintConfig settings) {
        ProcessHost host = MinecraftWorld.host(call);
        MinecraftWorld.Place origin = MinecraftWorld.place(call, position);
        ServerLevel level = origin.level();
        Container input = inventory(call, "input");
        Container tools = inventory(call, "tools");
        Container output = inventory(call, "output");
        if (input == null) {
            Rights.requireBlockChanges(host);
        }
        String contents = mode(call, "contents", List.of(REQUIRED, AVAILABLE, NONE));
        String blocks = mode(call, "blocks", List.of(REQUIRED, AVAILABLE));
        List<ItemStack> contentsNeeded = new ArrayList<>();
        ServerPlayer player = input == null ? null : player(host, level);
        Map<Block, BlockState> states = new HashMap<>();
        RenderPlan plan = RenderPlan.of(canvas, block -> hangs(states.computeIfAbsent(block, Renderer::state)));
        List<Work> work = new ArrayList<>();
        Map<Item, Integer> needed = new LinkedHashMap<>();
        for (RenderPlan.Step step : plan.steps()) {
            BlockPos pos = origin.pos().offset(step.voxel().x(), step.voxel().y(), step.voxel().z());
            if (!level.isLoaded(pos)) {
                throw C0deException.of(ErrorCode.CHUNK_NOT_LOADED, "The chunk of " + pos.toShortString() + " is not loaded");
            }
            BlockState target = states.computeIfAbsent(step.block(), Renderer::state);
            BlockState current = level.getBlockState(pos);
            if (current.equals(target) || (current.isAir() && target.isAir())) {
                continue;
            }
            if (LevelSheet.isOnSheet(level, pos) && pos.getY() >= SheetGeometry.FLOOR_Y) {
                throw C0deException.of(ErrorCode.PERMISSION_DENIED, "Blocks on the sheets can't be changed by programs");
            }
            if (!current.isAir() && current.getDestroySpeed(level, pos) < 0) {
                throw C0deException.of(
                        ErrorCode.PERMISSION_DENIED, "The block at " + pos.toShortString() + " can't be broken");
            }
            if (player != null && !level.mayInteract(player, pos)) {
                throw C0deException.of(
                        ErrorCode.PERMISSION_DENIED,
                        "The owner of the trigger block can't change the block at " + pos.toShortString()
                                + " (spawn protection or world border)");
            }
            Map<Item, Integer> items = Map.of();
            if (input != null) {
                if (removes(current, target) && !Mining.fits(current, bestTool(level, pos, current, tools))) {
                    throw C0deException.of(
                            ErrorCode.ITEM_MISSING,
                            "There is no tool that mines " + id(current) + " at " + pos.toShortString());
                }
                items = BlockItems.items(target);
                if (items.isEmpty() && !target.isAir() && !BlockItems.part(target)) {
                    if (blocks.equals(REQUIRED)) {
                        throw C0deException.of(
                                ErrorCode.INVALID_ARGUMENT,
                                id(target) + " has no item and can't be taken from an inventory");
                    }
                    // Placed by no item: skipped and listed with its block id
                    items = null;
                } else {
                    items.forEach((item, count) -> needed.merge(item, count, Integer::sum));
                }
            }
            CompoundTag data = data(step.block(), target, pos, host, input != null);
            if (data != null && input != null && !contents.equals(NONE)) {
                contentsNeeded.addAll(contents(level, pos, target, data));
            }
            work.add(new Work(pos, target, items, data));
        }
        if (input != null && blocks.equals(REQUIRED)) {
            for (Map.Entry<Item, Integer> need : needed.entrySet()) {
                int available = Mining.count(output, need.getKey()) + Mining.count(input, need.getKey());
                if (available < need.getValue()) {
                    throw C0deException.of(
                            ErrorCode.ITEM_MISSING,
                            "The inventories contain " + available + " of the " + need.getValue() + " "
                                    + BuiltInRegistries.ITEM.getKey(need.getKey()) + " the canvas needs");
                }
            }
        }
        if (input != null && contents.equals(REQUIRED)) {
            checkContents(contentsNeeded, needed, input, output);
        }
        return new Renderer(level, input, tools, output, work, settings, contents, blocks);
    }

    @Override
    public boolean step(int budget, RandomGenerator random) {
        if (wait > 0) {
            wait--;
            return false;
        }
        int done = 0;
        while (next < work.size() && done < perTick && wait == 0) {
            Work item = work.get(next);
            if (!level.isLoaded(item.pos())) {
                // Waits until the chunk is loaded again
                return false;
            }
            render(item);
            next++;
            done++;
        }
        return next >= work.size() && wait == 0;
    }

    private void render(Work item) {
        BlockPos pos = item.pos();
        BlockState current = level.getBlockState(pos);
        if (current.equals(item.target())) {
            return;
        }
        if (input != null) {
            if (item.items() == null) {
                missing.merge(id(item.target()), 1L, Long::sum);
                return;
            }
            // The items are checked before a block in the way is mined
            for (Map.Entry<Item, Integer> need : item.items().entrySet()) {
                int available = Mining.count(output, need.getKey()) + Mining.count(input, need.getKey());
                if (available < need.getValue()) {
                    if (blocks.equals(REQUIRED)) {
                        throw failed(
                                ErrorCode.ITEM_MISSING,
                                "The inventories contain no more " + BuiltInRegistries.ITEM.getKey(need.getKey()));
                    }
                    item.items().forEach((it, count) -> missing.merge(
                            BuiltInRegistries.ITEM.getKey(it).toString(), (long) count, Long::sum));
                    return;
                }
            }
            if (removes(current, item.target())) {
                mine(pos, current);
            }
        }
        if (!item.target().isAir() || !level.getBlockState(pos).isAir()) {
            if (input != null) {
                for (Map.Entry<Item, Integer> need : item.items().entrySet()) {
                    for (int i = 0; i < need.getValue(); i++) {
                        if (!Mining.take(output, need.getKey()) && !Mining.take(input, need.getKey())) {
                            throw failed(
                                    ErrorCode.ITEM_MISSING,
                                    "The inventories contain no more " + BuiltInRegistries.ITEM.getKey(need.getKey()));
                        }
                        if (need.getKey() instanceof net.minecraft.world.item.BucketItem && need.getKey() != Items.BUCKET) {
                            giveBack(pos, new ItemStack(Items.BUCKET));
                        }
                    }
                }
            }
            level.setBlock(pos, item.target(), net.minecraft.world.level.block.Block.UPDATE_ALL);
            if (item.data() != null) {
                placeData(pos, item);
            }
        }
        placed++;
    }

    /** Loads the data into the new block entity and fills its inventory from the inventories (task 9.2). */
    private void placeData(BlockPos pos, Work item) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return;
        }
        Compat.loadBlockEntityData(blockEntity, item.data().copy(), level.registryAccess());
        if (input != null && blockEntity instanceof Container inventory) {
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack wanted = inventory.getItem(slot);
                if (wanted.isEmpty()) {
                    continue;
                }
                int taken = contents.equals(NONE) ? 0 : takeMatching(output, wanted, wanted.getCount());
                taken += contents.equals(NONE) ? 0 : takeMatching(input, wanted, wanted.getCount() - taken);
                if (taken < wanted.getCount()) {
                    if (contents.equals(REQUIRED)) {
                        throw failed(
                                ErrorCode.ITEM_MISSING,
                                "The inventories contain no more " + BuiltInRegistries.ITEM.getKey(wanted.getItem())
                                        + " for the contents of " + id(item.target()));
                    }
                    if (contents.equals(AVAILABLE)) {
                        missing.merge(
                                BuiltInRegistries.ITEM.getKey(wanted.getItem()).toString(),
                                (long) (wanted.getCount() - taken),
                                Long::sum);
                    }
                }
                inventory.setItem(slot, taken == 0 ? ItemStack.EMPTY : wanted.copyWithCount(taken));
            }
        }
        blockEntity.setChanged();
        level.sendBlockUpdated(pos, item.target(), item.target(), net.minecraft.world.level.block.Block.UPDATE_ALL);
    }

    /** Puts an item back into the output, else the input, else drops it at the position. */
    private void giveBack(BlockPos pos, ItemStack stack) {
        ItemStack rest = output == null ? stack : Mining.insert(output, stack);
        rest = rest.isEmpty() || input == null ? rest : Mining.insert(input, rest);
        if (!rest.isEmpty()) {
            net.minecraft.world.level.block.Block.popResource(level, pos, rest);
        }
    }

    @Override
    public Map<String, de.joernott.c0de.core.value.Value> results() {
        de.joernott.c0de.core.value.HashValue hash =
                new de.joernott.c0de.core.value.HashValue(de.joernott.c0de.core.value.Type.Simple.INTEGER);
        missing.forEach((key, count) -> hash.elements().put(key, new de.joernott.c0de.core.value.IntegerValue(count)));
        return Map.of("missing", hash);
    }

    /** The value of a mode parameter, one of the allowed texts. */
    private static String mode(NativeCall call, String name, List<String> allowed) {
        String value = ((de.joernott.c0de.core.value.StringValue) call.argument(name)).value();
        if (!allowed.contains(value)) {
            throw C0deException.of(
                    ErrorCode.INVALID_ARGUMENT, name + " is '" + value + "', not one of " + String.join(", ", allowed));
        }
        return value;
    }

    /**
     * The data of a block as a compound, checked before the first block: SNBT, rights for the data of command blocks
     * and the like; with input, without what would come out of nothing. Null for blocks without data or without a
     * block entity (after a replacement, e.g. a chest that became stone).
     */
    private static CompoundTag data(Block block, BlockState target, BlockPos pos, ProcessHost host, boolean survival) {
        if (!block.hasData()
                || !(target.getBlock() instanceof net.minecraft.world.level.block.EntityBlock entityBlock)) {
            return null;
        }
        BlockEntity sample = entityBlock.newBlockEntity(pos, target);
        if (sample == null) {
            return null;
        }
        if (Compat.onlyOpCanSetData(sample) && !Rights.isAdministrator(host)) {
            throw C0deException.of(
                    ErrorCode.PERMISSION_DENIED, "Only administrators can place the data of " + id(target));
        }
        CompoundTag data;
        try {
            data = Compat.parseCompound(block.data());
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            throw C0deException.of(
                    ErrorCode.INVALID_ARGUMENT, "The data of " + block.id() + " is not SNBT: " + e.getMessage());
        }
        if (survival) {
            boolean inventory = sample instanceof Container;
            for (String key : FREE_THINGS) {
                // The items of an inventory are its contents, refilled from the inventories; other items are free
                if (!(inventory && (key.equals("item") || key.equals("RecordItem")))) {
                    data.remove(key);
                }
            }
            if (!inventory) {
                data.remove("Items");
            }
        }
        return data;
    }

    /** The contents of an inventory with the data, without placing it. */
    private static List<ItemStack> contents(ServerLevel level, BlockPos pos, BlockState target, CompoundTag data) {
        if (!(target.getBlock() instanceof net.minecraft.world.level.block.EntityBlock entityBlock)) {
            return List.of();
        }
        BlockEntity sample = entityBlock.newBlockEntity(pos, target);
        if (!(sample instanceof Container inventory)) {
            return List.of();
        }
        sample.setLevel(level);
        Compat.loadBlockEntityData(sample, data.copy(), level.registryAccess());
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (!inventory.getItem(slot).isEmpty()) {
                stacks.add(inventory.getItem(slot).copy());
            }
        }
        return stacks;
    }

    /** ITEM_MISSING if the inventories don't hold the contents besides the blocks (contents "REQUIRED"). */
    private static void checkContents(
            List<ItemStack> wanted, Map<Item, Integer> blocks, Container input, Container output) {
        List<ItemStack> kinds = new ArrayList<>();
        for (ItemStack stack : wanted) {
            ItemStack kind = kinds.stream()
                    .filter(k -> ItemStack.isSameItemSameComponents(k, stack))
                    .findFirst()
                    .orElse(null);
            if (kind == null) {
                kinds.add(stack.copy());
            } else {
                kind.grow(stack.getCount());
            }
        }
        for (ItemStack kind : kinds) {
            long available = countMatching(output, kind) + countMatching(input, kind);
            if (kind.getComponentsPatch().isEmpty()) {
                available -= blocks.getOrDefault(kind.getItem(), 0);
            }
            if (available < kind.getCount()) {
                throw C0deException.of(
                        ErrorCode.ITEM_MISSING,
                        "The inventories contain " + Math.max(0, available) + " of the " + kind.getCount() + " "
                                + BuiltInRegistries.ITEM.getKey(kind.getItem())
                                + " the contents of the inventories of the canvas need");
            }
        }
    }

    /** The items of an inventory that are the same as the stack, with the same components. */
    private static long countMatching(Container inventory, ItemStack stack) {
        if (inventory == null) {
            return 0;
        }
        long count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (ItemStack.isSameItemSameComponents(inventory.getItem(i), stack)) {
                count += inventory.getItem(i).getCount();
            }
        }
        return count;
    }

    /** Takes up to a number of items that are the same as the stack; returns how many were taken. */
    private static int takeMatching(Container inventory, ItemStack stack, int wanted) {
        if (inventory == null || wanted <= 0) {
            return 0;
        }
        int taken = 0;
        for (int i = 0; i < inventory.getContainerSize() && taken < wanted; i++) {
            ItemStack slot = inventory.getItem(i);
            if (ItemStack.isSameItemSameComponents(slot, stack)) {
                int amount = Math.min(slot.getCount(), wanted - taken);
                slot.shrink(amount);
                taken += amount;
            }
        }
        if (taken > 0) {
            inventory.setChanged();
        }
        return taken;
    }

    /** Mines a block in the way like MINE_BLOCK: the drops go to the output, the tool takes damage. */
    private void mine(BlockPos pos, BlockState current) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack tool = bestTool(level, pos, current, tools);
        if (!Mining.fits(current, tool)) {
            throw failed(ErrorCode.ITEM_MISSING, "There is no tool left that mines " + id(current));
        }
        List<ItemStack> drops =
                net.minecraft.world.level.block.Block.getDrops(current, level, pos, blockEntity, null, tool);
        if (output != null && !Mining.fits(output, drops)) {
            throw failed(ErrorCode.INVENTORY_FULL, "The drops of " + id(current) + " don't fit into the output");
        }
        float hardness = current.getDestroySpeed(level, pos);
        int ticks = Mining.ticks(current, hardness, tool);
        if (!tool.isEmpty()) {
            net.minecraft.world.item.component.Tool component =
                    tool.get(net.minecraft.core.component.DataComponents.TOOL);
            if (component != null && hardness != 0) {
                tool.hurtAndBreak(component.damagePerBlock(), level, null, broken -> {});
            }
            tools.setChanged();
        }
        level.destroyBlock(pos, false);
        current.spawnAfterBreak(level, pos, tool, true);
        if (output != null) {
            drops.forEach(drop -> Mining.insert(output, drop));
        }
        wait = miningSpeed == 0 ? 0 : (int) Math.ceil((double) ticks / miningSpeed);
    }

    private C0deException failed(ErrorCode code, String message) {
        return C0deException.of(
                code, message + "; " + placed + (placed == 1 ? " block was" : " blocks were") + " placed");
    }

    /** True if the block in the way has to be mined: it is neither air nor replaceable (like grass or water). */
    private static boolean removes(BlockState current, BlockState target) {
        return !current.isAir() && !current.canBeReplaced();
    }

    private static ItemStack bestTool(ServerLevel level, BlockPos pos, BlockState state, Container tools) {
        return Mining.bestTool(level, pos, state, level.getBlockEntity(pos), tools);
    }

    /** The inventory of an optional POSITION parameter, or null. */
    private static Container inventory(NativeCall call, String name) {
        if (!call.provided(name)) {
            return null;
        }
        MinecraftWorld.Place place = MinecraftWorld.place(call, (StructValue) call.argument(name));
        if (place.level().getBlockEntity(place.pos()) instanceof Container container) {
            return container;
        }
        throw C0deException.of(
                ErrorCode.INVALID_ARGUMENT, "The block at " + place.pos().toShortString() + " has no inventory");
    }

    /** The owner of the trigger block, or a fake player for an owner who is offline. */
    private static ServerPlayer player(ProcessHost host, ServerLevel level) {
        UUID owner = host.owner() == null ? new UUID(0, 0) : host.owner();
        ServerPlayer player = host.server().getPlayerList().getPlayer(owner);
        if (player == null) {
            String name = Compat.knownName(host.server(), owner).orElse("c0de");
            player = de.joernott.c0de.platform.Platform.INSTANCE.fakePlayer(level, new GameProfile(owner, name));
        }
        return player;
    }

    /** The block state of a block of a canvas; INVALID_ARGUMENT for unknown blocks and states. */
    static BlockState state(Block block) {
        ResourceLocation id = ResourceLocation.tryParse(block.id());
        if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
            throw C0deException.of(ErrorCode.INVALID_ARGUMENT, "There is no block " + block.id());
        }
        BlockState state = MinecraftWorld.registered(id).defaultBlockState();
        for (Map.Entry<String, String> entry : block.properties().entrySet()) {
            Property<?> property = state.getBlock().getStateDefinition().getProperty(entry.getKey());
            if (property == null) {
                throw C0deException.of(ErrorCode.INVALID_ARGUMENT, block.id() + " has no property " + entry.getKey());
            }
            state = withValue(state, property, entry.getValue());
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState withValue(BlockState state, Property<T> property, String text) {
        return property.getValue(text)
                .map(value -> state.setValue(property, value))
                .orElseThrow(() -> C0deException.of(
                        ErrorCode.INVALID_ARGUMENT, text + " is not a value of " + property.getName()));
    }

    /** True for blocks that hang on others, which are placed after the other blocks of their layer. */
    private static boolean hangs(BlockState state) {
        net.minecraft.world.level.block.Block block = state.getBlock();
        return block instanceof net.minecraft.world.level.block.BaseTorchBlock
                || block instanceof net.minecraft.world.level.block.LadderBlock
                || block instanceof net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock
                || block instanceof net.minecraft.world.level.block.SignBlock
                || block instanceof net.minecraft.world.level.block.TripWireHookBlock
                || block instanceof net.minecraft.world.level.block.VineBlock
                || block instanceof net.minecraft.world.level.block.CocoaBlock;
    }

    private static String id(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }
}
