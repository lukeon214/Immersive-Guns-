package com.imguns.guns.client.input;

import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.imguns.guns.api.item.IGun;
import com.imguns.guns.api.item.gun.FireMode;
import com.imguns.guns.client.sound.SoundPlayManager;
import com.imguns.guns.api.client.event.InputEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import static com.imguns.guns.util.InputExtraCheck.isInGame;

@Environment(EnvType.CLIENT)
public class ShootKey {
    public static final KeyBinding SHOOT_KEY = new KeyBinding("key.immersive_guns.shoot.desc",
            InputUtil.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_LEFT,
            "key.category.imguns");

    public static void autoShoot(MinecraftClient mc) {
        if (!isInGame()) {
            return;
        }
        ClientPlayerEntity player = mc.player;
        if (player == null || player.isSpectator()) {
            return;
        }
        ItemStack mainHandItem = player.getMainHandStack();
        if (mainHandItem.getItem() instanceof IGun iGun) {
            FireMode fireMode = iGun.getFireMode(mainHandItem);
            boolean isBurstAuto = fireMode == FireMode.BURST && TimelessAPI.getCommonGunIndex(iGun.getGunId(mainHandItem))
                    .map(index -> index.getGunData().getBurstData().isContinuousShoot())
                    .orElse(false);
            IClientPlayerGunOperator operator = IClientPlayerGunOperator.fromLocalPlayer(player);
            if (SHOOT_KEY.isPressed() && (fireMode == FireMode.AUTO || isBurstAuto)) {
                operator.shoot();
            }
        }
    }

    public static void semiShoot(InputEvent.MouseButton.Post event) {
        if (isInGame() && SHOOT_KEY.matchesMouse(event.getButton())) {
            // 松开鼠标，重置 DryFire 状态
            if (event.getAction() == GLFW.GLFW_RELEASE) {
                SoundPlayManager.resetDryFireSound();
                return;
            }
            MinecraftClient mc = MinecraftClient.getInstance();
            ClientPlayerEntity player = mc.player;
            if (player == null || player.isSpectator()) {
                return;
            }
            ItemStack mainHandItem = player.getMainHandStack();
            if (mainHandItem.getItem() instanceof IGun iGun) {
                FireMode fireMode = iGun.getFireMode(mainHandItem);
                boolean isBurstSemi = fireMode == FireMode.BURST && TimelessAPI.getCommonGunIndex(iGun.getGunId(mainHandItem))
                        .map(index -> !index.getGunData().getBurstData().isContinuousShoot())
                        .orElse(false);
                if (fireMode == FireMode.UNKNOWN) {
                    player.sendMessage(Text.translatable("message.immersive_guns.fire_select.fail"));
                }
                if (fireMode == FireMode.SEMI || isBurstSemi) {
                    IClientPlayerGunOperator.fromLocalPlayer(player).shoot();
                }
            }
        }
    }
}
