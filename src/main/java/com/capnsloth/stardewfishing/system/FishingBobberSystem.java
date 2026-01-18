package com.capnsloth.stardewfishing.system;

import com.capnsloth.stardewfishing.component.FishingBobberComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

// Handles the tick processsing of bobber components in the world.
public class FishingBobberSystem extends EntityTickingSystem<EntityStore> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    // Query checks which entities to include in system processing.
    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return FishingBobberComponent.getComponentType();
    }

    // Processes entities which made it through query, every game tick.
    @Override
    public void tick(float deltaTime, int index, @NonNull ArchetypeChunk<EntityStore> chunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        FishingBobberComponent bobber = store.getComponent(ref, FishingBobberComponent.getComponentType());
        Velocity bobberVelocity = store.getComponent(ref, Velocity.getComponentType());

        bobberVelocity.addForce(new Vector3d(0.5,0.5,0.5));

        if(bobber.isFishOn){
            // Do minigame logic.

        }else{
            if(bobber.isInWater){
                // Do waiting for bite logic.

            }else{
                // Do casting logic.
                // Check if current occupied space is a water block.

                // Do cast physics.
                bobberVelocity.addForce(new Vector3d(0.5,0.5,0.5));
            }

        }

        // Run down bobber timer.
        bobber.wetLifetime += deltaTime;



    }

}
