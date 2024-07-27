package com.imguns.guns.entity.shooter;

import com.imguns.guns.api.LogicalSide;
import com.imguns.guns.api.event.common.GunFireSelectEvent;
import com.imguns.guns.api.item.IGun;
import com.imguns.guns.api.item.gun.AbstractGunItem;
import com.imguns.guns.network.NetworkHandler;
import com.imguns.guns.network.message.event.ServerMessageGunFireSelect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class LivingEntityFireSelect {
    private final LivingEntity shooter;
    private final ShooterDataHolder data;

    public LivingEntityFireSelect(LivingEntity shooter, ShooterDataHolder data) {
        this.shooter = shooter;
        this.data = data;
    }

    public void fireSelect() {
        if (data.currentGunItem == null) {
            return;
        }
        ItemStack currentGunItem = data.currentGunItem.get();
        if (!(currentGunItem.getItem() instanceof IGun iGun)) {
            return;
        }
        if (new GunFireSelectEvent(shooter, currentGunItem, LogicalSide.SERVER).post()) {
            return;
        }
        NetworkHandler.sendToTrackingEntity(new ServerMessageGunFireSelect(shooter.getId(), currentGunItem), shooter);
        if (iGun instanceof AbstractGunItem logicGun) {
            logicGun.fireSelect(currentGunItem);
        }
    }
}
