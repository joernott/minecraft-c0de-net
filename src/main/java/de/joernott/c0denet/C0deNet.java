package de.joernott.c0denet;

import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0denet.library.HttpLibrary;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loader-independent initialization of c0de net. The entry points of the loaders call {@link #init()}. */
public final class C0deNet {
    public static final String MOD_ID = "c0de_net";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private C0deNet() {}

    /** Registers the library c0de_net/http with c0de, which is a required dependency and always loaded. */
    public static void init() {
        ModLibraries.register(new HttpLibrary());
    }

    /** An identifier in the namespace of the mod. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
