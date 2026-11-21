package de.joernott.c0depaint.runner;

import de.joernott.c0de.core.library.LibraryProvider;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0depaint.core.config.PaintConfig;
import de.joernott.c0depaint.core.library.CpaintLibrary;
import java.nio.file.Path;

/**
 * Adds c0de_paint/cpaint to the program runner of c0de (task 8.2). The runner finds this provider through
 * META-INF/services when c0de-paint-runner-<version>.jar is on its class path. The settings are the defaults; the
 * world is {@link RunnerWorld}, in which RENDER_CANVAS writes the placed blocks to a YAML file.
 */
public final class PaintRunnerLibraries implements LibraryProvider {
    /** The system property with the file of RENDER_CANVAS; without it, {@link #DEFAULT_FILE} in the current directory. */
    public static final String RENDER_FILE = "c0depaint.render";

    public static final String DEFAULT_FILE = "c0de-paint-render.yaml";

    @Override
    public void register(LibraryRegistry registry) {
        PaintConfig settings = PaintConfig.defaults(PaintConfig.SERVER);
        registry.add(new CpaintLibrary(
                () -> settings, new RunnerWorld(Path.of(System.getProperty(RENDER_FILE, DEFAULT_FILE)))));
    }
}
