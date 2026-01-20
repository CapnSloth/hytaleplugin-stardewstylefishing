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
    public float fightProgress = 25f; // The progress to successful catch. Success when progress is at 100.
    public float fishPos = 0f; // The position of the fish in the bar.
    public float barPos = 0f; // The position of the catch bar.
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


    // Config:
    public float maxHookTime = 10f; // The longest that it can take to hook a fish in seconds.
    public float fishEscapeRate = 10f; // The progress lost per-second that the fish is not in the catch bar.
    public float fishReelRate = 10f; // The progress gain per second when the fish is inside catch bar.
    public float barRadius = 20f; // The size of half the bar, used to check if bar is over the fish.
    public float fishMaxVeocity = 40f; // The maximum speed of the fish.
    public double minigameModelVerticalOffset = 1f; // The height above bobber to display the minigame elements.
    public float castCooldown = 1f; // Seconds before rod can be cast or reeled.
    public float barGravity = 45f; // How fast the bar falls when not being risen. Should be close to fish max velocity.
    public float barSpeed = 45f; // How fast the bar rises when right click is held. Should be faster than fish max velocity.


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
        clone.barGravity = this.barGravity;
        clone.barSpeed = this.barSpeed;
        clone.lastInteractionTime = this.lastInteractionTime;
        clone.rodItemStackSlot = this.rodItemStackSlot;
        return clone;
    }

    @Nonnull
    public static ComponentType<EntityStore, FishingBobberComponent> getComponentType() {
        return StardewStyleFishing.bobberComponent;
    }


}
