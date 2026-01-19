package com.capnsloth.stardewfishing.interaction;

import com.capnsloth.stardewfishing.component.FishingBobberComponent;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.projectile.config.Projectile;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
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
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class UseFishingRodInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<UseFishingRodInteraction> CODEC;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();


    static {
        CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(UseFishingRodInteraction.class, UseFishingRodInteraction::new, SimpleInstantInteraction.CODEC)
                .documentation("Casts or reels in a bobber when right-clicked."))
                .build();
    }
    @Override
    protected void firstRun(@NonNull InteractionType interactionType, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler) {
        LOGGER.atInfo().log("Used Rod");


        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        if (commandBuffer == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        //World world = ((EntityStore)commandBuffer.getExternalData()).getWorld();
        World world = context.getEntity().getStore().getExternalData().getWorld();
        Store<EntityStore> store = world.getEntityStore().getStore();


        ItemStack heldItem = context.getHeldItem();
        if (heldItem == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        Ref<EntityStore> userRef = context.getEntity();
        //PlayerRef playerRef = commandBuffer.getComponent(userRef, PlayerRef.getComponentType());
        Player player = commandBuffer.getComponent(userRef, Player.getComponentType());

        // Check rod metadata for bobber.
        RodItemMetadata metadata = heldItem.getFromMetadataOrNull(RodItemMetadata.KEY, RodItemMetadata.CODEC);
        if (metadata != null && metadata.bobberUUID != null) { // Bobber exists in the world, reel it in or do minigame logic depending on state.

            Ref<EntityStore> bobberRef = world.getEntityRef(metadata.bobberUUID);
            if(bobberRef == null) return;
            FishingBobberComponent bobberComponent = store.getComponent(bobberRef, FishingBobberComponent.getComponentType());


            // If in minigame then right click moves bar up, else reel in.
            if(bobberComponent!=null && bobberComponent.isFishOn){
                // Set cooldown to instant while in minigame.

                //Move bar up.
                LOGGER.atInfo().log("Moving fishing bar up");

            }else { // Reel in and remove bobber.
                LOGGER.atInfo().log("Reeling in");
                world.execute(() -> {
                    removeAndDespawnBobber(player.getInventory(), context.getHeldItemSlot(), world);
                });
            }

        } else {

            LOGGER.atInfo().log("Casting out");
            // Run bobber spawning code inside the world execution queue.

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

            // Ensure critical components.
            holder.ensureComponent(UUIDComponent.getComponentType());
            holder.ensureComponent(FishingBobberComponent.getComponentType());

            //Set up bobber variables.
            FishingBobberComponent bobberComponent = holder.getComponent(FishingBobberComponent.getComponentType());
            bobberComponent.resetBobber();
            bobberComponent.ownerID = store.getComponent(userRef, UUIDComponent.getComponentType()).getUuid();

            // Set and initialise physics.
            SimplePhysicsProvider simplePhysicsProvider = new SimplePhysicsProvider(this::bounceHandler, this::impactHandler);
            simplePhysicsProvider.initialize(Projectile.getAssetMap().getAsset("Projectile_Bobber"), holder.getComponent(BoundingBox.getComponentType()));
            bobberComponent.physicsProvider = simplePhysicsProvider;

            // Replace fishing rod with a copy containing bobber metadata and set it to cast mode.
            addBobberToItemMetadata(player.getInventory(), context.getHeldItemSlot(), holder.getComponent(UUIDComponent.getComponentType()).getUuid());
            //bobberComponent.stateTrigger = FishingBobberComponent.Trigger.CAST;

            // Finally, queue up spawning of the entity by adding it to the world entity store.
            world.execute(() -> {store.addEntity(holder, AddReason.SPAWN); });



        }

    }

    protected void addBobberToItemMetadata(Inventory inventory, short hotbarSlot, UUID bobberUUID){
        ItemStack oldRod = inventory.getHotbar().getItemStack(hotbarSlot);
        RodItemMetadata metadata = new RodItemMetadata();
        metadata.bobberUUID = bobberUUID;
        ItemStack newRod = oldRod.withMetadata(RodItemMetadata.KEYED_CODEC, metadata);
        inventory.getHotbar().replaceItemStackInSlot(hotbarSlot, oldRod, newRod);
    }
    protected void removeAndDespawnBobber(Inventory inventory, short hotbarSlot, World world){
        ItemStack oldRod = inventory.getHotbar().getItemStack(hotbarSlot);
        RodItemMetadata metadata = oldRod.getFromMetadataOrNull(RodItemMetadata.KEY, RodItemMetadata.CODEC);
        if(metadata != null) {
            UUID bobberUUID = metadata.bobberUUID;
            // Despawn bobber.
            world.getEntityStore().getStore().removeEntity(world.getEntityRef(bobberUUID), RemoveReason.REMOVE);
        }

        // Replace rod with new rod of empty metadata.
        ItemStack newRod = oldRod.withMetadata(RodItemMetadata.KEY, null); // Make metadata empty.
        inventory.getHotbar().replaceItemStackInSlot(hotbarSlot, oldRod, newRod);
    }


    protected void bounceHandler(@Nonnull Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
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

    protected void impactHandler(
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


