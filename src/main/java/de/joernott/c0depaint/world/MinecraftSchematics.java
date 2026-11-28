package de.joernott.c0depaint.world;

import com.mojang.serialization.Dynamic;
import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.compat.Nbt;
import de.joernott.c0depaint.core.canvas.Block;
import de.joernott.c0depaint.core.library.SchematicsLibrary;
import de.joernott.c0depaint.core.schematic.Schematic;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

/**
 * What c0de_paint/schematics needs of Minecraft (task 9.6): the data fixer for schematics of older versions, the
 * blocks of the server and the items that build them.
 */
public final class MinecraftSchematics implements SchematicsLibrary.Server {
    private static final MinecraftWorld WORLD = new MinecraftWorld();

    /** The data version of this Minecraft. */
    private static final int CURRENT = Nbt.integer(NbtUtils.addCurrentDataVersion(new CompoundTag()), "DataVersion");

    private final Schematic.Upgrader upgrader = new Schematic.Upgrader() {
        @Override
        public Block upgrade(Block block, int dataVersion) {
            if (dataVersion >= CURRENT) {
                return block;
            }
            CompoundTag state = new CompoundTag();
            state.putString("Name", block.id());
            if (!block.properties().isEmpty()) {
                CompoundTag properties = new CompoundTag();
                block.properties().forEach(properties::putString);
                state.put("Properties", properties);
            }
            Tag fixed = fix(References.BLOCK_STATE, state, dataVersion);
            if (!(fixed instanceof CompoundTag result) || Nbt.string(result, "Name").isEmpty()) {
                de.joernott.c0depaint.C0dePaint.LOGGER.warn(
                        "The data fixer returned {} for {} of data version {}", fixed, state, dataVersion);
                return block;
            }
            Map<String, String> properties = new TreeMap<>();
            CompoundTag fixedProperties = Nbt.compound(result, "Properties");
            for (String key : Nbt.keys(fixedProperties)) {
                properties.put(key, Nbt.string(fixedProperties, key));
            }
            return Block.of(Nbt.string(result, "Name"), properties, block.data());
        }

        @Override
        public Block blockEntity(Block block, Map<String, Object> blockEntity, int dataVersion) {
            if (dataVersion >= CURRENT) {
                return Schematic.Upgrader.super.blockEntity(block, blockEntity, dataVersion);
            }
            CompoundTag tag;
            try {
                tag = Compat.parseCompound(de.joernott.c0depaint.core.schematic.Nbt.snbt(blockEntity));
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
                return Schematic.Upgrader.super.blockEntity(block, blockEntity, dataVersion);
            }
            Tag fixed = fix(References.BLOCK_ENTITY, tag, dataVersion);
            if (!(fixed instanceof CompoundTag result)) {
                return Schematic.Upgrader.super.blockEntity(block, blockEntity, dataVersion);
            }
            for (String key : new String[] {"x", "y", "z", "id"}) {
                result.remove(key);
            }
            return block.withData(Nbt.keys(result).isEmpty() ? "" : result.toString());
        }
    };

    private static Tag fix(com.mojang.datafixers.DSL.TypeReference type, Tag tag, int dataVersion) {
        return DataFixers.getDataFixer()
                .update(type, new Dynamic<>(NbtOps.INSTANCE, tag), dataVersion, CURRENT)
                .getValue();
    }

    @Override
    public Schematic.Upgrader upgrader() {
        return upgrader;
    }

    @Override
    public boolean available(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        return key != null && BuiltInRegistries.BLOCK.containsKey(key);
    }

    @Override
    public Map<String, Long> items(Block block) {
        BlockState state = state(block);
        Map<String, Long> items = new LinkedHashMap<>();
        if (state == null) {
            // A block the server doesn't know: its own id, so the list still names it
            items.put(block.id(), 1L);
            return items;
        }
        for (Map.Entry<Item, Integer> entry : BlockItems.items(state).entrySet()) {
            items.put(BuiltInRegistries.ITEM.getKey(entry.getKey()).toString(), (long) entry.getValue());
        }
        return items;
    }

    @Override
    public boolean part(Block block) {
        BlockState state = state(block);
        return state != null && BlockItems.part(state);
    }

    @Override
    public Block valid(Block block) {
        return WORLD.valid(block);
    }

    /** The block state of a block, or null if the server doesn't know the block or its states. */
    private static BlockState state(Block block) {
        ResourceLocation key = ResourceLocation.tryParse(block.id());
        if (key == null || !BuiltInRegistries.BLOCK.containsKey(key) || WORLD.valid(block) == null) {
            return null;
        }
        try {
            return Renderer.state(block);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
