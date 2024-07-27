package com.imguns.guns.client.input;

import com.imguns.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.imguns.guns.api.item.IGun;
import com.imguns.guns.config.client.KeyConfig;
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
public class AimKey {
    public static final KeyBinding AIM_KEY = new KeyBinding("key.immersive_guns.aim.desc",
            InputUtil.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_RIGHT,
            "key.category.imguns");

    public static void onAimPress(InputEvent.MouseButton.Post event) {
        if (isInGame() && AIM_KEY.matchesMouse(event.getButton())) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null || player.isSpectator()) {
                return;
            }
            if (!(player instanceof IClientPlayerGunOperator operator)) {
                return;
            }
            if (IGun.mainhandHoldGun(player)) {
                boolean action = true;
                if (!KeyConfig.HOLD_TO_AIM.get()) {
                    action = !operator.isAim();
                }
                if (event.getAction() == GLFW.GLFW_PRESS) {
                    IClientPlayerGunOperator.fromLocalPlayer(player).aim(action);
                }
                if (KeyConfig.HOLD_TO_AIM.get() && event.getAction() == GLFW.GLFW_RELEASE) {
                    IClientPlayerGunOperator.fromLocalPlayer(player).aim(false);
                }
            }
        }
    }

    public static void cancelAim(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        if (!(player instanceof IClientPlayerGunOperator operator)) {
            return;
        }
        if (operator.isAim() && !isInGame()) {
            IClientPlayerGunOperator.fromLocalPlayer(player).aim(false);
        }
    }
}
