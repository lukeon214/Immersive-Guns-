package com.imguns.guns.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.imguns.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.imguns.guns.api.entity.ShootResult;
import com.imguns.guns.client.gameplay.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin implements IClientPlayerGunOperator {
    private final @Unique ClientPlayerEntity tac$player = (ClientPlayerEntity) (Object) this;
    private final @Unique LocalPlayerDataHolder tac$data = new LocalPlayerDataHolder(tac$player);
    private final @Unique LocalPlayerAim tac$aim = new LocalPlayerAim(tac$data, tac$player);
    private final @Unique LocalPlayerBolt tac$bolt = new LocalPlayerBolt(tac$data, tac$player);
    private final @Unique LocalPlayerDraw tac$draw = new LocalPlayerDraw(tac$data, tac$player);
    private final @Unique LocalPlayerFireSelect tac$fireSelect = new LocalPlayerFireSelect(tac$data, tac$player);
    private final @Unique LocalPlayerMelee tac$melee = new LocalPlayerMelee(tac$data, tac$player);
    private final @Unique LocalPlayerInspect tac$inspect = new LocalPlayerInspect(tac$data, tac$player);
    private final @Unique LocalPlayerReload tac$reload = new LocalPlayerReload(tac$data, tac$player);
    private final @Unique LocalPlayerShoot tac$shoot = new LocalPlayerShoot(tac$data, tac$player);

    @Unique
    @Override
    public ShootResult shoot() {
        return tac$shoot.shoot();
    }

    @Unique
    @Override
    public void draw(ItemStack lastItem) {
        tac$draw.draw(lastItem);
    }

    @Unique
    @Override
    public void bolt() {
        tac$bolt.bolt();
    }

    @Unique
    @Override
    public void reload() {
        tac$reload.reload();
    }

    @Unique
    @Override
    public void inspect() {
        tac$inspect.inspect();
    }

    @Override
    public void fireSelect() {
        tac$fireSelect.fireSelect();
    }

    @Override
    public void melee() {
        tac$melee.melee();
    }

    @Override
    public void aim(boolean isAim) {
        tac$aim.aim(isAim);
    }

    @Unique
    @Override
    public float getClientAimingProgress(float partialTicks) {
        return tac$aim.getClientAimingProgress(partialTicks);
    }

    @Unique
    @Override
    public long getClientShootCoolDown() {
        return tac$shoot.getClientShootCoolDown();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTickClientSide(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player.getWorld().isClient()) {
            tac$aim.tickAimingProgress();
            tac$data.tickStateLock();
            tac$bolt.tickAutoBolt();
        }
    }

    @WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setSprinting(Z)V"))
    public void swapSprintStatus(ClientPlayerEntity player, boolean sprinting, Operation<Void> original) {
        original.call(player, this.tac$aim.cancelSprint(player, sprinting));
    }

    @Inject(method = "requestRespawn", at = @At("RETURN"))
    public void onRespawn(CallbackInfo ci) {
        tac$data.reset();
        draw(ItemStack.EMPTY);
    }

    @Override
    public boolean isAim() {
        return tac$aim.isAim();
    }
}
