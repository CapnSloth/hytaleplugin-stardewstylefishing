package com.capnsloth.stardewfishing.component;

import com.capnsloth.stardewfishing.StardewStyleFishing;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.inventory.ItemStack;
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
    public float fightProgress = 0.25f; // The progress to successful catch. Success when progress is at 1f.
    public float fishPos = 0f; // The position of the fish in the bar as a scale from 0 - 1.
    public float barPos = 0f; // The position of the catch bar.
    public float barVelocity = 0f; // The current direction and speed of the fishing bar.
    public float nextFishMoveTime = 0.5f; // The time until the next fish movement.
    public float fishMoveTimer = 0f; // Counts up until next fish move.
    public float fishVelocity = 0f; // The movement of the fish.
    public float hookAtTime = 10f; // Randomised time at which fish will be hooked.
    public SimplePhysicsProvider physicsProvider;
    public UUID ownerID;
    public UUID selfUUID;
    public byte rodItemStackSlot; // The inventory slot in which the bobber rod exists.
    public ItemStack rodItemStack; // Both these rod item vars are used to ensure player doesn't move it or their active hotbar slot.
    public enum Trigger {NOTRIGGER, BITE, MINIGAME, FISHMOVE, SUCCESS, FAIL}
    public Trigger stateTrigger = Trigger.NOTRIGGER;
    public UUID minigameFishModelId;
    public UUID minigameBarModelId;
    public float bobberAge = 0f;
    public long lastInteractionTime = 0;
    public float approxBobberSystemDeltaTime = 1f; // The last known delta time of the bobber system tick.
    public float minigameScale = 2f; // The visual size of the minigame display, adjusted based on distance from bobber.


    // Config:
    public float maxHookTime = 6f; // The longest that it can take to hook a fish in seconds.
    public float fishEscapeRate = 0.3333f; // The progress lost per-second that the fish is not in the catch bar.
    public float fishReelRate = 0.2f; // The progress gain per second when the fish is inside catch bar.
    public float barRadius = 0.1f; // The size of half the bar, used to check if bar is over the fish.
    public float fishMaxVeocity = 0.8f; // The maximum speed of the fish.
    public double minigameModelVerticalOffset = 1f; // The height above bobber to display the minigame elements.
    public float minigameScaleMin = 1.5f; // Minigame minimum size.
    public float minigameScaleMax = 14f; // Minigame maximum size.
    public float minigameScaleMultiplier = 1f; // minigameScale = distance from player * minigame scale multiplier.
    public float castCooldown = 0.5f; // Seconds before rod can be cast or reeled.
    public float barGravity = 0.7f; // How fast the bar falls when not being risen. Should be close to fish max velocity.
    public float barSpeed = 1.0f; // How fast the bar rises when right click is held. Should be faster than fish max velocity.
    public float barAcceleration = 0.25f; // The rate at which the bar accelerates multiplied by the speed of its direction.
    public double waterFriction = 0.85; // The amount by which the bobber model is slowed down while in water.


    //public boolean checkSuccess(){
    //    return fightProgress >= 100f;
    //}

    public void resetBobber(){
        isFishOn = false;
        isInWater = false;
        wetLifetime = 0f;
        fightProgress = 0.25f;
        hookAtTime = new Random().nextFloat() * maxHookTime;
        stateTrigger = Trigger.NOTRIGGER;
        fishPos = 0f;
        barPos = 0f;
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
        clone.minigameBarModelId = this.minigameBarModelId;
        clone.bobberAge = this.bobberAge;
        clone.castCooldown = this.castCooldown;
        clone.lastInteractionTime = this.lastInteractionTime;
        clone.rodItemStackSlot = this.rodItemStackSlot;
        clone.minigameScale = this.minigameScale;
        clone.barVelocity = this.barVelocity;
        return clone;
    }

    @Nonnull
    public static ComponentType<EntityStore, FishingBobberComponent> getComponentType() {
        return StardewStyleFishing.bobberComponent;
    }


}
