package com.capnsloth.stardewfishing.interaction;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.UUID;

public class RodItemMetadata {
    // Define metadata values.
    public UUID bobberUUID;

    // Define codec containing item metadata keys and values.
    public static final String KEY = "SSF_BoundBobber";
    public static final BuilderCodec<RodItemMetadata> CODEC = BuilderCodec.builder(RodItemMetadata.class, RodItemMetadata::new)
            .append(new KeyedCodec<>("BobberUUID", Codec.UUID_BINARY), // Append item metadata as keyed codec.
                    (metadata, value) -> metadata.bobberUUID = value,
                    (metadata) -> metadata.bobberUUID
            ).documentation("The bobber owned by this item")
            .add().build();

    public static final KeyedCodec<RodItemMetadata> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);
}
