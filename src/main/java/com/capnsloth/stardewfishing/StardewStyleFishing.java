package com.capnsloth.stardewfishing;

import com.capnsloth.stardewfishing.component.FishingBobberComponent;
import com.capnsloth.stardewfishing.interaction.UseFishingRodInteraction;
import com.capnsloth.stardewfishing.system.FishingBobberSystem;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class StardewStyleFishing extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static ComponentType<EntityStore, FishingBobberComponent> bobberComponent;

    public StardewStyleFishing(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        getCommandRegistry().registerCommand(new ExampleCommand(this.getName(), this.getManifest().getVersion().toString()));

        registerComponents();
        registerInteractions();
        registerSystems();
    }


    protected void registerComponents(){
        bobberComponent = getEntityStoreRegistry().registerComponent(FishingBobberComponent.class, FishingBobberComponent::new);
    }

    protected void registerInteractions(){
        getCodecRegistry(Interaction.CODEC).register("UseFishingRod", UseFishingRodInteraction.class, UseFishingRodInteraction.CODEC);
    }

    protected void registerSystems(){
        getEntityStoreRegistry().registerSystem(new FishingBobberSystem());
    }
}
