package com.imguns.guns.client.input;

import com.imguns.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.imguns.guns.api.item.IGun;
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
public class ReloadKey {
    public static final KeyBinding RELOAD_KEY = new KeyBinding("key.immersive_guns.reload.desc",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.category.imguns");

    public static void onReloadPress(InputEvent.Key event) {
        if (isInGame() && event.getAction() == GLFW.GLFW_PRESS && RELOAD_KEY.matchesKey(event.getKey(), event.getScanCode())) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null || player.isSpectator()) {
                return;
            }
            if (IGun.mainhandHoldGun(player)) {
                IClientPlayerGunOperator.fromLocalPlayer(player).reload();
            }
        }
    }
}
