package de.joernott.c0denet.platform.fabric;

//? if fabric {
import de.joernott.c0denet.C0deNet;
import net.fabricmc.api.ModInitializer;

/** Entry point for Fabric. */
public class C0deNetFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        C0deNet.init();
    }
}
//?}
