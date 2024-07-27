package com.imguns.guns.client.gameplay;

import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.api.item.IGun;
import com.imguns.guns.client.animation.statemachine.GunAnimationStateMachine;
import com.imguns.guns.client.sound.SoundPlayManager;
import com.imguns.guns.network.NetworkHandler;
import com.imguns.guns.network.message.ClientMessagePlayerBoltGun;
import com.imguns.guns.resource.pojo.data.gun.Bolt;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class LocalPlayerBolt {
    private final LocalPlayerDataHolder data;
    private final ClientPlayerEntity player;

    public LocalPlayerBolt(LocalPlayerDataHolder data, ClientPlayerEntity player) {
        this.data = data;
        this.player = player;
    }

    public void bolt() {
        // 检查状态锁
        if (data.clientStateLock) {
            return;
        }
        if (data.isBolting) {
            return;
        }
        ItemStack mainhandItem = player.getMainHandStack();
        if (!(mainhandItem.getItem() instanceof IGun iGun)) {
            return;
        }
        Identifier gunId = iGun.getGunId(mainhandItem);
        TimelessAPI.getClientGunIndex(gunId).ifPresent(gunIndex -> {
            // 检查 bolt 类型是否是 manual action
            Bolt boltType = gunIndex.getGunData().getBolt();
            if (boltType != Bolt.MANUAL_ACTION) {
                return;
            }
            // 检查是否有弹药在枪膛内
            if (iGun.hasBulletInBarrel(mainhandItem)) {
                return;
            }
            // 检查弹匣内是否有子弹
            if (iGun.getCurrentAmmoCount(mainhandItem) == 0) {
                return;
            }
            // 锁上状态锁
            data.lockState(operator -> operator.getSynBoltCoolDown() >= 0);
            data.isBolting = true;
            // 发包通知服务器
            NetworkHandler.sendToServer(new ClientMessagePlayerBoltGun());
            // 播放动画和音效
            GunAnimationStateMachine animationStateMachine = gunIndex.getAnimationStateMachine();
            if (animationStateMachine != null) {
                SoundPlayManager.playBoltSound(player, gunIndex);
                animationStateMachine.onGunBolt();
            }
        });
    }

    public void tickAutoBolt() {
        ItemStack mainhandItem = player.getMainHandStack();
        if (!(mainhandItem.getItem() instanceof IGun iGun)) {
            data.isBolting = false;
            return;
        }
        bolt();
        if (data.isBolting) {
            // 对于客户端来说，膛内弹药被填入的状态同步到客户端的瞬间，bolt 过程才算完全结束
            if (iGun.hasBulletInBarrel(mainhandItem)) {
                data.isBolting = false;
            }
        }
    }
}
