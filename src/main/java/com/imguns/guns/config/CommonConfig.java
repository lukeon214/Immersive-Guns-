package com.imguns.guns.config;

import com.imguns.guns.config.common.AmmoConfig;
import com.imguns.guns.config.common.GunConfig;
import com.imguns.guns.config.common.OtherConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public final class CommonConfig {
    public static ForgeConfigSpec init() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        GunConfig.init(builder);
        AmmoConfig.init(builder);
        OtherConfig.init(builder);
        return builder.build();
    }
}
