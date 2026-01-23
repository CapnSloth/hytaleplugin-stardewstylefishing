package com.capnsloth.stardewfishing.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/// A helper class for position and rotation calculations.
public class HelperTransforms {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static void applyBillboard(UUID looker, UUID lookAtTarget, Vector3f finalRotationAdjustment, Store<EntityStore> store){
        Vector3f newRotation = new Vector3f();

        Ref<EntityStore> lookerRef = store.getExternalData().getRefFromUUID(looker);
        Ref<EntityStore> targetRef = store.getExternalData().getRefFromUUID(lookAtTarget);
        if(lookerRef == null || targetRef == null){
            LOGGER.atWarning().log("Failed to apply billboard. (Missing entity Ref)");
            return;
        }

        Vector3d lookerPos = store.getComponent(lookerRef, TransformComponent.getComponentType()).getPosition();
        Vector3d targetPos = store.getComponent(targetRef, TransformComponent.getComponentType()).getPosition();
        newRotation = billboard(lookerPos, targetPos, finalRotationAdjustment, store);

        if(store.getComponent(lookerRef, HeadRotation.getComponentType()) != null){
            store.getComponent(lookerRef, HeadRotation.getComponentType()).setRotation(newRotation);
        }
        store.getComponent(lookerRef, TransformComponent.getComponentType()).setRotation(newRotation);

    }

    public static void applyBillboard(UUID lookerID, Vector3d lookerPos, Vector3d lookAtTargetPos, Vector3f finalRotationAdjustment, Store<EntityStore> store){
        Vector3f newRotation = new Vector3f();

        Ref<EntityStore> lookerRef = store.getExternalData().getRefFromUUID(lookerID);
        if(lookerRef == null){
            LOGGER.atWarning().log("Failed to apply billboard. (Missing entity Ref)");
            return;
        }

        newRotation = billboard(lookerPos, lookAtTargetPos, finalRotationAdjustment, store);

        if(store.getComponent(lookerRef, HeadRotation.getComponentType()) != null){
            store.getComponent(lookerRef, HeadRotation.getComponentType()).setRotation(newRotation);
        }
        store.getComponent(lookerRef, TransformComponent.getComponentType()).setRotation(newRotation);

    }
    public static void applyBillboardYOnly(UUID lookerID, Vector3d lookerPos, Vector3d lookAtTargetPos, Vector3f finalRotationAdjustment, Store<EntityStore> store){
        Vector3f newRotation = new Vector3f();

        Ref<EntityStore> lookerRef = store.getExternalData().getRefFromUUID(lookerID);
        if(lookerRef == null){
            LOGGER.atWarning().log("Failed to apply billboard. (Missing entity Ref)");
            return;
        }

        newRotation = billboard(lookerPos, lookAtTargetPos, finalRotationAdjustment, store);
        newRotation = new Vector3f(0, newRotation.y, 0);

        if(store.getComponent(lookerRef, HeadRotation.getComponentType()) != null){
            store.getComponent(lookerRef, HeadRotation.getComponentType()).setRotation(newRotation);
        }
        store.getComponent(lookerRef, TransformComponent.getComponentType()).setRotation(newRotation);

    }

    public static Vector3f billboard(Vector3d lookerPos, Vector3d lookAtTargetPos, Vector3f finalRotationAdjustment, Store<EntityStore> store){
        Vector3d directionToPlayer = Vector3d.directionTo(lookerPos, lookAtTargetPos);
        Vector3f fishRotation = Vector3f.lookAt(directionToPlayer);
        fishRotation = fishRotation.add(finalRotationAdjustment);
        return fishRotation;
    }

    public static Vector3d moveBetween(Vector3d startPos, Vector3d endPos, float progress){
        Vector3d direction = Vector3d.directionTo(startPos, endPos);
        double distance = startPos.distanceTo(endPos);
        return direction.scale(distance * progress);
    }

    public static Vector3d moveTowards(Vector3d startPos, Vector3d targetPos, double amount){
        Vector3d direction = Vector3d.directionTo(startPos, targetPos);
        return direction.scale(amount);
    }

    public static Vector3d moveAwayFrom(Vector3d startPos, Vector3d targetPos, double amount){
        return moveTowards(startPos, targetPos, amount).scale(-1);
    }
}
