package de.joernott.c0depaint.platform.neoforge;

//? if neoforge {
/*import de.joernott.c0depaint.C0dePaint;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// Entry point for NeoForge
@Mod(C0dePaint.MOD_ID)
public class C0dePaintNeoForge {
    public C0dePaintNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        C0dePaint.init();
        //? if >=1.21.11 {
        /^de.joernott.c0depaint.gametest.neoforge.GameTestRegistration.register(modEventBus);
        ^///?}
    }
}
*///?}
