package de.joernott.c0denet.platform.fabric;

//? if fabric {
import de.joernott.c0denet.client.ClientRequests;
import net.fabricmc.api.ClientModInitializer;

/** Client entry point for Fabric. */
public class C0deNetFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientRequests.init();
    }
}
//?}
