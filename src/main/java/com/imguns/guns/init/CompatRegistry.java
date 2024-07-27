package com.imguns.guns.init;

import com.imguns.guns.compat.carryon.BlackList;
import com.imguns.guns.compat.iris.IrisCompat;
import net.fabricmc.loader.api.FabricLoader;

public class CompatRegistry {
    public static final String CLOTH_CONFIG = "cloth-config";
    public static final String IRIS = "iris";
    public static final String CARRY_ON_ID = "carryon";

    public static void init() {
        checkModLoad(IRIS, IrisCompat::initCompat);
        checkModLoad(CARRY_ON_ID, BlackList::addBlackList);
    }

    public static void checkModLoad(String modId, Runnable runnable) {
        if (FabricLoader.getInstance().isModLoaded(modId)) {
            runnable.run();
        }
    }
}
