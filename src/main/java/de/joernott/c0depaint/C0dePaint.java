package de.joernott.c0depaint;

import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0depaint.core.config.PaintConfig;
import de.joernott.c0depaint.core.library.CpaintLibrary;
import de.joernott.c0depaint.world.MinecraftWorld;
import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loader-independent initialization of c0de paint. The entry points of the loaders call {@link #init()}. */
public final class C0dePaint {
    public static final String MOD_ID = "c0de_paint";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    /** The version of the mod (mod.version in stonecutter.properties.toml). */
    public static final String VERSION = /*$ mod_version*/"0.2.0";
    /** The built-in constant with the version of the mod, like C0DE_NET_VERSION (task 9.1.7). */
    public static final String VERSION_CONSTANT = "C0DE_PAINT_VERSION";

    private static volatile PaintConfig config = PaintConfig.defaults(PaintConfig.SERVER);

    private C0dePaint() {}

    /**
     * Loads the settings of the server and registers the libraries c0de_paint/cpaint and c0de_paint/schematics
     * with c0de, which is a required dependency and always loaded first.
     */
    public static void init() {
        try {
            config = PaintConfig.load(Platform.INSTANCE.configDir().resolve(PaintConfig.SERVER_FILE), PaintConfig.SERVER);
            config.problems().forEach(LOGGER::warn);
        } catch (IOException e) {
            LOGGER.error("Can't read {}, using the defaults", PaintConfig.SERVER_FILE, e);
        }
        ModLibraries.register(new CpaintLibrary(C0dePaint::config, new MinecraftWorld()));
        ModLibraries.registerConstant(VERSION_CONSTANT, new de.joernott.c0de.core.value.StringValue(VERSION));
        // c0de_paint/schematics reads the schematics on the clients of the players (task 9.5)
        de.joernott.c0depaint.network.SchematicChannel.register();
        de.joernott.c0depaint.network.PendingFiles.channel(new de.joernott.c0depaint.network.SchematicChannel());
        Platform.INSTANCE.onServerTickEnd(de.joernott.c0depaint.network.PendingFiles::tick);
        ModLibraries.register(new de.joernott.c0depaint.core.library.SchematicsLibrary(
                C0dePaint::config,
                new de.joernott.c0depaint.network.PendingFiles(),
                new de.joernott.c0depaint.world.MinecraftSchematics()));
    }

    /** The settings of the server. */
    public static PaintConfig config() {
        return config;
    }

    /** An identifier in the namespace of the mod. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
