package com.imguns.guns.init;

import com.imguns.guns.GunMod;
import com.imguns.guns.entity.EntityKineticBullet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static EntityType<EntityKineticBullet> BULLET = register("bullet", EntityKineticBullet.TYPE);

    public static void init() {
    }

    private static <T extends Entity> EntityType<T> register(String path, EntityType<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, new Identifier(GunMod.MOD_ID, path), type);
    }
}
