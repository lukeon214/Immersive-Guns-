package com.imguns.guns.mixin.client;

import com.imguns.guns.api.client.event.BeforeRenderHandEvent;
import com.imguns.guns.api.client.event.RenderHandEvent;
import com.imguns.guns.api.client.other.KeepingItemRenderer;
import com.imguns.guns.api.item.IGun;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin implements KeepingItemRenderer {
    @Shadow
    private float equipProgressMainHand;
    @Shadow
    private float prevEquipProgressMainHand;
    @Shadow
    private ItemStack mainHand;

    @Shadow protected abstract void renderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    @Unique
    private ItemStack imguns$KeepItem;
    @Unique
    private long imguns$KeepTimeMs;
    @Unique
    private long imguns$KeepTimestamp;

    @Inject(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At(value = "HEAD"))
    private void beforeHandRender(float tickDelta, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, ClientPlayerEntity player, int light, CallbackInfo ci) {
        new BeforeRenderHandEvent(matrices).post();
    }

    @Redirect(method = "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
    private void renderFirstPersonItemMainHand(HeldItemRenderer instance, AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (callEvent(hand, matrices, vertexConsumers, light, tickDelta, pitch, swingProgress, equipProgress, item)) {
            renderFirstPersonItem(player, tickDelta, pitch, hand, swingProgress, item, equipProgress, matrices, vertexConsumers, light);
        }
    }

    @Unique
    private boolean callEvent(Hand hand, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, float partialTick, float interpPitch, float swingProgress, float equipProgress, ItemStack stack) {
        return !new RenderHandEvent(hand, poseStack, bufferSource, packedLight, partialTick, interpPitch, swingProgress, equipProgress, stack).post();
    }

    @Inject(method = "updateHeldItems", at = @At("HEAD"))
    private void cancelEquippedProgress(CallbackInfo ci) {
        if (MinecraftClient.getInstance().player == null) {
            return;
        }
        if (imguns$KeepItem != null) {
            long time = System.currentTimeMillis() - imguns$KeepTimestamp;
            if (time < imguns$KeepTimeMs) {
                equipProgressMainHand = 1.0f;
                prevEquipProgressMainHand = 1.0f;
                mainHand = imguns$KeepItem;
                return;
            }
        }
        ItemStack itemStack = MinecraftClient.getInstance().player.getMainHandStack();
        IGun iGun = IGun.getIGunOrNull(itemStack);
        if (iGun != null) {
            equipProgressMainHand = 1.0f;
            prevEquipProgressMainHand = 1.0f;
            mainHand = itemStack;
        }
    }

    @Unique
    @Override
    public void keep(ItemStack itemStack, long timeMs) {
        long time = System.currentTimeMillis() - imguns$KeepTimestamp;
        if (time < imguns$KeepTimeMs) {
            return;
        }
        this.imguns$KeepItem = itemStack;
        this.mainHand = itemStack;
        this.imguns$KeepTimeMs = timeMs;
        this.imguns$KeepTimestamp = System.currentTimeMillis();
    }

    @Override
    public ItemStack getCurrentItem() {
        return mainHand;
    }
}
