package com.imguns.guns.inventory.tooltip;

import com.imguns.guns.api.item.IGun;
import com.imguns.guns.resource.index.CommonGunIndex;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class GunTooltip implements TooltipData {
    private final ItemStack gun;
    private final IGun iGun;
    private final Identifier ammoId;
    private final CommonGunIndex gunIndex;

    public GunTooltip(ItemStack gun, IGun iGun, Identifier ammoId, CommonGunIndex gunIndex) {
        this.gun = gun;
        this.iGun = iGun;
        this.ammoId = ammoId;
        this.gunIndex = gunIndex;
    }

    public ItemStack getGun() {
        return gun;
    }

    public IGun getIGun() {
        return iGun;
    }

    public Identifier getAmmoId() {
        return ammoId;
    }

    public CommonGunIndex getGunIndex() {
        return gunIndex;
    }
}
