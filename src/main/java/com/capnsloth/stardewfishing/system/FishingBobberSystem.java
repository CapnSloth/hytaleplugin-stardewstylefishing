package com.capnsloth.stardewfishing.system;

import com.capnsloth.stardewfishing.component.FishingBobberComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Random;
import java.util.UUID;

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

        switch (bobber.stateTrigger){
            case BITE:
                bobber.stateTrigger = FishingBobberComponent.Trigger.NOTRIGGER;
                break;
            case MINIGAME:
                spawnMinigameModels(bobber, ref, store);
                bobber.stateTrigger = FishingBobberComponent.Trigger.NOTRIGGER;
                break;
            case FISHMOVE:
                bobber.nextFishMoveTime = new Random().nextFloat() * 3f;
                bobber.fishMoveTimer = 0f;
                bobber.fishVelocity = (bobber.fishMaxVeocity*-1f) + new Random().nextFloat() * (bobber.fishMaxVeocity - (bobber.fishMaxVeocity*-1f));
                bobber.stateTrigger = FishingBobberComponent.Trigger.NOTRIGGER;
                break;
            case FAIL:
                bobber.stateTrigger = FishingBobberComponent.Trigger.NOTRIGGER;
                break;
            case SUCCESS:
                bobber.stateTrigger = FishingBobberComponent.Trigger.NOTRIGGER;
                break;
        }



        // Handle fishing process.
        if(bobber.isFishOn){
            // Do minigame logic.
            //LOGGER.atInfo().log("derp");
            // Check if fish will change velocity or direction.
            if(bobber.fishMoveTimer >= bobber.nextFishMoveTime){
                bobber.stateTrigger = FishingBobberComponent.Trigger.FISHMOVE;
                LOGGER.atInfo().log("Fish changing velocity.");
            }

            // Apply fish movement.
            bobber.fishPos = Math.clamp(bobber.fishPos + bobber.fishVelocity, 0f, 100f);
            //LOGGER.atInfo().log("Fish pos = %s", bobber.fishPos);
            updateMinigameModelPositions(bobber, ref, store);
            bobber.fishMoveTimer += deltaTime;
        }else{
            if(bobber.isInWater){
                // Do waiting for bite logic.
                if(bobber.wetLifetime >= bobber.hookAtTime){
                    bobber.isFishOn = true;
                    bobber.stateTrigger = FishingBobberComponent.Trigger.MINIGAME;
                }

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

    protected void spawnMinigameModels(FishingBobberComponent bobber, Ref<EntityStore> bobberRef, Store<EntityStore> store){
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        UUID fishModelId = UUID.randomUUID();
        holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(fishModelId));
        bobber.minigameFishModelId = fishModelId;

        // Assign transform to minigame and move it above the bobber.
        holder.addComponent(TransformComponent.getComponentType(), new TransformComponent());
        holder.getComponent(TransformComponent.getComponentType()).setPosition(
                store.getComponent(bobberRef, TransformComponent.getComponentType())
                        .getPosition().add(new Vector3d(0,bobber.minigameModelVerticalOffset,0)));
        // Add model.
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Bobber_Red");
        if (modelAsset == null) modelAsset = ModelAsset.DEBUG;
        Model model = Model.createScaledModel(modelAsset, 1.0f);
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));

        // Spawn the model in the world.
        World world = store.getExternalData().getWorld();
        world.execute(() -> {
            store.addEntity(holder, AddReason.SPAWN);
            LOGGER.atInfo().log("Spawned fish model at: %s", holder.getComponent(TransformComponent.getComponentType()).getPosition());
        });

    }

    protected void updateMinigameModelPositions(FishingBobberComponent bobber, Ref<EntityStore> bobberRef, Store<EntityStore> store){
        Ref<EntityStore> fishModelRef = store.getExternalData().getWorld().getEntityRef(bobber.minigameFishModelId);
        if(fishModelRef == null) return;

        Vector3d newPos = store.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition();
        newPos = newPos.add(new Vector3d(0,bobber.minigameModelVerticalOffset + (bobber.fishPos / 100f),0));


        store.getComponent(fishModelRef, TransformComponent.getComponentType()).setPosition(newPos);

        LOGGER.atInfo().log("Fish pos = %s", newPos);
    }

}
