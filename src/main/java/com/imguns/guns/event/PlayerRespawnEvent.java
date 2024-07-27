package com.imguns.guns.event;

import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.api.entity.IGunOperator;
import com.imguns.guns.api.item.gun.AbstractGunItem;
import com.imguns.guns.config.common.GunConfig;
import com.imguns.guns.entity.shooter.LivingEntityReload;
import com.imguns.guns.util.AttachmentDataUtils;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerRespawnEvent {

    public static void afterRespawn(ServerPlayerEntity ignoredDeadPlayer, ServerPlayerEntity player, boolean ignoredAlive) {
        if (!GunConfig.AUTO_RELOAD_WHEN_RESPAWN.get()) {
            return;
        }
        player.getInventory().main.forEach(currentGunItem -> {
            if (!(currentGunItem.getItem() instanceof AbstractGunItem iGun)) {
                return;
            }
            TimelessAPI.getCommonGunIndex(iGun.getGunId(currentGunItem)).ifPresent(gunIndex -> {
                int currentAmmoCount = iGun.getCurrentAmmoCount(currentGunItem);
                int maxAmmoCount = AttachmentDataUtils.getAmmoCountWithAttachment(currentGunItem, gunIndex.getGunData());
                if (IGunOperator.fromLivingEntity(player).needCheckAmmo() && !LivingEntityReload.inventoryHasAmmo(player, currentAmmoCount, maxAmmoCount, currentGunItem, iGun)) {
                    return;
                }
                iGun.reloadAmmo(currentGunItem, LivingEntityReload.getAndExtractNeedAmmoCount(player, currentGunItem, iGun, maxAmmoCount), false);
            });
        });
    }
}
