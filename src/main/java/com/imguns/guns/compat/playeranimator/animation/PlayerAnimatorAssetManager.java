package com.imguns.guns.compat.playeranimator.animation;

import com.google.common.collect.Maps;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum PlayerAnimatorAssetManager {
    INSTANCE;

    private final HashMap<Identifier, HashMap<String, KeyframeAnimation>> animations = new HashMap<>();

    void putAnimation(Identifier id, InputStream stream) throws IOException {
        List<KeyframeAnimation> keyframeAnimations = AnimationSerializing.deserializeAnimation(stream);
        for (var animation : keyframeAnimations) {
            if (animation.extraData.get("name") instanceof String text) {
                String name = PlayerAnimationRegistry.serializeTextToString(text).toLowerCase(Locale.ENGLISH);
                animations.computeIfAbsent(id, k -> Maps.newHashMap()).put(name, animation);
            }
        }
    }

    Optional<KeyframeAnimation> getAnimations(Identifier id, String name) {
        var animationHashMap = this.animations.get(id);
        if (animationHashMap == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(animationHashMap.get(name));
    }

    public boolean containsKey(Identifier id) {
        return animations.containsKey(id);
    }

    public void clearAll() {
        animations.clear();
    }
}