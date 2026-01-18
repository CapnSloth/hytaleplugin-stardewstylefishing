package com.capnsloth.stardewfishing;

import com.capnsloth.stardewfishing.interaction.UseFishingRodInteraction;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class StardewStyleFishing extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public StardewStyleFishing(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        this.getCommandRegistry().registerCommand(new ExampleCommand(this.getName(), this.getManifest().getVersion().toString()));

        registerComponents();
        registerInteractions();
        registerSystems();
    }


    protected void registerComponents(){

    }

    protected void registerInteractions(){
        this.getCodecRegistry(Interaction.CODEC).register("UseFishingRod", UseFishingRodInteraction.class, UseFishingRodInteraction.CODEC);
        ((HytaleLogger.Api)LOGGER.atInfo()).log("Registered Interactions");
    }

    protected void registerSystems(){

    }
}
