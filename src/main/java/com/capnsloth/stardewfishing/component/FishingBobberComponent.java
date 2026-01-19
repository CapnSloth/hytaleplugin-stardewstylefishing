package com.capnsloth.stardewfishing.component;

import com.capnsloth.stardewfishing.StardewStyleFishing;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.modules.physics.SimplePhysicsProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.UUID;

public class FishingBobberComponent implements Component<EntityStore> {
    // Internal use:
    public boolean isFishOn = false;
    public boolean isInWater = false;
    public float wetLifetime = 0f; // The seconds for which the bobber has spent in the water.
    public float fightProgress = 25f; // The progress to successful catch. Success when progress is at 100.
    public float fishPos = 0f; // The position of the fish in the bar.
    public float nextFishMoveTime = 0.5f; // The time until the next fish movement.
    public float fishMoveTimer = 0f; // Counts up until next fish move.
    public float fishVelocity = 0f; // The movement of the fish.
    public float hookAtTime = 10f; // Randomised time at which fish will be hooked.
    public SimplePhysicsProvider physicsProvider;
    public UUID ownerID;
    public enum Trigger {NOTRIGGER,CAST, BITE, MINIGAME, FISHMOVE, SUCCESS, FAIL}
    public Trigger stateTrigger = Trigger.NOTRIGGER;
    public UUID minigameFishModelId;
    public float bobberAge = 0f;

    // Config:
    public float maxHookTime = 10f; // The longest that it can take to hook a fish in seconds.
    public float fishEscapeRate = 10f; // The progress lost per-second that the fish is not in the catch bar.
    public float fishReelRate = 10f; // The progress gain per second when the fish is inside catch bar.
    public float barRadius = 10f; // The size of half the bar, used to check if bar is over the fish.
    public float fishMaxVeocity = 5f; // The maximum speed of the fish.
    public double minigameModelVerticalOffset = 1f; // The height above bobber to display the minigame elements.
    public float castCooldown = 1f; // Seconds before rod can be cast or reeled.


    public boolean checkSuccess(){
        return fightProgress >= 100f;
    }
    public void resetBobber(){
        isFishOn = false;
        isInWater = false;
        wetLifetime = 0f;
        fightProgress = 25f;
        hookAtTime = new Random().nextFloat() * maxHookTime;
        stateTrigger = Trigger.NOTRIGGER;
        fishPos = 0f;
        fishVelocity = 0f;
        fishMoveTimer = 0f;
        bobberAge = 0f;
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
        clone.stateTrigger = this.stateTrigger;
        clone.barRadius = this.barRadius;
        clone.fishMaxVeocity = this.fishMaxVeocity;
        clone.minigameModelVerticalOffset = this.minigameModelVerticalOffset;
        clone.minigameFishModelId = this.minigameFishModelId;
        clone.bobberAge = this.bobberAge;
        clone.castCooldown = this.castCooldown;
        return clone;
    }

    @Nonnull
    public static ComponentType<EntityStore, FishingBobberComponent> getComponentType() {
        return StardewStyleFishing.bobberComponent;
    }


}
