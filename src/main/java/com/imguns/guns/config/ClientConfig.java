package com.imguns.guns.config;

import com.imguns.guns.config.client.KeyConfig;
import com.imguns.guns.config.client.RenderConfig;
import com.imguns.guns.config.client.ZoomConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static ForgeConfigSpec init() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        KeyConfig.init(builder);
        RenderConfig.init(builder);
        ZoomConfig.init(builder);
        return builder.build();
    }
}
