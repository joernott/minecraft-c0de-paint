package de.joernott.c0depaint.world;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.library.ProcessHost;
import de.joernott.c0depaint.core.canvas.Block;
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
        if (!level.isLoaded(pos)) {
            throw C0deException.of(ErrorCode.CHUNK_NOT_LOADED, "The chunk of " + pos.toShortString() + " is not loaded");
        }
        return new Place(level, pos);
    }
}
