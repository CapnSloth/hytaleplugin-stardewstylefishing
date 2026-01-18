package com.capnsloth.stardewfishing.interaction;

import com.capnsloth.stardewfishing.component.FishingBobberComponent;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.atomic.AtomicReference;

public class UseFishingRodInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<UseFishingRodInteraction> CODEC;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    static {
        CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(UseFishingRodInteraction.class, UseFishingRodInteraction::new, SimpleInstantInteraction.CODEC)
                .documentation("Spawns or reels in a bobber when right-clicked on a block with a fishing rod"))
                .build();
    }
    @Override
    protected void firstRun(@NonNull InteractionType interactionType, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler) {
        ((HytaleLogger.Api)LOGGER.atInfo()).log("Used Rod");

        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        if (commandBuffer == null) {context.getState().state = InteractionState.Failed; return;}

        //World world = ((EntityStore)commandBuffer.getExternalData()).getWorld();
        World world = context.getEntity().getStore().getExternalData().getWorld();
        Store<EntityStore> store = world.getEntityStore().getStore();

        ItemStack heldItem = context.getHeldItem();
        if(heldItem == null){ context.getState().state = InteractionState.Failed; return;}

        Ref<EntityStore> userRef = context.getEntity();


        // Run bobber spawning code inside the world execution queue.
        world.execute(() -> {
            Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder(); // A "holder" is the entity which holds the components.

            // Attach transform component.
            TransformComponent transform = store.getComponent(userRef, EntityModule.get().getTransformComponentType()); //Positioning component.
            holder.addComponent(TransformComponent.getComponentType(), transform); // (Type of component to add, actual component).

            // Attach velocity and apply initial force based on user look direction.
            holder.addComponent(PhysicsValues.getComponentType(), new PhysicsValues());
            Vector3d userLook = store.getComponent(userRef, EntityModule.get().getHeadRotationComponentType()).getRotation().toVector3d();
            //Velocity velocity = new Velocity(userLook.normalize().scale(3.0));
            Velocity velocity = new Velocity(new Vector3d(1, 1, 1));
            holder.addComponent(Velocity.getComponentType(), velocity);

            //Attach model and bounding box components.
            ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Bobber_Red");
            if(modelAsset == null) modelAsset = ModelAsset.DEBUG;
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
            holder.getComponent(FishingBobberComponent.getComponentType()).resetBobber();

            // Finally, spawn the entity by adding it to the world entity store.
            store.addEntity(holder, AddReason.SPAWN);
        });

    }

}
