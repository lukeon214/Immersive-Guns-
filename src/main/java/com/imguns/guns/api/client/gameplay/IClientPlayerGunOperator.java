package com.imguns.guns.api.client.gameplay;

import com.imguns.guns.api.entity.ShootResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * 客户端枪械操纵者
 * 目前仅用于 LocalPlayer
 */
@Environment(EnvType.CLIENT)
public interface IClientPlayerGunOperator {
    /**
     * LocalPlayer 通过 Mixin 的方式实现了这个接口
     */
    static IClientPlayerGunOperator fromLocalPlayer(ClientPlayerEntity player) {
        return (IClientPlayerGunOperator) player;
    }

    /**
     * 检查玩家能否开火，并执行客户端开火逻辑。
     *
     * @return 返回开火的结果
     */
    ShootResult shoot();

    /**
     * 执行客户端切枪逻辑。
     */
    void draw(ItemStack lastItem);

    /**
     * 客户端手动换弹
     */
    void bolt();

    /**
     * 客户端换弹
     */
    void reload();

    /**
     * 客户端检视
     */
    void inspect();

    /**
     * 客户端切换开火模式
     */
    void fireSelect();

    /**
     * 客户端瞄准
     */
    void aim(boolean isAim);

    /**
     * 客户端近战（刺刀）
     */
    void melee();

    /**
     * 客户端是否处于瞄准状态
     */
    boolean isAim();

    /**
     * 客户端瞄准进度
     *
     * @return 0-1，1 代表开镜进度到 100%
     */
    float getClientAimingProgress(float partialTicks);

    /**
     * 客户端射击冷却时间
     */
    long getClientShootCoolDown();
}
