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
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * RENDER_CANVAS (docs/concept.md#rendering): checks everything it can before the first block, then places up to
 * renderBlocksPerTick blocks per tick from bottom to top while the process waits. Blocks in the way are replaced
 * (without input) or mined with the tools, which take damage and the time of a player divided by renderMiningSpeed.
 */
final class Renderer implements Job {
    /** A block to place or remove. */
    private record Work(BlockPos pos, BlockState target, Item item) {}

    private final ServerLevel level;
    private final Container input;
    private final Container tools;
    private final Container output;
    private final List<Work> work;
    private final int perTick;
    private final int miningSpeed;
    private int next;
    private int placed;
    private int wait;

    private Renderer(
            ServerLevel level, Container input, Container tools, Container output, List<Work> work, PaintConfig settings) {
        this.level = level;
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
            Item item = null;
            if (input != null) {
                if (removes(current, target) && !Mining.fits(current, bestTool(level, pos, current, tools))) {
                    throw C0deException.of(
                            ErrorCode.ITEM_MISSING,
                            "There is no tool that mines " + id(current) + " at " + pos.toShortString());
                }
                item = item(target);
                if (item != null) {
                    needed.merge(item, 1, Integer::sum);
                }
            }
            work.add(new Work(pos, target, item));
        }
        for (Map.Entry<Item, Integer> need : needed.entrySet()) {
            int available = Mining.count(output, need.getKey()) + Mining.count(input, need.getKey());
            if (available < need.getValue()) {
                throw C0deException.of(
                        ErrorCode.ITEM_MISSING,
                        "The inventories contain " + available + " of the " + need.getValue() + " "
                                + BuiltInRegistries.ITEM.getKey(need.getKey()) + " the canvas needs");
            }
        }
        return new Renderer(level, input, tools, output, work, settings);
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
        if (input != null && removes(current, item.target())) {
            mine(pos, current);
        }
        if (!item.target().isAir() || !level.getBlockState(pos).isAir()) {
            if (item.item() != null && !Mining.take(output, item.item()) && !Mining.take(input, item.item())) {
                throw failed(ErrorCode.ITEM_MISSING, "The inventories contain no more " + BuiltInRegistries.ITEM.getKey(item.item()));
            }
            level.setBlock(pos, item.target(), net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
        placed++;
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

    /** The item a player places the block with, or null for air and the upper halves of doors, plants and beds. */
    private static Item item(BlockState target) {
        if (target.isAir()
                || (target.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                        && target.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER)
                || (target.hasProperty(BlockStateProperties.BED_PART)
                        && target.getValue(BlockStateProperties.BED_PART) == BedPart.HEAD)) {
            return null;
        }
        Item item = target.getBlock().asItem();
        if (item == Items.AIR) {
            throw C0deException.of(
                    ErrorCode.INVALID_ARGUMENT, id(target) + " has no item and can't be taken from an inventory");
        }
        return item;
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
