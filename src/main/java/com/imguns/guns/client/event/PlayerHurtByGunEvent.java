package com.imguns.guns.client.event;

import com.imguns.guns.api.LogicalSide;
import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.api.event.common.EntityHurtByGunEvent;
import com.imguns.guns.client.renderer.other.GunHurtBobTweak;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class PlayerHurtByGunEvent {

    public static void onPlayerHurtByGun(EntityHurtByGunEvent event) {
        LogicalSide logicalSide = event.getLogicalSide();
        if (logicalSide != LogicalSide.CLIENT) {
            return;
        }
        Entity hurtEntity = event.getHurtEntity();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        // 当受伤的是自己的时候，触发受伤晃动的调整参数
        if (player != null && player.equals(hurtEntity)) {
            Identifier gunId = event.getGunId();
            TimelessAPI.getCommonGunIndex(gunId).ifPresent(index -> {
                float tweakMultiplier = index.getGunData().getHurtBobTweakMultiplier();
                GunHurtBobTweak.markTimestamp(tweakMultiplier);
            });
        }
    }
}
