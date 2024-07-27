package com.imguns.guns.client.init;

import com.imguns.guns.client.gui.GunSmithTableScreen;
import com.imguns.guns.inventory.GunSmithTableMenu;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class ModContainerScreen {
    public static void clientSetup() {
        HandledScreens.register(GunSmithTableMenu.TYPE, GunSmithTableScreen::new);
    }
}
