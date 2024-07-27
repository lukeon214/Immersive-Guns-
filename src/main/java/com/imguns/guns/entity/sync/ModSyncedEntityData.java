package com.imguns.guns.entity.sync;

import com.imguns.guns.GunMod;
import com.imguns.guns.api.entity.ReloadState;
import com.imguns.guns.entity.sync.core.Serializers;
import com.imguns.guns.entity.sync.core.SyncedClassKey;
import com.imguns.guns.entity.sync.core.SyncedDataKey;
import com.imguns.guns.entity.sync.core.SyncedEntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class ModSyncedEntityData {
    public static final SyncedDataKey<LivingEntity, Long> SHOOT_COOL_DOWN_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.LONG)
            .id(new Identifier(GunMod.MOD_ID, "shoot_cool_down"))
            .defaultValueSupplier(() -> -1L)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static final SyncedDataKey<LivingEntity, Long> MELEE_COOL_DOWN_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.LONG)
            .id(new Identifier(GunMod.MOD_ID, "melee_cool_down"))
            .defaultValueSupplier(() -> -1L)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static final SyncedDataKey<LivingEntity, ReloadState> RELOAD_STATE_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, ModSerializers.RELOAD_STATE)
            .id(new Identifier(GunMod.MOD_ID, "reload_state"))
            .defaultValueSupplier(ReloadState::new)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static final SyncedDataKey<LivingEntity, Float> AIMING_PROGRESS_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.FLOAT)
            .id(new Identifier(GunMod.MOD_ID, "aiming_progress"))
            .defaultValueSupplier(() -> 0f)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static final SyncedDataKey<LivingEntity, Long> DRAW_COOL_DOWN_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.LONG)
            .id(new Identifier(GunMod.MOD_ID, "draw_cool_down"))
            .defaultValueSupplier(() -> -1L)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static final SyncedDataKey<LivingEntity, Boolean> IS_AIMING_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.BOOLEAN)
            .id(new Identifier(GunMod.MOD_ID, "is_aiming"))
            .defaultValueSupplier(() -> false)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static final SyncedDataKey<LivingEntity, Float> SPRINT_TIME_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.FLOAT)
            .id(new Identifier(GunMod.MOD_ID, "sprint_time"))
            .defaultValueSupplier(() -> 0f)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static final SyncedDataKey<LivingEntity, Long> BOLT_COOL_DOWN_KEY = SyncedDataKey.builder(SyncedClassKey.LIVING_ENTITY, Serializers.LONG)
            .id(new Identifier(GunMod.MOD_ID, "bolt_cool_down"))
            .defaultValueSupplier(() -> -1L)
            .syncMode(SyncedDataKey.SyncMode.ALL)
            .build();

    public static void init() {
        registerEntityData(SHOOT_COOL_DOWN_KEY);
        registerEntityData(MELEE_COOL_DOWN_KEY);
        registerEntityData(RELOAD_STATE_KEY);
        registerEntityData(AIMING_PROGRESS_KEY);
        registerEntityData(DRAW_COOL_DOWN_KEY);
        registerEntityData(IS_AIMING_KEY);
        registerEntityData(SPRINT_TIME_KEY);
        registerEntityData(BOLT_COOL_DOWN_KEY);
    }

    private static void registerEntityData(SyncedDataKey<? extends Entity, ?> dataKey) {
        SyncedEntityData.instance().registerDataKey(dataKey);
    }
}
