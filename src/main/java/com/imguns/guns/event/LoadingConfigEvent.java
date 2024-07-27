package com.imguns.guns.event;

import com.imguns.guns.client.download.ClientGunPackDownloadManager;
import com.imguns.guns.config.util.HeadShotAABBConfigRead;
import com.imguns.guns.config.util.InteractKeyConfigRead;
import com.imguns.guns.util.EnvironmentUtil;
import net.minecraftforge.fml.config.ModConfig;

public class LoadingConfigEvent {
    private static final String CONFIG_NAME = "imguns-server.toml";

    public static void onModConfigLoading(ModConfig config) {
        String fileName = config.getFileName();
        if (CONFIG_NAME.equals(fileName)) {
            HeadShotAABBConfigRead.init();
            InteractKeyConfigRead.init();
        }
    }

    public static void onModConfigReloading(ModConfig config) {
        String fileName = config.getFileName();
        if (CONFIG_NAME.equals(fileName)) {
            HeadShotAABBConfigRead.init();
            InteractKeyConfigRead.init();
            if (EnvironmentUtil.isClient()) {
                ClientGunPackDownloadManager.downloadClientGunPack();
            }
        }
    }
}
