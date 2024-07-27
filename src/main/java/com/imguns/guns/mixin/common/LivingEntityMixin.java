package com.imguns.guns.mixin.common;

import com.imguns.guns.api.entity.IGunOperator;
import com.imguns.guns.api.entity.KnockBackModifier;
import com.imguns.guns.api.entity.ReloadState;
import com.imguns.guns.api.entity.ShootResult;
import com.imguns.guns.entity.shooter.*;
import com.imguns.guns.entity.sync.ModSyncedEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(LivingEntity.class)
public class LivingEntityMixin implements IGunOperator, KnockBackModifier {
    private final @Unique LivingEntity imguns$shooter = (LivingEntity) (Object) this;
    private final @Unique ShooterDataHolder imguns$data = new ShooterDataHolder();
    private final @Unique LivingEntityDrawGun imguns$draw = new LivingEntityDrawGun(imguns$shooter, imguns$data);
    private final @Unique LivingEntityAim imguns$aim = new LivingEntityAim(imguns$shooter, this.imguns$data);
    private final @Unique LivingEntityAmmoCheck imguns$ammoCheck = new LivingEntityAmmoCheck(imguns$shooter);
    private final @Unique LivingEntityFireSelect imguns$fireSelect = new LivingEntityFireSelect(imguns$shooter, this.imguns$data);
    private final @Unique LivingEntityMelee imguns$melee = new LivingEntityMelee(imguns$shooter, this.imguns$data, this.imguns$draw);
    private final @Unique LivingEntityShoot imguns$shoot = new LivingEntityShoot(imguns$shooter, this.imguns$data, this.imguns$draw);
    private final @Unique LivingEntityBolt imguns$bolt = new LivingEntityBolt(this.imguns$data, this.imguns$draw, this.imguns$shoot);
    private final @Unique LivingEntityReload imguns$reload = new LivingEntityReload(imguns$shooter, this.imguns$data, this.imguns$draw, this.imguns$shoot);

    @Override
    @Unique
    public long getSynShootCoolDown() {
        return ModSyncedEntityData.SHOOT_COOL_DOWN_KEY.getValue(imguns$shooter);
    }

    @Override
    public long getSynMeleeCoolDown() {
        return ModSyncedEntityData.MELEE_COOL_DOWN_KEY.getValue(imguns$shooter);
    }

    @Override
    @Unique
    public long getSynDrawCoolDown() {
        return ModSyncedEntityData.DRAW_COOL_DOWN_KEY.getValue(imguns$shooter);
    }

    @Override
    @Unique
    public long getSynBoltCoolDown() {
        return ModSyncedEntityData.BOLT_COOL_DOWN_KEY.getValue(imguns$shooter);
    }

    @Override
    @Unique
    public ReloadState getSynReloadState() {
        return ModSyncedEntityData.RELOAD_STATE_KEY.getValue(imguns$shooter);
    }

    @Override
    @Unique
    public float getSynAimingProgress() {
        return ModSyncedEntityData.AIMING_PROGRESS_KEY.getValue(imguns$shooter);
    }

    @Override
    @Unique
    public float getSynSprintTime() {
        return ModSyncedEntityData.SPRINT_TIME_KEY.getValue(imguns$shooter);
    }

    @Override
    @Unique
    public boolean getSynIsAiming() {
        return ModSyncedEntityData.IS_AIMING_KEY.getValue(imguns$shooter);
    }

    @Override
    @Unique
    public void initialData() {
        this.imguns$data.initialData();
    }

    @Unique
    @Override
    public void draw(Supplier<ItemStack> gunItemSupplier) {
        this.imguns$draw.draw(gunItemSupplier);
    }

    @Unique
    @Override
    public void bolt() {
        this.imguns$bolt.bolt();
    }

    @Unique
    @Override
    public void reload() {
        this.imguns$reload.reload();
    }

    @Override
    public void melee() {
        this.imguns$melee.melee();
    }

    @Unique
    @Override
    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw) {
        return this.imguns$shoot.shoot(pitch, yaw);
    }

    @Unique
    @Override
    public boolean needCheckAmmo() {
        return this.imguns$ammoCheck.needCheckAmmo();
    }

    @Unique
    @Override
    public boolean consumesAmmoOrNot() {
        return this.imguns$ammoCheck.consumesAmmoOrNot();
    }

    @Unique
    @Override
    public void aim(boolean isAim) {
        this.imguns$aim.aim(isAim);
    }

    @Unique
    @Override
    public void fireSelect() {
        this.imguns$fireSelect.fireSelect();
    }

    @Unique
    @Override
    public void zoom() {
        this.imguns$aim.zoom();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickServerSide(CallbackInfo ci) {
        // 仅在服务端调用
        if (!This().getWorld().isClient()) {
            // 完成各种 tick 任务
            ReloadState reloadState = this.imguns$reload.tickReloadState();
            this.imguns$aim.tickAimingProgress();
            this.imguns$aim.tickSprint();
            this.imguns$bolt.tickBolt();
            this.imguns$melee.scheduleTickMelee();
            // 从服务端同步数据
            ModSyncedEntityData.SHOOT_COOL_DOWN_KEY.setValue(imguns$shooter, this.imguns$shoot.getShootCoolDown());
            ModSyncedEntityData.MELEE_COOL_DOWN_KEY.setValue(imguns$shooter, this.imguns$melee.getMeleeCoolDown());
            ModSyncedEntityData.DRAW_COOL_DOWN_KEY.setValue(imguns$shooter, this.imguns$draw.getDrawCoolDown());
            ModSyncedEntityData.BOLT_COOL_DOWN_KEY.setValue(imguns$shooter, this.imguns$data.boltCoolDown);
            ModSyncedEntityData.RELOAD_STATE_KEY.setValue(imguns$shooter, reloadState);
            ModSyncedEntityData.AIMING_PROGRESS_KEY.setValue(imguns$shooter, this.imguns$data.aimingProgress);
            ModSyncedEntityData.IS_AIMING_KEY.setValue(imguns$shooter, this.imguns$data.isAiming);
            ModSyncedEntityData.SPRINT_TIME_KEY.setValue(imguns$shooter, this.imguns$data.sprintTimeS);
        }
    }

    @Override
    @Unique
    public void resetKnockBackStrength() {
        this.imguns$data.knockbackStrength = -1;
    }

    @Override
    @Unique
    public double getKnockBackStrength() {
        return this.imguns$data.knockbackStrength;
    }

    @Override
    @Unique
    public void setKnockBackStrength(double strength) {
        this.imguns$data.knockbackStrength = strength;
    }

    @Unique
    private LivingEntity This() {
        return (LivingEntity) (Object) this;
    }
}
