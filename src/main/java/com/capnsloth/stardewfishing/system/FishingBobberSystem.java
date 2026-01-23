package com.capnsloth.stardewfishing.system;

import com.capnsloth.stardewfishing.component.FishingBobberComponent;
import com.capnsloth.stardewfishing.interaction.RodItemMetadata;
import com.capnsloth.stardewfishing.interaction.UseFishingRodInteraction;
import com.capnsloth.stardewfishing.util.DefaultLootTableGenerator;
import com.capnsloth.stardewfishing.util.HelperTransforms;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Animation;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.*;
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

        // Check if fishing rod is no longer active item and remove bobber if so.
        Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(bobber.ownerID);
        if(playerRef != null){
            Player player = store.getComponent(playerRef, Player.getComponentType());
            if(player != null) {
                Inventory inventory = player.getInventory();
                // Check if active hotbar slot changed.
                if(inventory.getActiveHotbarSlot() != bobber.rodItemStackSlot){
                    UseFishingRodInteraction.reelRod(player, store.getExternalData().getWorld(), bobber);
                    LOGGER.atInfo().log("Active hotbar slot changed.");
                // Check if rod is no longer in active slot.
                }else{
                    ItemStack heldItem = inventory.getActiveHotbarItem();
                    if(heldItem != null) {
                        RodItemMetadata metadata = heldItem.getFromMetadataOrNull(RodItemMetadata.KEY, RodItemMetadata.CODEC);
                        if(metadata != null) {
                            if (!metadata.bobberUUID.toString().equals(bobber.selfUUID.toString())){

                                UseFishingRodInteraction.reelRod(player, store.getExternalData().getWorld(), bobber);
                                LOGGER.atInfo().log("Item in hotbar slot changed. %s,  %s", metadata.bobberUUID, bobber.selfUUID);
                            }
                        }
                    }else{
                        UseFishingRodInteraction.reelRod(player, store.getExternalData().getWorld(), bobber);
                        LOGGER.atInfo().log("No item in hotbar slot.");
                    }
                }
            }
        }else{
            // Rod user is missing, remove bobber and minigame.
            UseFishingRodInteraction.removeAndDespawnBobber(bobber, store.getExternalData().getWorld());
        }



        switch (bobber.stateTrigger){
            //case CAST: // Don't revert to NOTRIGGER here as this is used to prevent spam casting in Interaction.
            //    break;
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
                if(bobber.fishPos <= 5) bobber.fishVelocity = Math.abs(bobber.fishVelocity); // Always ensure that fish moves away from edges if near top / bottom.
                if(bobber.fishPos >= 95) bobber.fishVelocity = Math.abs(bobber.fishVelocity) * -1f; //  ^
                bobber.stateTrigger = FishingBobberComponent.Trigger.NOTRIGGER;
                break;
            case FAIL:
                LOGGER.atInfo().log("YOU FAIL");
                // Reel in the rod which the bobber owner is using.
                UseFishingRodInteraction.reelRod(store.getComponent(store.getExternalData().getWorld().getEntityRef(bobber.ownerID), Player.getComponentType()), store.getExternalData().getWorld(), bobber);
                break;
            case SUCCESS:
                LOGGER.atInfo().log("YOU WIN");
                // Spawn fish rewards!
                ItemStack fishStack = DefaultLootTableGenerator.createRandomFish(DefaultLootTableGenerator.GENERIC());
                if (!fishStack.isEmpty()) {
                    ItemUtils.throwItem(store.getExternalData().getWorld().getEntityRef(bobber.ownerID), fishStack, 4.0F, commandBuffer);
                }
                // Reel in the rod which the bobber owner is using.
                UseFishingRodInteraction.reelRod(store.getComponent(store.getExternalData().getWorld().getEntityRef(bobber.ownerID), Player.getComponentType()), store.getExternalData().getWorld(), bobber);
                break;
        }



        // Handle fishing process.
        if(bobber.isFishOn){
            // Do minigame logic.

            // Check if bar is over the fish and check win state.
            //float distance = Math.abs(bobber.fishPos - bobber.barPos);
            //if(distance <= bobber.barRadius){
            if(bobber.fishPos < bobber.barPos + bobber.barRadius && bobber.fishPos > bobber.barPos - bobber.barRadius){
                bobber.fightProgress += bobber.fishReelRate * deltaTime;
                // DEBUG
                //Message message = new Message(new FormattedMessage());
                //message.insert("Yes");
                //store.getComponent(playerRef, Player.getComponentType()).sendMessage(message);
                if(bobber.fightProgress >= 1.0f){
                    bobber.stateTrigger = FishingBobberComponent.Trigger.SUCCESS;
                    return;
                }
            }else{
                bobber.fightProgress -= bobber.fishEscapeRate * deltaTime;
                // DEBUG
                //Message message = new Message(new FormattedMessage());
                //message.insert("Nope");
                //store.getComponent(playerRef, Player.getComponentType()).sendMessage(message);
                if(bobber.fightProgress <= 0f){
                    bobber.stateTrigger = FishingBobberComponent.Trigger.FAIL;
                    return;
                }
            }
            LOGGER.atInfo().log("fight progress: %s", bobber.fightProgress);

            // Check if fish will change velocity or direction.
            if(bobber.fishMoveTimer >= bobber.nextFishMoveTime){
                bobber.stateTrigger = FishingBobberComponent.Trigger.FISHMOVE;
                //LOGGER.atInfo().log("Fish changing velocity.");
            }

            // Apply bar motion. (Rising is computed in UseFishingRodInteraction)
            bobber.barPos = Math.clamp(bobber.barPos - (bobber.barGravity * deltaTime),0f,1.0f);

            // Apply fish movement.
            bobber.fishPos = Math.clamp(bobber.fishPos + (bobber.fishVelocity*deltaTime), 0f, 1.0f);
            //DEBUG
            //bobber.fishPos = 0.5f;

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

        // Handle casting / reeling cooldown.
        //if(bobber.stateTrigger == FishingBobberComponent.Trigger.CAST && bobber.bobberAge >= bobber.castCooldown) bobber.stateTrigger = FishingBobberComponent.Trigger.NOTRIGGER;
        bobber.bobberAge += deltaTime;
        bobber.approxBobberSystemDeltaTime = deltaTime;
    }

    protected void spawnMinigameModels(FishingBobberComponent bobber, Ref<EntityStore> bobberRef, Store<EntityStore> store){

        // ------- FISH MODEL -------------------------------------------------------------------
        Holder<EntityStore> fishModelEntity = EntityStore.REGISTRY.newHolder();
        UUID fishModelId = UUID.randomUUID();
        fishModelEntity.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(fishModelId));
        bobber.minigameFishModelId = fishModelId;

        // Assign transform to minigame and move it above the bobber.
        fishModelEntity.addComponent(TransformComponent.getComponentType(), new TransformComponent());
        Vector3d newPos = store.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();
        newPos = newPos.add(new Vector3d(0,bobber.minigameModelVerticalOffset,0));
        fishModelEntity.getComponent(TransformComponent.getComponentType()).setPosition(newPos);
        HelperTransforms.applyBillboard(fishModelId, bobber.ownerID, new Vector3f(90,0,0), store);

        // Add model.
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("SSF_FishIcon");
        if (modelAsset == null) modelAsset = ModelAsset.DEBUG;
        Model model = Model.createScaledModel(modelAsset, 0.5f * bobber.minigameScale);
        fishModelEntity.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        fishModelEntity.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        fishModelEntity.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));

        // Attach network component.
        fishModelEntity.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));

        // Spawn the model in the world.
        World world = store.getExternalData().getWorld();
        world.execute(() -> {
            store.addEntity(fishModelEntity, AddReason.SPAWN);
            LOGGER.atInfo().log("Spawned fish model at: %s", fishModelEntity.getComponent(TransformComponent.getComponentType()).getPosition());
        });


        //---------- BAR MODEL -----------------------------------------------------
        Holder<EntityStore> barModelEntity = EntityStore.REGISTRY.newHolder();
        UUID barModelEntityId = UUID.randomUUID();
        barModelEntity.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(barModelEntityId));
        bobber.minigameBarModelId = barModelEntityId;

        // Assign transform to minigame and move it above the bobber.
        barModelEntity.addComponent(TransformComponent.getComponentType(), new TransformComponent());
        Vector3d newBarPos = store.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();
        //newBarPos = newBarPos.add(new Vector3d(0,bobber.minigameModelVerticalOffset,0));
        Vector3d playerPos = store.getComponent(store.getExternalData().getRefFromUUID(bobber.ownerID), TransformComponent.getComponentType()).getPosition().clone();
        newBarPos = newBarPos.add(HelperTransforms.moveAwayFrom(newBarPos ,playerPos, 2));
        barModelEntity.getComponent(TransformComponent.getComponentType()).setPosition(newBarPos);
        //HelperTransforms.applyBillboard(barModelEntityId, bobber.ownerID, new Vector3f(90,0,0), store);


        // Add model.
        ModelAsset barModelAsset = ModelAsset.getAssetMap().getAsset("SSF_FishingBar");
        if (barModelAsset == null) barModelAsset = ModelAsset.DEBUG;
        Model barModel = Model.createScaledModel(barModelAsset, (bobber.barRadius * 2f) * bobber.minigameScale);
        barModelEntity.addComponent(PersistentModel.getComponentType(), new PersistentModel(barModel.toReference()));
        barModelEntity.addComponent(ModelComponent.getComponentType(), new ModelComponent(barModel));
        barModelEntity.addComponent(BoundingBox.getComponentType(), new BoundingBox(barModel.getBoundingBox()));



        // Attach network component.
        barModelEntity.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));

        // Spawn the model in the world.
        world.execute(() -> {
            store.addEntity(barModelEntity, AddReason.SPAWN);
            LOGGER.atInfo().log("Spawned bar model at: %s", barModelEntity.getComponent(TransformComponent.getComponentType()).getPosition());
        });

    }

    protected void updateMinigameModelPositions(FishingBobberComponent bobber, Ref<EntityStore> bobberRef, Store<EntityStore> store){
        // Do fish logic.
        Ref<EntityStore> fishModelRef = store.getExternalData().getWorld().getEntityRef(bobber.minigameFishModelId);
        if(fishModelRef == null) return;

        // Do fish model motion.
        Vector3d newFishPos = store.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();

        /*
        // Move fish to player based on progress.
        Ref<EntityStore> playerRef = store.getExternalData().getRefFromUUID(bobber.ownerID);
        if(playerRef != null) {
            Vector3d playerPos = store.getComponent(playerRef, TransformComponent.getComponentType()).getPosition().clone();
            newFishPos = HelperTransforms.moveTowards(newFishPos, playerPos, bobber.fightProgress/100f);
        }

         */

        // Adjust fish height based on minigame fishPos.
        newFishPos = newFishPos.add(new Vector3d(0,bobber.minigameModelVerticalOffset + (bobber.fishPos * bobber.minigameScale),0));



        store.getComponent(fishModelRef, TransformComponent.getComponentType()).setPosition(newFishPos);
        // Do Fish rotation.
        float camOffset =  store.getComponent(store.getExternalData().getRefFromUUID(bobber.ownerID), ModelComponent.getComponentType()).getModel().getEyeHeight();
        Vector3d playerHeadPos = store.getComponent(store.getExternalData().getRefFromUUID(bobber.ownerID), TransformComponent.getComponentType()).getPosition().clone().add(new Vector3d(0, camOffset,0));
        HelperTransforms.applyBillboardYOnly(bobber.minigameFishModelId, newFishPos, playerHeadPos ,new Vector3f(90,0,0), store);


        // Do bar logic.
        Ref<EntityStore> barModelRef = store.getExternalData().getWorld().getEntityRef(bobber.minigameBarModelId);
        if(barModelRef == null) return;

        // Do bar model motion.
        Vector3d newBarPos = store.getComponent(bobberRef, TransformComponent.getComponentType()).getPosition().clone();
        newBarPos = newBarPos.add(new Vector3d(0,bobber.minigameModelVerticalOffset + (bobber.barPos * bobber.minigameScale) ,0));
        Vector3d playerPos = store.getComponent(store.getExternalData().getRefFromUUID(bobber.ownerID), TransformComponent.getComponentType()).getPosition().clone();
        //newBarPos = newBarPos.add(HelperTransforms.moveAwayFrom(newBarPos ,playerPos, 0.2));

        Vector3d layering = newBarPos.clone().add(HelperTransforms.moveAwayFrom(newBarPos.clone() ,playerPos, 0.2));
        newBarPos = new Vector3d(layering.x, newBarPos.y, layering.z);


        store.getComponent(barModelRef, TransformComponent.getComponentType()).setPosition(newBarPos);



        // Do bar rotation.
        HelperTransforms.applyBillboardYOnly(bobber.minigameBarModelId, newBarPos, playerHeadPos, new Vector3f(0,0,0), store);

        //LOGGER.atInfo().log("CamOffset: %s,   PlayerHeadPos: %s",camOffset, playerHeadPos);
        //LOGGER.atInfo().log("GameBarPos = %s,  WorldBarPos = %s", bobber.barPos ,newBarPos);

    }

}
