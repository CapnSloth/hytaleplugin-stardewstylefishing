package com.capnsloth.stardewfishing.interaction;

import com.capnsloth.stardewfishing.component.FishingBobberComponent;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.projectile.config.Projectile;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.physics.SimplePhysicsProvider;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class UseFishingRodInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<UseFishingRodInteraction> CODEC;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final float ROD_CAST_COOLDOWN = 0.1f;


    static {
        CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(UseFishingRodInteraction.class, UseFishingRodInteraction::new, SimpleInstantInteraction.CODEC)
                .documentation("Casts or reels in a bobber when right-clicked."))
                .build();
    }
    @Override
    protected void firstRun(@NonNull InteractionType interactionType, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler) {
        //LOGGER.atInfo().log("Used Rod");

        // Check that everything is all good.
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        if (commandBuffer == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        World world = context.getEntity().getStore().getExternalData().getWorld();
        Store<EntityStore> store = world.getEntityStore().getStore();

        // Get the held item which should be the fishing rod.
        ItemStack heldItem = context.getHeldItem();
        if (heldItem == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        // Get the player using the item.
        Ref<EntityStore> userRef = context.getEntity();
        Player player = commandBuffer.getComponent(userRef, Player.getComponentType());

        // Check rod metadata for bobber.
        RodItemMetadata metadata = heldItem.getFromMetadataOrNull(RodItemMetadata.KEY, RodItemMetadata.CODEC);
        if(metadata == null) {
            metadata = updateLastInteractionTimeMetadata(player.getInventory(), context.getHeldItemSlot());
        }

        // Set up references to the bobber.
        Ref<EntityStore> bobberRef = null;
        FishingBobberComponent bobberComponent;

        // Check if a bobber exists.
        if (metadata.bobberUUID != null &&
                world.getEntityRef(metadata.bobberUUID) != null &&
                store.getComponent(world.getEntityRef(metadata.bobberUUID), FishingBobberComponent.getComponentType()) != null) {

            // ---- Bobber exists in the world, reel it in or do minigame logic depending on state. ---

            bobberRef = world.getEntityRef(metadata.bobberUUID);
            bobberComponent = store.getComponent(bobberRef, FishingBobberComponent.getComponentType());

            // If in minigame then right click moves bar up, else reel in.
            if (bobberComponent.isFishOn) {

                //Move bar up.
                bobberComponent.barPos = Math.clamp(bobberComponent.barPos + ((bobberComponent.barSpeed + bobberComponent.barGravity) * bobberComponent.approxBobberSystemDeltaTime), 0f, 100f);

                //TimeUnit.SECONDS.convert(System.nanoTime() - metadata.lastCastOrReelTime, TimeUnit.NANOSECONDS) >= 0.2f
            } else if (metadata.castState == 1 && metadata.canCast(ROD_CAST_COOLDOWN)) { // Reel in and remove bobber.
                reelRod(player, world);
            }

        }
        else if (metadata.canCast(ROD_CAST_COOLDOWN)){

            castRod(store, player, world);

        }




    }


    public static void reelRod(Player player, World world){
        LOGGER.atInfo().log("Reeling in");

        removeAndDespawnBobber(player.getInventory(), player.getInventory().getActiveHotbarSlot(), world);

        // Store the current time as the lastInteractionTime on the rod.
        updateLastInteractionTimeMetadata(player.getInventory(), player.getInventory().getActiveHotbarSlot());
        setCastState(0, player.getInventory(), player.getInventory().getActiveHotbarSlot());
    }

    public static void castRod(Store<EntityStore> store, Player player, World world){
        LOGGER.atInfo().log("Casting out");
        // Run bobber spawning code inside the world execution queue.
        Ref<EntityStore> userRef = player.getReference();
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder(); // A "holder" is the entity which holds the components.

        // Attach transform component.
        TransformComponent transform = store.getComponent(userRef, EntityModule.get().getTransformComponentType()).clone(); //Positioning component.
        transform.setPosition(transform.getPosition().add(new Vector3d(0, 1.5, 0)));
        holder.addComponent(TransformComponent.getComponentType(), transform); // (Type of component to add, actual component).
        Vector3d userLook = store.getComponent(userRef, EntityModule.get().getHeadRotationComponentType()).getDirection();

        // Attach velocity and apply initial force based on user look direction.
        holder.addComponent(PhysicsValues.getComponentType(), new PhysicsValues());
        //Velocity velocity = new Velocity(userLook.normalize().scale(3.0));
        Velocity velocity = new Velocity(userLook.scale(10));
        holder.addComponent(Velocity.getComponentType(), velocity);

        //Attach model and bounding box components.
        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Bobber_Red");
        if (modelAsset == null) modelAsset = ModelAsset.DEBUG;
        Model model = Model.createScaledModel(modelAsset, 1.0f);
        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));

        // Attach network component.
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));


        //Set up bobber variables.
        holder.addComponent(FishingBobberComponent.getComponentType(), new FishingBobberComponent());
        FishingBobberComponent bobberComponent = holder.getComponent(FishingBobberComponent.getComponentType());
        bobberComponent.resetBobber();
        bobberComponent.ownerID = store.getComponent(userRef, UUIDComponent.getComponentType()).getUuid();
        bobberComponent.rodItemStack = player.getInventory().getActiveHotbarItem();

        // Set and initialise physics.
        SimplePhysicsProvider simplePhysicsProvider = new SimplePhysicsProvider(UseFishingRodInteraction::bounceHandler, UseFishingRodInteraction::impactHandler);
        simplePhysicsProvider.initialize(Projectile.getAssetMap().getAsset("Projectile_Bobber"), holder.getComponent(BoundingBox.getComponentType()));
        bobberComponent.physicsProvider = simplePhysicsProvider;

        // Ensure critical components.
        holder.ensureComponent(UUIDComponent.getComponentType());
        holder.ensureComponent(FishingBobberComponent.getComponentType());

        // Replace fishing rod with a copy containing bobber metadata and set it to cast mode.
        addBobberToItemMetadata(player.getInventory(), player.getInventory().getActiveHotbarSlot(), holder.getComponent(UUIDComponent.getComponentType()).getUuid());
        //bobberComponent.stateTrigger = FishingBobberComponent.Trigger.CAST;

        // Add cast metadata to rod.
        updateLastInteractionTimeMetadata(player.getInventory(), player.getInventory().getActiveHotbarSlot());
        setCastState(1, player.getInventory(), player.getInventory().getActiveHotbarSlot());

        // Finally, queue up spawning of the entity by adding it to the world entity store.
        world.execute(() -> {
            store.addEntity(holder, AddReason.SPAWN);
        });

    }

    protected static void addBobberToItemMetadata(Inventory inventory, short hotbarSlot, UUID bobberUUID){
        // Get existing rod and its metadata.
        ItemStack oldRod = inventory.getHotbar().getItemStack(hotbarSlot);
        RodItemMetadata metadata = oldRod.getFromMetadataOrNull(RodItemMetadata.KEY, RodItemMetadata.CODEC);
        if(metadata == null) metadata = new RodItemMetadata();

        // Modify metadata.
        metadata.bobberUUID = bobberUUID;

        // Replace rod.
        ItemStack newRod = oldRod.withMetadata(RodItemMetadata.KEYED_CODEC, metadata);
        inventory.getHotbar().replaceItemStackInSlot(hotbarSlot, oldRod, newRod);
    }
    protected static RodItemMetadata updateLastInteractionTimeMetadata(Inventory inventory, short hotbarSlot){
        // Get existing rod and its metadata.
        ItemStack oldRod = inventory.getHotbar().getItemStack(hotbarSlot);
        RodItemMetadata metadata = oldRod.getFromMetadataOrNull(RodItemMetadata.KEY, RodItemMetadata.CODEC);
        if(metadata == null) metadata = new RodItemMetadata();

        // Modify metadata.
        metadata.lastCastOrReelTime = System.nanoTime();

        // Replace rod.
        ItemStack newRod = oldRod.withMetadata(RodItemMetadata.KEYED_CODEC, metadata);
        inventory.getHotbar().replaceItemStackInSlot(hotbarSlot, oldRod, newRod);

        return metadata;
    }

    protected static RodItemMetadata toggleCastState(Inventory inventory, short hotbarSlot){
        // Get existing rod and its metadata.
        ItemStack oldRod = inventory.getHotbar().getItemStack(hotbarSlot);
        RodItemMetadata metadata = oldRod.getFromMetadataOrNull(RodItemMetadata.KEY, RodItemMetadata.CODEC);
        if(metadata == null) metadata = new RodItemMetadata();

        // Modify metadata.
        if(metadata.castState == 0){metadata.castState = 1;}
        else if(metadata.castState == 1){ metadata.castState = 0;}

        // Replace rod.
        ItemStack newRod = oldRod.withMetadata(RodItemMetadata.KEYED_CODEC, metadata);
        inventory.getHotbar().replaceItemStackInSlot(hotbarSlot, oldRod, newRod);

        return metadata;
    }

    protected static RodItemMetadata setCastState(int state, Inventory inventory, short hotbarSlot){
        // Get existing rod and its metadata.
        ItemStack oldRod = inventory.getHotbar().getItemStack(hotbarSlot);
        RodItemMetadata metadata = oldRod.getFromMetadataOrNull(RodItemMetadata.KEY, RodItemMetadata.CODEC);
        if(metadata == null) metadata = new RodItemMetadata();

        // Modify metadata.
        metadata.castState = state;

        // Replace rod.
        ItemStack newRod = oldRod.withMetadata(RodItemMetadata.KEYED_CODEC, metadata);
        inventory.getHotbar().replaceItemStackInSlot(hotbarSlot, oldRod, newRod);

        return metadata;
    }

    protected static void removeAndDespawnBobber(Inventory inventory, short hotbarSlot, World world){
        ItemStack oldRod = inventory.getHotbar().getItemStack(hotbarSlot);
        RodItemMetadata metadata = oldRod.getFromMetadataOrNull(RodItemMetadata.KEY, RodItemMetadata.CODEC);
        Store<EntityStore> store = world.getEntityStore().getStore();
        if(metadata != null) {
            Ref<EntityStore> bobberRef = world.getEntityRef(metadata.bobberUUID);
            if(bobberRef == null) return; // If bobber doesn't exist, then nothing to do.
            FishingBobberComponent bobber = store.getComponent(bobberRef, FishingBobberComponent.getComponentType());

            // Attempt to despawn additional models.
            if(bobber != null) {
                if(bobber.minigameFishModelId != null) {
                    Ref<EntityStore> fishModelRef = world.getEntityRef(bobber.minigameFishModelId);
                    if (fishModelRef != null) {
                        world.execute(() -> {
                            store.removeEntity(fishModelRef, RemoveReason.REMOVE);
                        });
                    }
                }

                if(bobber.minigameBarModelId != null) {
                    Ref<EntityStore> barModelRef = world.getEntityRef(bobber.minigameBarModelId);
                    if (barModelRef != null) {
                        world.execute(() -> {
                            store.removeEntity(barModelRef, RemoveReason.REMOVE);
                        });
                    }
                }
            }

            // Despawn bobber.
            world.execute(() -> {
                store.removeEntity(bobberRef, RemoveReason.REMOVE);
            });


        }

        // Replace rod with new rod of empty metadata.
        ItemStack newRod = oldRod.withMetadata(RodItemMetadata.KEY, null); // Make metadata empty.
        inventory.getHotbar().replaceItemStackInSlot(hotbarSlot, oldRod, newRod);
    }


    protected static void bounceHandler(@Nonnull Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
        /*
        if (this.lastBouncePosition == null) {
            this.lastBouncePosition = new Vector3d(position);
        } else {
            if (!(this.lastBouncePosition.distanceSquaredTo(position) >= 0.5)) {
                return;
            }

            this.lastBouncePosition.assign(position);
        }

        this.onProjectileBounce(position, componentAccessor);

         */
    }

    protected static void impactHandler(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Vector3d position,
            @Nullable Ref<EntityStore> targetRef,
            @Nonnull ComponentAccessor<EntityStore> componentAccessor
    ) {
        /*
        if (targetRef != null) {
            this.onProjectileHitEvent(ref, position, targetRef, componentAccessor);
        } else {
            this.onProjectileMissEvent(position, componentAccessor);
        }

         */
    }

    /*
    public void shoot(@Nonnull Holder<EntityStore> holder, @Nonnull UUID creatorUuid, double x, double y, double z, float yaw, float pitch) {
        this.creatorUuid = creatorUuid;
        this.simplePhysicsProvider.setCreatorId(creatorUuid);
        Vector3d direction = new Vector3d();
        computeStartOffset(
                this.projectile.isPitchAdjustShot(),
                this.projectile.getVerticalCenterShot(),
                this.projectile.getHorizontalCenterShot(),
                this.projectile.getDepthShot(),
                yaw,
                pitch,
                direction
        );
        x += direction.x;
        y += direction.y;
        z += direction.z;
        holder.ensureAndGetComponent(TransformComponent.getComponentType()).setPosition(new Vector3d(x, y, z));
        PhysicsMath.vectorFromAngles(yaw, pitch, direction);
        direction.setLength(this.projectile.getMuzzleVelocity());
        this.simplePhysicsProvider.setVelocity(direction);
    }

     */

}


