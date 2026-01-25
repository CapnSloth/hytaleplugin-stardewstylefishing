package com.capnsloth.stardewfishing.system;

import com.capnsloth.stardewfishing.component.FishingBobberComponent;
import com.capnsloth.stardewfishing.component.MoveToComponent;
import com.capnsloth.stardewfishing.util.TransformHelpers;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class MoveToSystem extends EntityTickingSystem<EntityStore> {
    @Override
    public void tick(float deltaTime, int index, @NonNull ArchetypeChunk<EntityStore> chunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> componentRef = chunk.getReferenceTo(index);
        MoveToComponent mc = store.getComponent(componentRef, MoveToComponent.getComponentType());
        TransformComponent transform = store.getComponent(componentRef, TransformComponent.getComponentType());
        if(transform == null) return;

        // Check if already at position, and remove.
        if(transform.getPosition().distanceTo(mc.targetPos) <= mc.speed){
            transform.setPosition(mc.targetPos);
            store.removeComponent(componentRef, MoveToComponent.getComponentType());
            return;
        }
        // Do movement.
        Vector3d movement = TransformHelpers.moveTowards(transform.getPosition().clone(), mc.targetPos.clone(),mc.speed * deltaTime);
        transform.setPosition(transform.getPosition().clone().add(movement));
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return MoveToComponent.getComponentType();
    }
}
