package com.imguns.guns.compat.cloth.common;

import com.imguns.guns.config.common.GunConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class GunClothConfig {
    public static void init(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
        ConfigCategory gun = root.getOrCreateCategory(Text.translatable("config.immersive_guns.common.gun"));

        gun.addEntry(entryBuilder.startIntField(Text.translatable("config.immersive_guns.common.gun.default_gun_fire_sound_distance"), GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get())
                .setMin(0).setMax(Integer.MAX_VALUE).setDefaultValue(64).setTooltip(Text.translatable("config.immersive_guns.common.gun.default_gun_fire_sound_distance.desc"))
                .setSaveConsumer(GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE::set).build());

        gun.addEntry(entryBuilder.startIntField(Text.translatable("config.immersive_guns.common.gun.default_gun_other_sound_distance"), GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get())
                .setMin(0).setMax(Integer.MAX_VALUE).setDefaultValue(16).setTooltip(Text.translatable("config.immersive_guns.common.gun.default_gun_other_sound_distance.desc"))
                .setSaveConsumer(GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE::set).build());

        gun.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.immersive_guns.common.gun.creative_player_consume_ammo"), GunConfig.CREATIVE_PLAYER_CONSUME_AMMO.get())
                .setDefaultValue(true).setTooltip(Text.translatable("config.immersive_guns.common.gun.creative_player_consume_ammo.desc"))
                .setSaveConsumer(GunConfig.CREATIVE_PLAYER_CONSUME_AMMO::set).build());

        gun.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.immersive_guns.common.gun.auto_reload_when_respawn"), GunConfig.AUTO_RELOAD_WHEN_RESPAWN.get())
                .setDefaultValue(false).setTooltip(Text.translatable("config.immersive_guns.common.gun.auto_reload_when_respawn.desc"))
                .setSaveConsumer(GunConfig.AUTO_RELOAD_WHEN_RESPAWN::set).build());
    }
}
