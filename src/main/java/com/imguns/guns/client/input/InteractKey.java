package com.imguns.guns.client.input;

import com.imguns.guns.api.item.IGun;
import com.imguns.guns.config.util.InteractKeyConfigRead;
import com.imguns.guns.api.client.event.InputEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import static com.imguns.guns.util.InputExtraCheck.isInGame;

@Environment(EnvType.CLIENT)
public class InteractKey {
    public static final KeyBinding INTERACT_KEY = new KeyBinding("key.immersive_guns.interact.desc",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.category.imguns");

    public static void onInteractKeyPress(InputEvent.Key event) {
        if (isInGame() && event.getAction() == GLFW.GLFW_PRESS && INTERACT_KEY.matchesKey(event.getKey(), event.getScanCode())) {
            doInteractLogic();
        }
    }

    public static void onInteractMousePress(InputEvent.MouseButton.Post event) {
        if (isInGame() && event.getAction() == GLFW.GLFW_PRESS && INTERACT_KEY.matchesMouse(event.getButton())) {
            doInteractLogic();
        }
    }

    private static void doInteractLogic() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || player.isSpectator()) {
            return;
        }
        if (!IGun.mainhandHoldGun(player)) {
            return;
        }
        HitResult hitResult = mc.crosshairTarget;
        if (hitResult == null) {
            return;
        }
        if (hitResult instanceof BlockHitResult blockHitResult) {
            interactBlock(blockHitResult, player, mc);
            return;
        }
        if (hitResult instanceof EntityHitResult entityHitResult) {
            interactEntity(entityHitResult, mc);
        }
    }

    private static void interactBlock(BlockHitResult blockHitResult, ClientPlayerEntity player, MinecraftClient mc) {
        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState block = player.getWorld().getBlockState(blockPos);
        if (InteractKeyConfigRead.canInteractBlock(block)) {
            mc.doItemUse();
        }
    }

    private static void interactEntity(EntityHitResult entityHitResult, MinecraftClient mc) {
        Entity entity = entityHitResult.getEntity();
        if (InteractKeyConfigRead.canInteractEntity(entity)) {
            mc.doItemUse();
        }
    }
}
