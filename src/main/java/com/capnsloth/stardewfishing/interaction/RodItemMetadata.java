package com.capnsloth.stardewfishing.interaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RodItemMetadata {
    // Define metadata values.
    public UUID bobberUUID = null;
    public long lastCastOrReelTime = System.nanoTime();
    public int castState = 0;

    // Define codec containing item metadata keys and values.
    public static final String KEY = "SSF_BoundBobber";
    public static final BuilderCodec<RodItemMetadata> CODEC = BuilderCodec.builder(RodItemMetadata.class, RodItemMetadata::new)
            .append(new KeyedCodec<>("BobberUUID", Codec.UUID_BINARY), // Append item metadata as keyed codec.
                    (metadata, value) -> metadata.bobberUUID = value, // Setter.
                    (metadata) -> metadata.bobberUUID) // Getter
            .documentation("The bobber owned by this item")
            .add()
            .append(new KeyedCodec<>("LastInteractionTime", Codec.LONG),
                    (metadata, value) -> metadata.lastCastOrReelTime = value,
                    (metadata) -> metadata.lastCastOrReelTime)
            .documentation("The last Instant.now.nanotime that this item was used")
            .add()
            .append(new KeyedCodec<>("CastState", Codec.INTEGER),
                    (metadata, value) -> metadata.castState = value,
                    (metadata) -> metadata.castState)
            .documentation("Cast state: 0 = not cast, 1 = cast, 2 = in the shadow realm???")
            .add()
            .build();

    public static final KeyedCodec<RodItemMetadata> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);

    public boolean canCast(float cooldownTime){
        return TimeUnit.SECONDS.convert(System.nanoTime() - lastCastOrReelTime, TimeUnit.NANOSECONDS) >= cooldownTime;
    }
}
