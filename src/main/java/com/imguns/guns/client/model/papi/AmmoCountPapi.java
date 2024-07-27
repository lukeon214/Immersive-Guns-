package com.imguns.guns.client.model.papi;

import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.api.item.IGun;
import com.imguns.guns.client.resource.index.ClientGunIndex;
import com.imguns.guns.resource.pojo.data.gun.Bolt;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class AmmoCountPapi implements Function<ItemStack, String> {
    public static final String NAME = "ammo_count";

    @Override
    public String apply(ItemStack stack) {
        IGun iGun = IGun.getIGunOrNull(stack);
        if (iGun != null) {
            Identifier gunId = iGun.getGunId(stack);
            ClientGunIndex gunIndex = TimelessAPI.getClientGunIndex(gunId).orElse(null);
            if (gunIndex == null) {
                return "";
            }
            int ammoCount = iGun.getCurrentAmmoCount(stack) + (iGun.hasBulletInBarrel(stack) && gunIndex.getGunData().getBolt() != Bolt.OPEN_BOLT ? 1 : 0);
            return "" + ammoCount;
        }
        return "";
    }
}
