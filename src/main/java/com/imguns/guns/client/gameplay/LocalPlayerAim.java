package com.imguns.guns.client.gameplay;

import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.api.entity.IGunOperator;
import com.imguns.guns.api.entity.ReloadState;
import com.imguns.guns.api.item.IGun;
import com.imguns.guns.network.NetworkHandler;
import com.imguns.guns.network.message.ClientMessagePlayerAim;
import com.imguns.guns.resource.pojo.data.gun.GunData;
import com.imguns.guns.util.AttachmentDataUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class LocalPlayerAim {
    private final LocalPlayerDataHolder data;
    private final ClientPlayerEntity player;

    public LocalPlayerAim(LocalPlayerDataHolder data, ClientPlayerEntity player) {
        this.data = data;
        this.player = player;
    }

    public void aim(boolean isAim) {
        // 暂定为主手
        ItemStack mainhandItem = player.getMainHandStack();
        if (!(mainhandItem.getItem() instanceof IGun iGun)) {
            return;
        }
        Identifier gunId = iGun.getGunId(mainhandItem);
        TimelessAPI.getClientGunIndex(gunId).ifPresent(gunIndex -> {
            data.clientIsAiming = isAim;
            // 发送切换开火模式的数据包，通知服务器
            NetworkHandler.sendToServer(new ClientMessagePlayerAim(isAim));
        });
    }

    public float getClientAimingProgress(float partialTicks) {
        return MathHelper.lerp(partialTicks, LocalPlayerDataHolder.oldAimingProgress, data.clientAimingProgress);
    }

    public boolean isAim() {
        return data.clientIsAiming;
    }

    public boolean cancelSprint(ClientPlayerEntity player, boolean pSprinting) {
        IGunOperator gunOperator = IGunOperator.fromLivingEntity(player);
        boolean isAiming = gunOperator.getSynIsAiming();
        ReloadState.StateType reloadStateType = gunOperator.getSynReloadState().getStateType();
        if (isAiming || (reloadStateType.isReloading() && !reloadStateType.isReloadFinishing())) {
            return false;
        } else {
            return pSprinting;
        }
    }

    public void tickAimingProgress() {
        ItemStack mainhandItem = player.getMainHandStack();
        // 如果主手物品不是枪械，则取消瞄准状态并将 aimingProgress 归零，返回。
        if (!(mainhandItem.getItem() instanceof IGun iGun)) {
            data.clientAimingProgress = 0;
            LocalPlayerDataHolder.oldAimingProgress = 0;
            return;
        }
        // 如果正在收枪，则不能瞄准
        if (System.currentTimeMillis() - data.clientDrawTimestamp < 0) {
            data.clientIsAiming = false;
        }
        Identifier gunId = iGun.getGunId(mainhandItem);
        TimelessAPI.getCommonGunIndex(gunId).ifPresentOrElse(index -> {
            float alphaProgress = this.getAlphaProgress(index.getGunData(), mainhandItem);
            this.aimProgressCalculate(alphaProgress);
        }, () -> {
            data.clientAimingProgress = 0;
            LocalPlayerDataHolder.oldAimingProgress = 0;
        });
    }

    private void aimProgressCalculate(float alphaProgress) {
        LocalPlayerDataHolder.oldAimingProgress = data.clientAimingProgress;
        if (data.clientIsAiming) {
            // 处于执行瞄准状态，增加 aimingProgress
            data.clientAimingProgress += alphaProgress;
            if (data.clientAimingProgress > 1) {
                data.clientAimingProgress = 1;
            }
        } else {
            // 处于取消瞄准状态，减小 aimingProgress
            data.clientAimingProgress -= alphaProgress;
            if (data.clientAimingProgress < 0) {
                data.clientAimingProgress = 0;
            }
        }
        data.clientAimingTimestamp = System.currentTimeMillis();
    }

    private float getAlphaProgress(GunData gunData, ItemStack mainhandItem) {
        final float[] aimTime = new float[]{gunData.getAimTime()};
        AttachmentDataUtils.getAllAttachmentData(mainhandItem, gunData, attachmentData -> aimTime[0] += attachmentData.getAdsAddendTime());
        aimTime[0] = Math.max(0, aimTime[0]);
        return (System.currentTimeMillis() - data.clientAimingTimestamp + 1) / (aimTime[0] * 1000);
    }
}
