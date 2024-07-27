package com.imguns.guns.mixin.common.item;

import com.imguns.guns.util.item.IItemHandler;
import com.imguns.guns.util.item.wrapper.InvWrapper;
import com.imguns.guns.util.LazyOptional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorseEntity.class)
public abstract class AbstractHorseEntityMixin extends AnimalEntity {

    @Shadow protected SimpleInventory items;
    @Unique
    private LazyOptional<?> itemHandler = null;

    protected AbstractHorseEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onChestedStatusChanged", at = @At("TAIL"))
    private void createInventory(CallbackInfo ci) {
        this.itemHandler = LazyOptional.of(() -> new InvWrapper(this.items));
    }

    @Override
    public LazyOptional<IItemHandler> imguns$getItemHandler(@Nullable Direction facing) {
        return isAlive() && itemHandler != null ? itemHandler.cast() : super.imguns$getItemHandler(facing);
    }

    @Override
    public void imguns$invalidateItemHandler() {
        super.imguns$invalidateItemHandler();
        if (this.itemHandler != null) {
            LazyOptional<?> oldHandler = this.itemHandler;
            this.itemHandler = null;
            oldHandler.invalidate();
        }
    }
}
