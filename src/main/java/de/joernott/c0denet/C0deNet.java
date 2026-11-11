package de.joernott.c0denet;

import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0denet.core.config.NetConfig;
import de.joernott.c0denet.http.PendingRequests;
import de.joernott.c0denet.library.HttpLibrary;
import de.joernott.c0denet.network.NetworkChannel;
import java.io.IOException;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loader-independent initialization of c0de net. The entry points of the loaders call {@link #init()}. */
public final class C0deNet {
    public static final String MOD_ID = "c0de_net";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    /** The version of the mod (mod.version in stonecutter.properties.toml). */
    public static final String VERSION = /*$ mod_version*/ "0.1.0";
    /** The built-in constant with the version of the mod (task 6.5). */
    public static final String VERSION_CONSTANT = "C0DE_NET_VERSION";

    private C0deNet() {}

    /**
     * Loads the server settings, registers the payloads, the library c0de_net/http and the built-in constant
     * C0DE_NET_VERSION with c0de, which is a required dependency and always loaded.
     */
    public static void init() {
        try {
            NetConfig config =
                    NetConfig.load(Platform.INSTANCE.configDir().resolve(NetConfig.SERVER_FILE), NetConfig.SERVER);
            config.problems().forEach(LOGGER::warn);
            PendingRequests.config(config);
        } catch (IOException e) {
            LOGGER.error("Can't read {}, using the defaults", NetConfig.SERVER_FILE, e);
        }
        NetworkChannel.register();
        PendingRequests.channel(new NetworkChannel());
        Platform.INSTANCE.onServerTickEnd(PendingRequests::tick);
        ModLibraries.register(new HttpLibrary(PendingRequests::call));
        ModLibraries.registerConstant(VERSION_CONSTANT, new StringValue(VERSION));
    }

    /** An identifier in the namespace of the mod. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
