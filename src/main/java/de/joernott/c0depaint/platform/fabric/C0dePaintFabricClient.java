package de.joernott.c0depaint.platform.fabric;

//? if fabric {
import de.joernott.c0depaint.client.ClientSchematics;
import net.fabricmc.api.ClientModInitializer;

/** Client entry point for Fabric: the client side of c0de_paint/schematics (task 9.5). */
public class C0dePaintFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientSchematics.init();
    }
}
//?}
