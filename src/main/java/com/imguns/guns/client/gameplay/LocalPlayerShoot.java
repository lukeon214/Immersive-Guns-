package com.imguns.guns.client.gameplay;

import com.imguns.guns.api.LogicalSide;
import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.imguns.guns.api.entity.IGunOperator;
import com.imguns.guns.api.entity.ShootResult;
import com.imguns.guns.api.event.common.GunFireEvent;
import com.imguns.guns.api.event.common.GunShootEvent;
import com.imguns.guns.api.item.IGun;
import com.imguns.guns.api.item.gun.FireMode;
import com.imguns.guns.client.animation.statemachine.GunAnimationStateMachine;
import com.imguns.guns.client.resource.index.ClientGunIndex;
import com.imguns.guns.client.sound.SoundPlayManager;
import com.imguns.guns.network.NetworkHandler;
import com.imguns.guns.network.message.ClientMessagePlayerShoot;
import com.imguns.guns.resource.index.CommonGunIndex;
import com.imguns.guns.resource.pojo.data.gun.Bolt;
import com.imguns.guns.resource.pojo.data.gun.GunData;
import com.imguns.guns.sound.SoundManager;
import com.imguns.guns.util.AttachmentDataUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class LocalPlayerShoot {
    private static final Predicate<IGunOperator> SHOOT_LOCKED_CONDITION = operator -> operator.getSynShootCoolDown() > 0;
    private final LocalPlayerDataHolder data;
    private final ClientPlayerEntity player;

    public LocalPlayerShoot(LocalPlayerDataHolder data, ClientPlayerEntity player) {
        this.data = data;
        this.player = player;
    }

    public ShootResult shoot() {
        // 按钮冷却时间未到，防止点击按钮后误触开火
        // 默认设置为 50 ms
        if (System.currentTimeMillis() - LocalPlayerDataHolder.clientClickButtonTimestamp < 50) {
            return ShootResult.COOL_DOWN;
        }
        // 如果上一次异步开火的效果还未执行，则直接返回，等待异步开火效果执行
        if (!data.isShootRecorded) {
            return ShootResult.COOL_DOWN;
        }
        // 如果状态锁正在准备锁定，且不是开火的状态锁，则不允许开火(主要用于防止切枪后开火动作覆盖切枪动作)
        if (data.clientStateLock && data.lockedCondition != SHOOT_LOCKED_CONDITION && data.lockedCondition != null) {
            data.isShootRecorded = true;
            // 因为这块主要目的是防止切枪后开火动作覆盖切枪动作，返回 IS_DRAWING
            return ShootResult.IS_DRAWING;
        }
        // 暂定为只有主手能开枪
        ItemStack mainhandItem = player.getMainHandStack();
        if (!(mainhandItem.getItem() instanceof IGun iGun)) {
            return ShootResult.NOT_GUN;
        }
        Identifier gunId = iGun.getGunId(mainhandItem);
        Optional<ClientGunIndex> gunIndexOptional = TimelessAPI.getClientGunIndex(gunId);
        if (gunIndexOptional.isEmpty()) {
            return ShootResult.ID_NOT_EXIST;
        }
        ClientGunIndex gunIndex = gunIndexOptional.get();
        GunData gunData = gunIndex.getGunData();
        long coolDown = this.getCoolDown(iGun, mainhandItem, gunData);
        // 如果射击冷却大于等于 1 tick (即 50 ms)，则不允许开火
        if (coolDown >= 50) {
            return ShootResult.COOL_DOWN;
        }
        // 因为开火冷却检测用了特别定制的方法，所以不检查状态锁，而是手动检查是否换弹、切枪
        IGunOperator gunOperator = IGunOperator.fromLivingEntity(player);
        // 检查是否正在换弹
        if (gunOperator.getSynReloadState().getStateType().isReloading()) {
            return ShootResult.IS_RELOADING;
        }
        // 检查是否正在切枪
        if (gunOperator.getSynDrawCoolDown() != 0) {
            return ShootResult.IS_DRAWING;
        }
        // 判断是否处于近战冷却时间
        if (gunOperator.getSynMeleeCoolDown() != 0) {
            return ShootResult.IS_MELEE;
        }
        // 判断子弹数
        Bolt boltType = gunIndex.getGunData().getBolt();
        boolean hasAmmoInBarrel = iGun.hasBulletInBarrel(mainhandItem) && boltType != Bolt.OPEN_BOLT;
        int ammoCount = iGun.getCurrentAmmoCount(mainhandItem) + (hasAmmoInBarrel ? 1 : 0);
        if (ammoCount < 1) {
            SoundPlayManager.playDryFireSound(player, gunIndex);
            return ShootResult.NO_AMMO;
        }
        // 判断膛内子弹
        if (boltType == Bolt.MANUAL_ACTION && !hasAmmoInBarrel) {
            IClientPlayerGunOperator.fromLocalPlayer(player).bolt();
            return ShootResult.NEED_BOLT;
        }
        // 检查是否正在奔跑
        if (gunOperator.getSynSprintTime() > 0) {
            return ShootResult.IS_SPRINTING;
        }
        // 触发开火事件
        if (new GunShootEvent(player, mainhandItem, LogicalSide.CLIENT).post()) {
            return ShootResult.FORGE_EVENT_CANCEL;
        }
        // 切换状态锁，不允许换弹、检视等行为进行。
        data.lockState(SHOOT_LOCKED_CONDITION);
        data.isShootRecorded = false;
        // 调用开火逻辑
        this.doShoot(gunIndex, iGun, mainhandItem, gunData, coolDown);
        return ShootResult.SUCCESS;
    }

    private void doShoot(ClientGunIndex gunIndex, IGun iGun, ItemStack mainhandItem, GunData gunData, long delay) {
        FireMode fireMode = iGun.getFireMode(mainhandItem);
        Bolt boltType = gunIndex.getGunData().getBolt();
        // 获取余弹数
        boolean consumeAmmo = IGunOperator.fromLivingEntity(player).consumesAmmoOrNot();
        boolean hasAmmoInBarrel = iGun.hasBulletInBarrel(mainhandItem) && boltType != Bolt.OPEN_BOLT;
        int ammoCount = consumeAmmo ? iGun.getCurrentAmmoCount(mainhandItem) + (hasAmmoInBarrel ? 1 : 0) : Integer.MAX_VALUE;
        // 连发射击间隔
        long period = fireMode == FireMode.BURST ? gunData.getBurstShootInterval() : 1;
        // 最大连发数
        final int maxCount = Math.min(ammoCount, fireMode == FireMode.BURST ? gunData.getBurstData().getCount() : 1);
        // 连发计数器
        AtomicInteger count = new AtomicInteger(0);
        LocalPlayerDataHolder.SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            if (count.get() == 0) {
                // 转换 isRecord 状态，允许下一个tick的开火检测。
                data.isShootRecorded = true;
            }
            // 如果达到最大连发次数，或者玩家已经死亡，取消任务
            if (count.get() >= maxCount || player.isDead()) {
                ScheduledFuture<?> future = (ScheduledFuture<?>) Thread.currentThread();
                future.cancel(false); // 取消当前任务
                return;
            }
            // 以下逻辑只需要执行一次
            if (count.get() == 0) {
                // 如果状态锁正在准备锁定，且不是开火的状态锁，则不允许开火(主要用于防止切枪后开火动作覆盖切枪动作)
                if (data.clientStateLock && data.lockedCondition != SHOOT_LOCKED_CONDITION && data.lockedCondition != null) {
                    return;
                }
                // 记录新的开火时间戳
                data.clientShootTimestamp = System.currentTimeMillis();
                // 发送开火的数据包，通知服务器
                NetworkHandler.sendToServer(new ClientMessagePlayerShoot());
            }
            // 触发击发事件
            boolean fire = !new GunFireEvent(player, mainhandItem, LogicalSide.CLIENT).post();
            if (fire) {
                // 动画和声音循环播放
                GunAnimationStateMachine animationStateMachine = gunIndex.getAnimationStateMachine();
                if (animationStateMachine != null) {
                    animationStateMachine.onGunShoot();
                }
                // 获取消音
                boolean useSilenceSound = this.useSilenceSound(mainhandItem, gunData);
                // 播放声音需要从异步线程上传到主线程执行。
                MinecraftClient.getInstance().submitAsync(() -> {
                    // 开火需要打断检视
                    SoundPlayManager.stopPlayGunSound(gunIndex, SoundManager.INSPECT_SOUND);
                    if (useSilenceSound) {
                        SoundPlayManager.playSilenceSound(player, gunIndex);
                    } else {
                        SoundPlayManager.playShootSound(player, gunIndex);
                    }
                });
            }
            count.getAndIncrement();
        }, delay, period, TimeUnit.MILLISECONDS);
    }

    private boolean useSilenceSound(ItemStack mainhandItem, GunData gunData) {
        final boolean[] useSilenceSound = new boolean[]{false};
        AttachmentDataUtils.getAllAttachmentData(mainhandItem, gunData, attachmentData -> {
            if (attachmentData.getSilence() != null && attachmentData.getSilence().isUseSilenceSound()) {
                useSilenceSound[0] = true;
            }
        });
        return useSilenceSound[0];
    }

    private long getCoolDown(IGun iGun, ItemStack mainHandItem, GunData gunData) {
        FireMode fireMode = iGun.getFireMode(mainHandItem);
        long coolDown;
        if (fireMode == FireMode.BURST) {
            coolDown = (long) (gunData.getBurstData().getMinInterval() * 1000f) - (System.currentTimeMillis() - data.clientShootTimestamp);
        } else {
            coolDown = gunData.getShootInterval() - (System.currentTimeMillis() - data.clientShootTimestamp);
        }
        return Math.max(coolDown, 0);
    }

    public long getClientShootCoolDown() {
        ItemStack mainHandItem = player.getMainHandStack();
        IGun iGun = IGun.getIGunOrNull(mainHandItem);
        if (iGun == null) {
            return -1;
        }
        Identifier gunId = iGun.getGunId(mainHandItem);
        Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
        return gunIndexOptional.map(commonGunIndex -> getCoolDown(iGun, mainHandItem, commonGunIndex.getGunData())).orElse(-1L);
    }
}
