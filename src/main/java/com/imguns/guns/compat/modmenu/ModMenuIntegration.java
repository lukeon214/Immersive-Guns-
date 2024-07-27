package com.imguns.guns.compat.modmenu;

import com.imguns.guns.client.gui.compat.ClothConfigScreen;
import com.imguns.guns.compat.cloth.MenuIntegration;
import com.imguns.guns.init.CompatRegistry;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (parent) -> {
            if (FabricLoader.getInstance().isModLoaded(CompatRegistry.CLOTH_CONFIG)) {
                return MenuIntegration.getConfigScreen(parent);
            } else {
                return new ClothConfigScreen(parent);
            }
        };
    }
}
