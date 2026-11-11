package de.joernott.c0denet.runner;

import de.joernott.c0de.core.library.LibraryProvider;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0denet.library.HttpLibrary;

/**
 * Adds c0de_net/http and C0DE_NET_VERSION to the program runner of c0de (task 6.1). The runner finds this provider
 * through META-INF/services when c0de-net-runner-<version>.jar is on its class path. The requests are sent directly
 * by the runner ({@link DirectRequests}), not through the client of a player.
 */
public final class NetRunnerLibraries implements LibraryProvider {
    /** The built-in constant with the version of c0de net, like C0deNet.VERSION_CONSTANT of the mod. */
    public static final String VERSION_CONSTANT = "C0DE_NET_VERSION";

    @Override
    public void register(LibraryRegistry registry) {
        registry.add(new HttpLibrary(DirectRequests::call));
        registry.addConstant(VERSION_CONSTANT, new StringValue(version()));
    }

    /**
     * The version of c0de net: from the manifest of the jar, else from the system property {@code c0denet.version}
     * (the tests), else "dev".
     */
    public static String version() {
        String version = NetRunnerLibraries.class.getPackage().getImplementationVersion();
        if (version == null || version.isBlank()) {
            version = System.getProperty("c0denet.version", "dev");
        }
        return version;
    }
}
