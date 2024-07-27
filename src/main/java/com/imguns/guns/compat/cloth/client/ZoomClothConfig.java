package com.imguns.guns.compat.cloth.client;

import com.imguns.guns.config.client.ZoomConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ZoomClothConfig {
    public static void init(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
        ConfigCategory zoom = root.getOrCreateCategory(Text.translatable("config.immersive_guns.client.zoom"));

        zoom.addEntry(entryBuilder.startDoubleField(Text.translatable("config.immersive_guns.client.zoom.screen_distance_coefficient"), ZoomConfig.SCREEN_DISTANCE_COEFFICIENT.get())
                .setMin(0).setMax(3.0).setDefaultValue(1.33).setTooltip(Text.translatable("config.immersive_guns.client.zoom.screen_distance_coefficient.desc"))
                .setSaveConsumer(ZoomConfig.SCREEN_DISTANCE_COEFFICIENT::set).build());

        zoom.addEntry(entryBuilder.startDoubleField(Text.translatable("config.immersive_guns.client.zoom.zoom_sensitivity_base_multiplier"), ZoomConfig.ZOOM_SENSITIVITY_BASE_MULTIPLIER.get())
                .setMin(0).setMax(2.0).setDefaultValue(1).setTooltip(Text.translatable("config.immersive_guns.client.zoom.zoom_sensitivity_base_multiplier.desc"))
                .setSaveConsumer(ZoomConfig.ZOOM_SENSITIVITY_BASE_MULTIPLIER::set).build());
    }
}
