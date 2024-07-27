package com.imguns.guns.client.resource.pojo.animation.bedrock;

import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import net.minecraft.util.Identifier;

public class SoundEffectKeyframes {
    private final Double2ObjectRBTreeMap<Identifier> keyframes;

    public SoundEffectKeyframes(Double2ObjectRBTreeMap<Identifier> keyframes) {
        this.keyframes = keyframes;
    }

    public Double2ObjectRBTreeMap<Identifier> getKeyframes() {
        return keyframes;
    }
}
