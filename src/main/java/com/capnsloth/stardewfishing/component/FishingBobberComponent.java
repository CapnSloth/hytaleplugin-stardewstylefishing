package com.capnsloth.stardewfishing.component;

import com.capnsloth.stardewfishing.StardewStyleFishing;
import com.google.crypto.tink.subtle.Random;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.physics.SimplePhysicsProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.UUID;

public class FishingBobberComponent implements Component<EntityStore> {
    // Internal use:
    public boolean isFishOn = false;
    public boolean isInWater = false;
    public float wetLifetime = 0f; // The seconds for which the bobber has spent in the water.
    public float fightProgress = 25f; // The progress to successful catch. Success when progress is at 100.
    public float hookAtTime = 10f; // Randomised time at which fish will be hooked.
    public SimplePhysicsProvider physicsProvider;
    public UUID ownerID;

    // Config:
    public float maxHookTime = 10f; // The longest that it can take to hook a fish in seconds.
    public float fishEscapeRate = 10f; // The progress lost per-second that the fish is not in the catch bar.
    public float fishReelRate = 10f; // The progress gain per second when the fish is inside catch bar.


    public boolean checkSuccess(){
        return fightProgress >= 100f;
    }
    public void resetBobber(){
        isFishOn = false;
        isInWater = false;
        wetLifetime = 0f;
        fightProgress = 25f;
        hookAtTime = (float)Random.randInt((int)(maxHookTime * 100))/100;
    }

    @Override
    public @Nullable Component<EntityStore> clone() {
        FishingBobberComponent clone = new FishingBobberComponent();
        clone.isFishOn = this.isFishOn;
        clone.isInWater = this.isFishOn;
        clone.wetLifetime = this.wetLifetime;
        clone.maxHookTime = this.maxHookTime;
        clone.fightProgress = this.fightProgress;
        clone.fishEscapeRate = this.fishEscapeRate;
        clone.fishReelRate = this.fishReelRate;
        clone.hookAtTime = this.hookAtTime;
        return clone;
    }

    @Nonnull
    public static ComponentType<EntityStore, FishingBobberComponent> getComponentType() {
        return StardewStyleFishing.bobberComponent;
    }

}
