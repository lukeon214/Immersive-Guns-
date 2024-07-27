package com.imguns.guns.init;

import com.imguns.guns.GunMod;
import com.imguns.guns.crafting.GunSmithTableRecipe;
import com.imguns.guns.crafting.GunSmithTableSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRecipe {
    public static void init() {
        Registry.register(Registries.RECIPE_SERIALIZER, GunSmithTableSerializer.ID, GunSmithTableSerializer.INSTANCE);
        Registry.register(Registries.RECIPE_TYPE, new Identifier(GunMod.MOD_ID, GunSmithTableRecipe.Type.ID), GunSmithTableRecipe.Type.INSTANCE);
    }
}
