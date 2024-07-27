package com.imguns.guns.client.gameplay;

import com.imguns.guns.api.DefaultAssets;
import com.imguns.guns.api.LogicalSide;
import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.api.event.common.GunMeleeEvent;
import com.imguns.guns.api.item.IGun;
import com.imguns.guns.api.item.attachment.AttachmentType;
import com.imguns.guns.client.animation.statemachine.GunAnimationConstant;
import com.imguns.guns.client.animation.statemachine.GunAnimationStateMachine;
import com.imguns.guns.client.sound.SoundPlayManager;
import com.imguns.guns.network.NetworkHandler;
import com.imguns.guns.network.message.ClientMessagePlayerMelee;
import com.imguns.guns.resource.pojo.data.attachment.MeleeData;
import com.imguns.guns.resource.pojo.data.gun.GunDefaultMeleeData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class LocalPlayerMelee {
    private final LocalPlayerDataHolder data;
    private final ClientPlayerEntity player;
    private int meleeCounter = 0;

    public LocalPlayerMelee(LocalPlayerDataHolder data, ClientPlayerEntity player) {
        this.data = data;
        this.player = player;
    }

    public void melee() {
        // 检查状态锁
        if (data.clientStateLock) {
            return;
        }
        // 暂定为主手
        ItemStack mainhandItem = player.getMainHandStack();
        if (!(mainhandItem.getItem() instanceof IGun iGun)) {
            return;
        }
        Identifier gunId = iGun.getGunId(mainhandItem);
        // 先检查枪口有没有近战属性
        Identifier muzzleId = iGun.getAttachmentId(mainhandItem, AttachmentType.MUZZLE);
        MeleeData muzzleMeleeData = getMeleeData(muzzleId);
        if (muzzleMeleeData != null) {
            this.doMuzzleMelee(gunId);
            return;
        }

        Identifier stockId = iGun.getAttachmentId(mainhandItem, AttachmentType.STOCK);
        MeleeData stockMeleeData = getMeleeData(stockId);
        if (stockMeleeData != null) {
            this.doStockMelee(gunId);
            return;
        }

        TimelessAPI.getClientGunIndex(gunId).ifPresent(index -> {
            GunDefaultMeleeData defaultMeleeData = index.getGunData().getMeleeData().getDefaultMeleeData();
            if (defaultMeleeData == null) {
                return;
            }
            String animationType = defaultMeleeData.getAnimationType();
            if (GunAnimationConstant.MELEE_STOCK_ANIMATION.equals(animationType)) {
                this.doStockMelee(gunId);
                return;
            }
            this.doPushMelee(gunId);
        });
    }

    private boolean prepareMelee() {
        // 锁上状态锁
        data.lockState(operator -> operator.getSynMeleeCoolDown() > 0);
        // 触发近战事件
        GunMeleeEvent gunMeleeEvent = new GunMeleeEvent(player, player.getMainHandStack(), LogicalSide.CLIENT);
        return !gunMeleeEvent.post();
    }

    private void doMuzzleMelee(Identifier gunId) {
        if (prepareMelee()) {
            TimelessAPI.getClientGunIndex(gunId).ifPresent(gunIndex -> {
                // 播放音效
                SoundPlayManager.playMeleeBayonetSound(player, gunIndex);
                // 发送切换开火模式的数据包，通知服务器
                NetworkHandler.sendToServer(new ClientMessagePlayerMelee());
                // 动画状态机转移状态
                GunAnimationStateMachine animationStateMachine = gunIndex.getAnimationStateMachine();
                if (animationStateMachine != null) {
                    animationStateMachine.onBayonetAttack(meleeCounter);
                    meleeCounter = (meleeCounter + 1) % 3;
                }
            });
        }
    }

    private void doStockMelee(Identifier gunId) {
        if (prepareMelee()) {
            TimelessAPI.getClientGunIndex(gunId).ifPresent(gunIndex -> {
                // 播放音效
                SoundPlayManager.playMeleeStockSound(player, gunIndex);
                // 发送切换开火模式的数据包，通知服务器
                NetworkHandler.sendToServer(new ClientMessagePlayerMelee());
                // 动画状态机转移状态
                GunAnimationStateMachine animationStateMachine = gunIndex.getAnimationStateMachine();
                if (animationStateMachine != null) {
                    animationStateMachine.onStockAttack();
                }
            });
        }
    }

    private void doPushMelee(Identifier gunId) {
        if (prepareMelee()) {
            TimelessAPI.getClientGunIndex(gunId).ifPresent(gunIndex -> {
                // 播放音效
                SoundPlayManager.playMeleePushSound(player, gunIndex);
                // 发送切换开火模式的数据包，通知服务器
                NetworkHandler.sendToServer(new ClientMessagePlayerMelee());
                // 动画状态机转移状态
                GunAnimationStateMachine animationStateMachine = gunIndex.getAnimationStateMachine();
                if (animationStateMachine != null) {
                    animationStateMachine.onPushAttack();
                }
            });
        }
    }

    @Nullable
    private MeleeData getMeleeData(Identifier attachmentId) {
        if (DefaultAssets.isEmptyAttachmentId(attachmentId)) {
            return null;
        }
        return TimelessAPI.getClientAttachmentIndex(attachmentId).map(index -> index.getData().getMeleeData()).orElse(null);
    }
}
