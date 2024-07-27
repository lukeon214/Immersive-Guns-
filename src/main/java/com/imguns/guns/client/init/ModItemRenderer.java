package com.imguns.guns.client.init;

import com.imguns.guns.api.item.gun.AbstractGunItem;
import com.imguns.guns.client.renderer.item.AmmoItemRenderer;
import com.imguns.guns.client.renderer.item.AttachmentItemRenderer;
import com.imguns.guns.client.renderer.item.GunItemRenderer;
import com.imguns.guns.client.renderer.item.GunSmithTableItemRenderer;
import com.imguns.guns.init.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.registry.Registries;

public class ModItemRenderer {

    public static void itemRenderers() {
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.AMMO, new AmmoItemRenderer());
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.ATTACHMENT, new AttachmentItemRenderer());
        Registries.ITEM.forEach(item -> {
            if (item instanceof AbstractGunItem) {
                BuiltinItemRendererRegistry.INSTANCE.register(item, new GunItemRenderer());
            }
        });
        BuiltinItemRendererRegistry.INSTANCE.register(ModItems.GUN_SMITH_TABLE, new GunSmithTableItemRenderer());
    }
}
