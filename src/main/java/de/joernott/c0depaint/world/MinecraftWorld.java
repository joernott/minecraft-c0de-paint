package de.joernott.c0depaint.world;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.library.ProcessHost;
import de.joernott.c0depaint.core.canvas.Block;
import de.joernott.c0depaint.core.canvas.Canvas;
import de.joernott.c0depaint.core.canvas.Limits;
import de.joernott.c0depaint.core.canvas.Point;
import de.joernott.c0depaint.core.config.PaintConfig;
import de.joernott.c0depaint.core.transform.Job;
import de.joernott.c0depaint.core.library.PaintWorld;
import de.joernott.c0depaint.core.style.Weighted;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

/** The world of Minecraft for c0de_paint/cpaint, through the process host of c0de. */
public final class MinecraftWorld implements PaintWorld {
    /** A block position of the program: the level and the position. */
    public record Place(ServerLevel level, BlockPos pos) {}

    @Override
    public List<Weighted> inventoryBlocks(NativeCall call, StructValue position) {
        Place place = place(call, position);
        if (!(place.level().getBlockEntity(place.pos()) instanceof Container container)) {
            throw C0deException.of(
                    ErrorCode.INVALID_ARGUMENT, "The block at " + place.pos().toShortString() + " has no inventory");
        }
        Map<String, Long> counts = new LinkedHashMap<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()
                    && stack.getItem() instanceof BlockItem item
                    && !item.getBlock().defaultBlockState().is(Blocks.AIR)) {
                counts.merge(BuiltInRegistries.BLOCK.getKey(item.getBlock()).toString(), (long) stack.getCount(), Long::sum);
            }
        }
        List<Weighted> blocks = new ArrayList<>();
        counts.forEach((id, count) -> blocks.add(new Weighted(new Block(id), (int) Math.min(count, Integer.MAX_VALUE))));
        return blocks;
    }

    /**
     * The block if it has every state with its value, else null. Blocks that Minecraft doesn't know stay as they are
     * transformed; rendering reports them.
     */
    @Override
    public Block valid(Block block) {
        ResourceLocation id = ResourceLocation.tryParse(block.id());
        if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
            return block;
        }
        StateDefinition<net.minecraft.world.level.block.Block, BlockState> states =
                registered(id).getStateDefinition();
        for (Map.Entry<String, String> state : block.properties().entrySet()) {
            Property<?> property = states.getProperty(state.getKey());
            if (property == null || property.getValue(state.getValue()).isEmpty()) {
                return null;
            }
        }
        return block;
    }

    /** The block with all its states, the defaults filled in (task 9.1.3). */
    @Override
    public Block complete(Block block) {
        ResourceLocation id = ResourceLocation.tryParse(block.id());
        if (id == null || !BuiltInRegistries.BLOCK.containsKey(id) || valid(block) == null) {
            return block;
        }
        try {
            return block(Renderer.state(block)).withData(block.data());
        } catch (RuntimeException e) {
            return block;
        }
    }

    /** The items of a block for CANVAS_ITEMS, from the same mapping as RENDER_CANVAS (task 9.3). */
    @Override
    public Map<String, Long> items(Block block) {
        MinecraftSchematics server = new MinecraftSchematics();
        if (block.isAir() || server.part(block)) {
            return Map.of();
        }
        Map<String, Long> items = server.items(block);
        return items.isEmpty() ? Map.of("", 1L) : items;
    }

    @Override
    public Job render(NativeCall call, Canvas canvas, StructValue position, PaintConfig settings) {
        return Renderer.start(call, canvas, position, settings);
    }

    /**
     * CANVAS_FROM_WORLD: the blocks with all their states and the data of their block entities (since 0.2.0).
     * Loads the chunks of the region that aren't loaded (task 9.9); raises CANVAS_LIMIT if the
     * region is larger than a canvas may be.
     */
    @Override
    public Canvas fromWorld(NativeCall call, StructValue from, StructValue to, boolean air, Limits limits) {
        Place start = area(call, from);
        Place end = area(call, to);
        if (start.level() != end.level()) {
            throw C0deException.of(ErrorCode.INVALID_ARGUMENT, "from and to lie in different dimensions");
        }
        BlockPos min = new BlockPos(
                Math.min(start.pos().getX(), end.pos().getX()),
                Math.min(start.pos().getY(), end.pos().getY()),
                Math.min(start.pos().getZ(), end.pos().getZ()));
        BlockPos max = new BlockPos(
                Math.max(start.pos().getX(), end.pos().getX()),
                Math.max(start.pos().getY(), end.pos().getY()),
                Math.max(start.pos().getZ(), end.pos().getZ()));
        // The voxels of the corners, relative to from
        limits.check(new Point(
                min.getX() - start.pos().getX(), min.getY() - start.pos().getY(), min.getZ() - start.pos().getZ()));
        limits.check(new Point(
                max.getX() - start.pos().getX(), max.getY() - start.pos().getY(), max.getZ() - start.pos().getZ()));
        ServerLevel level = start.level();
        for (int cx = min.getX() >> 4; cx <= max.getX() >> 4; cx++) {
            for (int cz = min.getZ() >> 4; cz <= max.getZ() >> 4; cz++) {
                BlockPos corner = new BlockPos(cx << 4, min.getY(), cz << 4);
                if (!Chunks.ensureLoaded(level, corner)) {
                    throw C0deException.of(
                            ErrorCode.CHUNK_NOT_LOADED, "The chunk of " + corner.toShortString() + " is not loaded");
                }
            }
        }
        Canvas canvas = new Canvas();
        Map<BlockState, Block> blocks = new java.util.HashMap<>();
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() && !air) {
                continue;
            }
            Block block = state.isAir() ? Block.AIR : blocks.computeIfAbsent(state, MinecraftWorld::block);
            // The data of the block entity, as GET_BLOCK of c0de shows it (task 9.2)
            net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                net.minecraft.nbt.CompoundTag data = blockEntity.saveWithoutMetadata(level.registryAccess());
                if (!de.joernott.c0de.compat.Nbt.keys(data).isEmpty()) {
                    block = block.withData(data.toString());
                }
            }
            canvas.set(
                    pos.getX() - start.pos().getX(),
                    pos.getY() - start.pos().getY(),
                    pos.getZ() - start.pos().getZ(),
                    block);
        }
        return canvas;
    }

    /** The block of a canvas for a block state, with all its states. */
    static Block block(BlockState state) {
        Map<String, String> properties = new java.util.TreeMap<>();
        for (Property<?> property : state.getProperties()) {
            properties.put(property.getName(), valueName(state, property));
        }
        return Block.of(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(), properties);
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    /** The registered block, or air. */
    public static net.minecraft.world.level.block.Block registered(ResourceLocation id) {
        //? if >=1.21.2 {
        /*return BuiltInRegistries.BLOCK.getValue(id);
        *///?} else {
        return BuiltInRegistries.BLOCK.get(id);
        //?}
    }

    /** The process host of the calling process, or INVALID_ARGUMENT outside of the game. */
    public static ProcessHost host(NativeCall call) {
        if (call.process().host() instanceof ProcessHost host) {
            return host;
        }
        throw C0deException.of(ErrorCode.INVALID_ARGUMENT, "c0de_paint/cpaint needs a process of the game");
    }

    /**
     * The level and position of a POSITION, as in c0de/minecraft: an empty dimension is the one of the trigger block.
     * Raises CHUNK_NOT_LOADED if its chunk is not loaded, because functions never load chunks.
     */
    public static Place place(NativeCall call, StructValue position) {
        Place place = area(call, position);
        if (!place.level().isLoaded(place.pos())) {
            throw C0deException.of(
                    ErrorCode.CHUNK_NOT_LOADED, "The chunk of " + place.pos().toShortString() + " is not loaded");
        }
        return place;
    }

    /**
     * The level and position of a POSITION in an area that RENDER_CANVAS builds or CANVAS_FROM_WORLD copies, whose
     * chunks are loaded for them ({@link Chunks}, task 9.9).
     */
    static Place area(NativeCall call, StructValue position) {
        ProcessHost host = host(call);
        String dimension = ((StringValue) position.get("dimension")).value();
        ServerLevel level;
        if (dimension.isEmpty()) {
            level = host.level();
        } else {
            ResourceLocation id = ResourceLocation.tryParse(dimension);
            level = id == null ? null : host.server().getLevel(ResourceKey.create(Registries.DIMENSION, id));
        }
        if (level == null) {
            throw C0deException.of(ErrorCode.INVALID_ARGUMENT, "There is no dimension " + dimension);
        }
        BlockPos pos = new BlockPos(
                (int) ((IntegerValue) position.get("x")).value(),
                (int) ((IntegerValue) position.get("y")).value(),
                (int) ((IntegerValue) position.get("z")).value());
        return new Place(level, pos);
    }
}
