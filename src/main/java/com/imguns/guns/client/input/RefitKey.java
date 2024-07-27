package com.imguns.guns.client.input;

import com.imguns.guns.api.item.IGun;
import com.imguns.guns.client.gui.GunRefitScreen;
import com.imguns.guns.api.client.event.InputEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import static com.imguns.guns.util.InputExtraCheck.isInGame;

@Environment(EnvType.CLIENT)
public class RefitKey {
    public static final KeyBinding REFIT_KEY = new KeyBinding("key.immersive_guns.refit.desc",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "key.category.imguns");

    public static void onRefitPress(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS && REFIT_KEY.matchesKey(event.getKey(), event.getScanCode())) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null || player.isSpectator()) {
                return;
            }
            if (isInGame()) {
                if (IGun.mainhandHoldGun(player) && MinecraftClient.getInstance().currentScreen == null) {
                    IGun iGun = IGun.getIGunOrNull(player.getMainHandStack());
                    if (iGun != null && iGun.hasAttachmentLock(player.getMainHandStack())) {
                        return;
                    }
                    MinecraftClient.getInstance().setScreen(new GunRefitScreen());
                }
            } else if (MinecraftClient.getInstance().currentScreen instanceof GunRefitScreen) {
                MinecraftClient.getInstance().setScreen(null);
            }
        }
    }
}
