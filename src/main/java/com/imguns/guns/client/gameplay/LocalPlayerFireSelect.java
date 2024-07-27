package com.imguns.guns.client.gameplay;

import com.imguns.guns.api.LogicalSide;
import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.api.event.common.GunFireSelectEvent;
import com.imguns.guns.api.item.IGun;
import com.imguns.guns.client.animation.statemachine.GunAnimationStateMachine;
import com.imguns.guns.client.sound.SoundPlayManager;
import com.imguns.guns.network.NetworkHandler;
import com.imguns.guns.network.message.ClientMessagePlayerFireSelect;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class LocalPlayerFireSelect {
    private final LocalPlayerDataHolder data;
    private final ClientPlayerEntity player;

    public LocalPlayerFireSelect(LocalPlayerDataHolder data, ClientPlayerEntity player) {
        this.data = data;
        this.player = player;
    }

    public void fireSelect() {
        // 检查状态锁
        if (data.clientStateLock) {
            return;
        }
        // 暂定为主手
        ItemStack mainhandItem = player.getMainHandStack();
        if (!(mainhandItem.getItem() instanceof IGun iGun)) {
            return;
        }
        if (new GunFireSelectEvent(player, player.getMainHandStack(), LogicalSide.CLIENT).post()) {
            return;
        }
        Identifier gunId = iGun.getGunId(mainhandItem);
        TimelessAPI.getClientGunIndex(gunId).ifPresent(gunIndex -> {
            // 播放音效
            SoundPlayManager.playFireSelectSound(player, gunIndex);
            // 发送切换开火模式的数据包，通知服务器
            NetworkHandler.sendToServer(new ClientMessagePlayerFireSelect());
            // 动画状态机转移状态
            GunAnimationStateMachine animationStateMachine = gunIndex.getAnimationStateMachine();
            if (animationStateMachine != null) {
                animationStateMachine.onGunFireSelect();
            }
        });
    }
}
