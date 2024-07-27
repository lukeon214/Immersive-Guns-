package com.imguns.guns.client.resource.pojo.animation.bedrock;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BedrockAnimation {
    @SerializedName("loop")
    private boolean loop;

    @SerializedName("animation_length")
    private double animationLength;

    @SerializedName("bones")
    @Nullable
    private Map<String, AnimationBone> bones;

    @SerializedName("sound_effects")
    private SoundEffectKeyframes soundEffects;

    public boolean isLoop() {
        return loop;
    }

    public double getAnimationLength() {
        return animationLength;
    }

    @Nullable
    public Map<String, AnimationBone> getBones() {
        return bones;
    }

    public SoundEffectKeyframes getSoundEffects() {
        return soundEffects;
    }
}
