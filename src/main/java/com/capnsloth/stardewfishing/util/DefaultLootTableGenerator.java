package com.capnsloth.stardewfishing.util;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.npc.util.InventoryHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DefaultLootTableGenerator {
    private static final Random RANDOM = new Random();

    public static Map<String, Float> GENERIC(){
        Map<String, Float> fishTable = new HashMap();
        fishTable.put("Fish_Bluegill_Item", 5.0F);
        fishTable.put("Fish_Catfish_Item", 5.0F);
        fishTable.put("Fish_Minnow_Item", 5.0F);
        fishTable.put("Fish_Tang_Blue_Item", 3.0F);
        fishTable.put("Fish_Tang_Chevron_Item", 3.0F);
        fishTable.put("Fish_Tang_Lemon_Peel_Item", 3.0F);
        fishTable.put("Fish_Tang_Sailfin_Item", 3.0F);
        fishTable.put("Fish_Clownfish_Item", 1.5F);
        fishTable.put("Fish_Pufferfish_Item", 1.5F);
        fishTable.put("Fish_Trout_Rainbow_Item", 1.5F);
        fishTable.put("Fish_Salmon_Item", 1.5F);
        fishTable.put("Fish_Jellyfish_Blue_Item", 0.75F);
        fishTable.put("Fish_Jellyfish_Cyan_Item", 0.75F);
        fishTable.put("Fish_Jellyfish_Green_Item", 0.75F);
        fishTable.put("Fish_Jellyfish_Red_Item", 0.75F);
        fishTable.put("Fish_Jellyfish_Yellow_Item", 0.75F);
        fishTable.put("Fish_Jellyfish_Man_Of_War_Item", 0.25F);
        fishTable.put("Fish_Crab_Item", 0.25F);
        fishTable.put("Fish_Eel_Moray_Item", 0.25F);
        fishTable.put("Fish_Frostgill_Item", 0.25F);
        fishTable.put("Fish_Lobster_Item", 0.25F);
        fishTable.put("Fish_Pike_Item", 0.25F);
        fishTable.put("Fish_Piranha_Black_Item", 0.25F);
        fishTable.put("Fish_Piranha_Item", 0.25F);
        fishTable.put("Fish_Shark_Hammerhead_Item", 5.0F);
        fishTable.put("Fish_Shellfish_Lava_Item", 0.25F);
        fishTable.put("Fish_Snapjaw_Item", 0.25F);
        fishTable.put("Fish_Trilobite_Black_Item", 0.25F);
        fishTable.put("Fish_Trilobite_Item", 0.25F);
        fishTable.put("Fish_Whale_Humpback_Item", 0.25F);
        return fishTable;
    }

    public static String getRandomFish(Map<String, Float> lootTable) {
        if (lootTable.isEmpty()) return "";

        float totalWeight = 0.0F;
        for (Float w : lootTable.values()) {
            totalWeight += w;
        }

        float r = RANDOM.nextFloat() * totalWeight;
        for (Map.Entry<String, Float> entry : lootTable.entrySet()) {
            r -= entry.getValue();
            if (r <= 0.0F) {
                return entry.getKey();
            }
        }

        // Fallback (shouldn't normally reach here due to floating point)
        return lootTable.keySet().stream().findFirst().orElse(null);
    }

    public static ItemStack createRandomFish(Map<String, Float> lootTable) {
        String fishId = getRandomFish(lootTable);
        ItemStack fishStack = ItemStack.EMPTY;
        if (fishId.isEmpty()) {
            return fishStack;
        }
        fishStack = InventoryHelper.createItem(fishId);
        if (fishStack == null) {
            return ItemStack.EMPTY;
        }
        return fishStack;
    }
}
