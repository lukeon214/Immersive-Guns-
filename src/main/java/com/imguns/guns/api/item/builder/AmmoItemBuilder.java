package com.imguns.guns.api.item.builder;

import com.imguns.guns.api.DefaultAssets;
import com.imguns.guns.api.item.IAmmo;
import com.imguns.guns.init.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class AmmoItemBuilder {
    private int count = 1;
    private Identifier ammoId = DefaultAssets.DEFAULT_AMMO_ID;

    private AmmoItemBuilder() {
    }

    public static AmmoItemBuilder create() {
        return new AmmoItemBuilder();
    }

    public AmmoItemBuilder setCount(int count) {
        this.count = Math.max(count, 1);
        return this;
    }

    public AmmoItemBuilder setId(Identifier id) {
        this.ammoId = id;
        return this;
    }

    public ItemStack build() {
        ItemStack ammo = new ItemStack(ModItems.AMMO, this.count);
        if (ammo.getItem() instanceof IAmmo iAmmo) {
            iAmmo.setAmmoId(ammo, this.ammoId);
        }
        return ammo;
    }
}
