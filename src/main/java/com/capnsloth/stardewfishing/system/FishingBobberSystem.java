package com.capnsloth.stardewfishing.system;

import com.capnsloth.stardewfishing.component.FishingBobberComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.SimplePhysicsProvider;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.util.PositionProbeWater;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

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


        if(bobber.isFishOn){
            // Do minigame logic.

        }else{
            if(bobber.isInWater){
                // Do waiting for bite logic.


                // Run down bobber timer.
                bobber.wetLifetime += deltaTime;
            }else{
                // Do casting logic.
                // Check if current occupied space is a water block.
                Vector3i bobberPos = store.getComponent(ref, TransformComponent.getComponentType()).getPosition().toVector3i();
                int occupiedBlockId = store.getExternalData().getWorld().getFluidId(bobberPos.x, bobberPos.y, bobberPos.z);
                if(occupiedBlockId != 0){ // Is any fluid.
                    bobber.isInWater = true;
                }
                //LOGGER.atInfo().log("Bobber in %s at pos %s", BlockType.getAssetMap().getAsset(occupiedBlockId).getGroup(), bobberPos.toString());
                //LOGGER.atInfo().log("BlockId %s  =  %s",occupiedBlockId, BlockType.getAssetMap().getAsset(occupiedBlockId).getId());

                // Do cast physics.
                bobber.physicsProvider.tick(
                        deltaTime,
                        bobberVelocity,
                        store.getExternalData().getWorld(),
                        store.getComponent(ref, TransformComponent.getComponentType()),
                        ref,
                        store
                );


            }

        }


        



    }

}
