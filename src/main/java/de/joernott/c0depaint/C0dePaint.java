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
    public static final String VERSION = /*$ mod_version*/"0.1.0";

    private static volatile PaintConfig config = PaintConfig.defaults(PaintConfig.SERVER);

    private C0dePaint() {}

    /**
     * Loads the settings of the server and registers the library c0de_paint/cpaint with c0de, which is a required
     * dependency and always loaded first.
     */
    public static void init() {
        try {
            config = PaintConfig.load(Platform.INSTANCE.configDir().resolve(PaintConfig.SERVER_FILE), PaintConfig.SERVER);
            config.problems().forEach(LOGGER::warn);
        } catch (IOException e) {
            LOGGER.error("Can't read {}, using the defaults", PaintConfig.SERVER_FILE, e);
        }
        ModLibraries.register(new CpaintLibrary(C0dePaint::config, new MinecraftWorld()));
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
