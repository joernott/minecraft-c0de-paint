package de.joernott.c0depaint.platform.fabric;

//? if fabric {
import de.joernott.c0depaint.C0dePaint;
import net.fabricmc.api.ModInitializer;

/** Entry point for Fabric. */
public class C0dePaintFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        C0dePaint.init();
    }
}
//?}
