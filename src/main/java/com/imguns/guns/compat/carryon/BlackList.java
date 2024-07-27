package com.imguns.guns.compat.carryon;

import com.imguns.guns.GunMod;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import tschipp.carryon.common.config.ListHandler;

public class BlackList {

    public static void addBlackList() {
        for (Identifier id : Registries.BLOCK.getIds()) {
            if (id.getNamespace().equals(GunMod.MOD_ID)) {
                ListHandler.addForbiddenTiles(id.toString());
            }
        }
    }
}
