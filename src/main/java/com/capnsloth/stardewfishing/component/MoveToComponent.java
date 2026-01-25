package com.capnsloth.stardewfishing.component;

import com.capnsloth.stardewfishing.StardewStyleFishing;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class MoveToComponent implements Component<EntityStore> {

    public Vector3d targetPos;
    public float speed = 1f;



    @Override
    public @Nullable Component<EntityStore> clone() {
        MoveToComponent mc = new MoveToComponent();
        mc.targetPos = this.targetPos;
        mc.speed = this.speed;
        return mc;
    }

    @Nonnull
    public static ComponentType<EntityStore, MoveToComponent> getComponentType() {
        return StardewStyleFishing.moveToComponent;
    }
}
