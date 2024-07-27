package com.imguns.guns.event;

import com.imguns.guns.api.entity.KnockBackModifier;
import net.minecraft.entity.player.PlayerEntity;

public class KnockbackChange {

    public static double onKnockback(double in_strength, PlayerEntity player) {
        KnockBackModifier modifier = KnockBackModifier.fromLivingEntity(player);
        double strength = modifier.getKnockBackStrength();
        if (strength >= 0) {
            return strength;
        }
        return in_strength;
    }
}
