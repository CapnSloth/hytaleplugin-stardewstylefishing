package com.capnsloth.stardewfishing.util;

import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Interpolation;
import com.hypixel.hytale.procedurallib.logic.GeneralNoise;

public class MathHelpers {

    public static float linearEaseIn(float current, float target, float step){
        float result = current;
        float dist = target - current;
        if(dist <= step) return current;
        result += dist * step;

        return result;
    }
}
