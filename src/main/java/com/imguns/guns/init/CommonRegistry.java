package com.imguns.guns.init;

import com.imguns.guns.entity.sync.ModSyncedEntityData;
import com.imguns.guns.network.NetworkHandler;

public class CommonRegistry {
    private static boolean LOAD_COMPLETE = false;

    public static void init() {
        NetworkHandler.init();
        ModSyncedEntityData.init();
    }

    public static void onLoadComplete() {
        LOAD_COMPLETE = true;
    }

    public static boolean isLoadComplete() {
        return LOAD_COMPLETE;
    }
}
