package de.joernott.c0denet.platform.neoforge;

//? if neoforge {
/*import de.joernott.c0denet.C0deNet;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// Entry point for NeoForge
@Mod(C0deNet.MOD_ID)
public class C0deNetNeoForge {
    public C0deNetNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        C0deNet.init();
        if (de.joernott.c0de.platform.Platform.INSTANCE.isClient()) {
            de.joernott.c0denet.client.ClientRequests.init();
        }
        //? if >=1.21.11 {
        /^de.joernott.c0denet.gametest.neoforge.GameTestRegistration.register(modEventBus);
        ^///?}
    }
}
*///?}
