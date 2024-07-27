package com.imguns.guns.client.gameplay;

import com.imguns.guns.api.DefaultAssets;
import com.imguns.guns.api.LogicalSide;
import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.api.entity.IGunOperator;
import com.imguns.guns.api.event.common.GunReloadEvent;
import com.imguns.guns.api.item.IAmmo;
import com.imguns.guns.api.item.IAmmoBox;
import com.imguns.guns.api.item.IGun;
import com.imguns.guns.api.item.attachment.AttachmentType;
import com.imguns.guns.client.animation.statemachine.GunAnimationStateMachine;
import com.imguns.guns.client.resource.index.ClientGunIndex;
import com.imguns.guns.client.sound.SoundPlayManager;
import com.imguns.guns.network.NetworkHandler;
import com.imguns.guns.network.message.ClientMessagePlayerReloadGun;
import com.imguns.guns.resource.pojo.data.gun.Bolt;
import com.imguns.guns.util.AttachmentDataUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class LocalPlayerReload {
    private final LocalPlayerDataHolder data;
    private final ClientPlayerEntity player;

    public LocalPlayerReload(LocalPlayerDataHolder data, ClientPlayerEntity player) {
        this.data = data;
        this.player = player;
    }

    public void reload() {
        // 暂定只有主手可以装弹
        ItemStack mainhandItem = player.getMainHandStack();
        if (!(mainhandItem.getItem() instanceof IGun iGun)) {
            return;
        }
        Identifier gunId = iGun.getGunId(mainhandItem);
        TimelessAPI.getClientGunIndex(gunId).ifPresent(gunIndex -> {
            // 检查状态锁
            if (data.clientStateLock) {
                return;
            }
            // 弹药简单检查
            if (IGunOperator.fromLivingEntity(player).needCheckAmmo() && !inventoryHasAmmo(iGun, gunIndex, mainhandItem)) {
                return;
            }
            // 锁上状态锁
            data.lockState(operator -> operator.getSynReloadState().getStateType().isReloading());
            // 触发换弹事件
            if (new GunReloadEvent(player, player.getMainHandStack(), LogicalSide.CLIENT).post()) {
                return;
            }
            // 发包通知服务器
            NetworkHandler.sendToServer(new ClientMessagePlayerReloadGun());
            // 执行客户端 reload 相关内容
            this.doReload(iGun, gunIndex, mainhandItem);
        });
    }

    private void doReload(IGun iGun, ClientGunIndex gunIndex, ItemStack mainhandItem) {
        GunAnimationStateMachine animationStateMachine = gunIndex.getAnimationStateMachine();
        if (animationStateMachine != null) {
            Bolt boltType = gunIndex.getGunData().getBolt();
            boolean noAmmo;
            if (boltType == Bolt.OPEN_BOLT) {
                noAmmo = iGun.getCurrentAmmoCount(mainhandItem) <= 0;
            } else {
                noAmmo = !iGun.hasBulletInBarrel(mainhandItem);
            }
            // TODO 这块没完全弄好，目前还有问题
            // this.playMagExtendedAnimation(mainhandItem, iGun, animationStateMachine);
            // 触发 reload，停止播放声音
            SoundPlayManager.stopPlayGunSound();
            SoundPlayManager.playReloadSound(player, gunIndex, noAmmo);
            animationStateMachine.setNoAmmo(noAmmo).onGunReload();
        }
    }

    // TODO 这块没完全弄好，目前还有问题
    private void playMagExtendedAnimation(ItemStack mainhandItem, IGun iGun, GunAnimationStateMachine animationStateMachine) {
        Identifier extendedMagId = iGun.getAttachmentId(mainhandItem, AttachmentType.EXTENDED_MAG);
        if (!DefaultAssets.isEmptyAttachmentId(extendedMagId)) {
            TimelessAPI.getCommonAttachmentIndex(extendedMagId).ifPresent(index -> {
                animationStateMachine.setMagExtended(index.getData().getExtendedMagLevel() > 0);
            });
        }
    }

    private boolean inventoryHasAmmo(IGun iGun, ClientGunIndex gunIndex, ItemStack mainhandItem) {
        // 满弹检查也放这，这样创造模式玩家随意随便换弹
        // 满弹不需要换
        int maxAmmoCount = AttachmentDataUtils.getAmmoCountWithAttachment(mainhandItem, gunIndex.getGunData());
        if (iGun.getCurrentAmmoCount(mainhandItem) >= maxAmmoCount) {
            return false;
        }
        if (iGun.useDummyAmmo(mainhandItem)) {
            return iGun.getDummyAmmoAmount(mainhandItem) > 0;
        }
        // 背包弹药检查
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack checkAmmo = inventory.getStack(i);
            if (checkAmmo.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(mainhandItem, checkAmmo)) {
                return true;
            }
            if (checkAmmo.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(mainhandItem, checkAmmo)) {
                return true;
            }
        }
        return false;
    }
}
