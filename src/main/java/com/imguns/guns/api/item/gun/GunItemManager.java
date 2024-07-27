package com.imguns.guns.api.item.gun;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;

public class GunItemManager {
    private static final Map<String, AbstractGunItem> GUN_ITEM_MAP = Maps.newHashMap();

    /**
     * 建议在 RegistryEvent.Register<Item> 事件时注册此枪械变种
     */
    public static void registerGunItem(String name, AbstractGunItem item) {
        GUN_ITEM_MAP.put(name, item);
    }

    public static AbstractGunItem getGunItemRegistryObject(String key) {
        return GUN_ITEM_MAP.get(key);
    }

    public static Collection<AbstractGunItem> getAllGunItems() {
        return GUN_ITEM_MAP.values();
    }
}
