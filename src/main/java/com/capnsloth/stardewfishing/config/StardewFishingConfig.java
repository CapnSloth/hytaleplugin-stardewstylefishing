package com.capnsloth.stardewfishing.config;

import com.capnsloth.stardewfishing.StardewStyleFishing;
import com.capnsloth.stardewfishing.util.DefaultLootTableGenerator;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;

import java.util.HashMap;
import java.util.Map;

public class StardewFishingConfig{
    public static final BuilderCodec<StardewFishingConfig> CODEC;
    public Map<String, Float> lootTable_generic = DefaultLootTableGenerator.GENERIC();
    public int minFishingTime = 100;
    public int maxFishingTime = 600;

    // Builds the codec for plugin configuration.
    static {
        var codecBuilder = BuilderCodec.builder(StardewFishingConfig.class, StardewFishingConfig::new);

        // Add a new key and value to the config.
        codecBuilder.append(new KeyedCodec("FishingLoot_Generic", new MapCodec(Codec.FLOAT, HashMap::new)),
                (config, map) -> config.lootTable_generic = map,
                (config) -> config.lootTable_generic).documentation("A table of fish IDs and their corresponding weights for fishing.")
                .add();

        codecBuilder.append(new KeyedCodec("MinFishingTime", Codec.INTEGER),
                (config, value) -> config.minFishingTime = value,
                (config) -> config.minFishingTime).documentation("The minimum time (in ticks) required for a fish to hook onto the line. [default: 100]")
                .add();

        codecBuilder.append(new KeyedCodec("MaxFishingTime", Codec.INTEGER),
                (config, value) -> config.maxFishingTime = value,
                (config) -> config.maxFishingTime).documentation("The maximum time (in ticks) before a fish must hook onto the line. [default: 600]")
                .add();

        // Build and set the codec.
        CODEC = codecBuilder.build();
    }
}
