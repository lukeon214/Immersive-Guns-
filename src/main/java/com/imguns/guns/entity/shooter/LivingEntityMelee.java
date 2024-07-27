package com.imguns.guns.entity.shooter;

import com.imguns.guns.api.DefaultAssets;
import com.imguns.guns.api.LogicalSide;
import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.api.event.common.GunMeleeEvent;
import com.imguns.guns.api.item.IGun;
import com.imguns.guns.api.item.attachment.AttachmentType;
import com.imguns.guns.api.item.gun.AbstractGunItem;
import com.imguns.guns.network.NetworkHandler;
import com.imguns.guns.network.message.event.ServerMessageGunMelee;
import com.imguns.guns.resource.index.CommonGunIndex;
import com.imguns.guns.resource.pojo.data.attachment.MeleeData;
import com.imguns.guns.resource.pojo.data.gun.GunDefaultMeleeData;
import com.imguns.guns.resource.pojo.data.gun.GunMeleeData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LivingEntityMelee {
    private final LivingEntity shooter;
    private final ShooterDataHolder data;
    private final LivingEntityDrawGun draw;

    public LivingEntityMelee(LivingEntity shooter, ShooterDataHolder data, LivingEntityDrawGun draw) {
        this.shooter = shooter;
        this.data = data;
        this.draw = draw;
    }

    public void melee() {
        if (data.currentGunItem == null) {
            return;
        }
        // 检查是否在切枪
        if (draw.getDrawCoolDown() != 0) {
            return;
        }
        // 检查是否在拉栓
        if (data.boltCoolDown >= 0) {
            return;
        }
        long coolDown = getMeleeCoolDown();
        if (coolDown != 0) {
            return;
        }
        ItemStack currentGunItem = data.currentGunItem.get();
        // 触发近战事件
        if (new GunMeleeEvent(shooter, currentGunItem, LogicalSide.SERVER).post()) {
            return;
        }
        NetworkHandler.sendToTrackingEntity(new ServerMessageGunMelee(shooter.getId(), currentGunItem), shooter);
        if (currentGunItem.getItem() instanceof AbstractGunItem logicGun) {
            data.meleeTimestamp = System.currentTimeMillis();

            Identifier muzzleId = logicGun.getAttachmentId(currentGunItem, AttachmentType.MUZZLE);
            MeleeData muzzleMeleeData = getMeleeData(muzzleId);
            if (muzzleMeleeData != null) {
                float prepTime = muzzleMeleeData.getPrepTime();
                data.meleePrepTickCount = (int) Math.max(0, prepTime * 20);
                return;
            }

            Identifier stockId = logicGun.getAttachmentId(currentGunItem, AttachmentType.STOCK);
            MeleeData stockMeleeData = getMeleeData(stockId);
            if (stockMeleeData != null) {
                float prepTime = stockMeleeData.getPrepTime();
                data.meleePrepTickCount = (int) Math.max(0, prepTime * 20);
                return;
            }

            Identifier gunId = logicGun.getGunId(currentGunItem);
            TimelessAPI.getCommonGunIndex(gunId).ifPresent(index -> {
                GunDefaultMeleeData defaultMeleeData = index.getGunData().getMeleeData().getDefaultMeleeData();
                if (defaultMeleeData == null) {
                    return;
                }
                float prepTime = defaultMeleeData.getPrepTime();
                data.meleePrepTickCount = (int) Math.max(0, prepTime * 20);
            });
        }
    }

    public void scheduleTickMelee() {
        if (this.data.meleePrepTickCount > 0) {
            this.data.meleePrepTickCount--;
            return;
        }
        if (this.data.meleePrepTickCount == 0) {
            this.data.meleePrepTickCount = -1;
            if (data.currentGunItem == null) {
                return;
            }
            ItemStack currentGunItem = data.currentGunItem.get();
            if (currentGunItem.getItem() instanceof AbstractGunItem logicGun) {
                logicGun.melee(this.shooter, currentGunItem);
            }
        }
    }

    public long getMeleeCoolDown() {
        if (data.currentGunItem == null) {
            return 0;
        }
        ItemStack currentGunItem = data.currentGunItem.get();
        if (!(currentGunItem.getItem() instanceof IGun iGun)) {
            return 0;
        }
        Identifier gunId = iGun.getGunId(currentGunItem);
        Optional<CommonGunIndex> gunIndex = TimelessAPI.getCommonGunIndex(gunId);
        return gunIndex.map(index -> {
            GunMeleeData meleeData = index.getGunData().getMeleeData();
            // 获取枪口，看看有没有近战数据
            Identifier muzzleId = iGun.getAttachmentId(currentGunItem, AttachmentType.MUZZLE);
            MeleeData muzzleMeleeData = getMeleeData(muzzleId);
            if (muzzleMeleeData != null) {
                return getTotalCooldownTime(meleeData, muzzleMeleeData.getCooldown());
            }

            // 枪托
            Identifier stockId = iGun.getAttachmentId(currentGunItem, AttachmentType.STOCK);
            MeleeData stockMeleeData = getMeleeData(stockId);
            if (stockMeleeData != null) {
                return getTotalCooldownTime(meleeData, stockMeleeData.getCooldown());
            }

            GunDefaultMeleeData defaultMeleeData = meleeData.getDefaultMeleeData();
            float defaultMeleeCooldownTime = defaultMeleeData == null ? 0 : defaultMeleeData.getCooldown();
            return getTotalCooldownTime(meleeData, defaultMeleeCooldownTime);
        }).orElse(-1L);
    }

    private long getTotalCooldownTime(GunMeleeData meleeData, float extraCooldownTime) {
        float totalCooldownTime = meleeData.getCooldown() + extraCooldownTime;
        long coolDown = (long) (totalCooldownTime * 1000) - (System.currentTimeMillis() - data.meleeTimestamp);
        // 给 5 ms 的窗口时间，以平衡延迟
        coolDown = coolDown - 5;
        if (coolDown < 0) {
            return 0L;
        }
        return coolDown;
    }

    @Nullable
    private MeleeData getMeleeData(Identifier attachmentId) {
        if (DefaultAssets.isEmptyAttachmentId(attachmentId)) {
            return null;
        }
        return TimelessAPI.getCommonAttachmentIndex(attachmentId).map(index -> index.getData().getMeleeData()).orElse(null);
    }
}
