package com.imguns.guns.api.event.common;

import com.imguns.guns.api.LogicalSide;
import com.imguns.guns.api.event.GunBaseEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class GunShootEvent extends GunBaseEvent {
    public static final Event<Callback> EVENT = EventFactory.createArrayBacked(Callback.class, callbacks -> event -> {
        for (Callback e : callbacks) e.onGunShoot(event);
    });

    private final LivingEntity shooter;
    private final ItemStack gunItemStack;
    private final LogicalSide logicalSide;

    public GunShootEvent(LivingEntity shooter, ItemStack gunItemStack, LogicalSide side) {
        this.shooter = shooter;
        this.gunItemStack = gunItemStack;
        this.logicalSide = side;
    }

    public LivingEntity getShooter() {
        return shooter;
    }

    public ItemStack getGunItemStack() {
        return gunItemStack;
    }

    public LogicalSide getLogicalSide() {
        return logicalSide;
    }

    @Override
    public void sendEvent() {
        EVENT.invoker().onGunShoot(this);
    }

    public interface Callback {
        void onGunShoot(GunShootEvent event);
    }
}
