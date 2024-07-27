package com.imguns.guns.api.mixin;

import com.imguns.guns.util.item.IItemHandler;
import com.imguns.guns.util.LazyOptional;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public interface ItemHandlerCapability {

    default LazyOptional<IItemHandler> imguns$getItemHandler(@Nullable Direction facing) {
        return LazyOptional.empty();
    }

    default void imguns$invalidateItemHandler() {
    }

    default void imguns$reviveItemHandler() {
    }
}
