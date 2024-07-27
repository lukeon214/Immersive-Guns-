package com.imguns.guns.client.event;

import com.imguns.guns.api.LogicalSide;
import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.api.event.common.EntityHurtByGunEvent;
import com.imguns.guns.api.event.common.EntityKillByGunEvent;
import com.imguns.guns.client.gui.overlay.KillAmountOverlay;
import com.imguns.guns.client.sound.SoundPlayManager;
import com.imguns.guns.config.client.RenderConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ClientHitMark {
    public static long lastHitTimestamp = 0;
    public static float damageAmount = 0;

    public static void onEntityHurtByGun(EntityHurtByGunEvent event) {
        LogicalSide logicalSide = event.getLogicalSide();
        if (logicalSide != LogicalSide.CLIENT) {
            return;
        }
        LivingEntity attacker = event.getAttacker();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        Entity hurtEntity = event.getHurtEntity();
        if (player != null && player.equals(attacker) && hurtEntity!=null) {
            Identifier gunId = event.getGunId();
            RenderCrosshairEvent.markHitTimestamp();
            if (event.isHeadShot()) {
                RenderCrosshairEvent.markHeadShotTimestamp();
                TimelessAPI.getClientGunIndex(gunId).ifPresent(index -> SoundPlayManager.playHeadHitSound(player, index));
            } else {
                TimelessAPI.getClientGunIndex(gunId).ifPresent(index -> SoundPlayManager.playFleshHitSound(player, index));
            }
        }
    }

    public static void onEntityKillByGun(EntityKillByGunEvent event) {
        LogicalSide logicalSide = event.getLogicalSide();
        if (logicalSide != LogicalSide.CLIENT) {
            return;
        }
        LivingEntity attacker = event.getAttacker();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && player.equals(attacker)) {
            RenderCrosshairEvent.markKillTimestamp();
            KillAmountOverlay.markTimestamp();
            TimelessAPI.getClientGunIndex(event.getGunId()).ifPresent(index -> SoundPlayManager.playKillSound(player, index));
            if (event.isHeadShot()) {
                RenderCrosshairEvent.markHeadShotTimestamp();
            }
        }
    }
}
