package com.imguns.guns.event.ammo;

import com.imguns.guns.api.event.server.AmmoHitBlockEvent;
import com.imguns.guns.config.common.AmmoConfig;
import com.imguns.guns.entity.EntityKineticBullet;
import net.minecraft.block.*;
import net.minecraft.block.enums.Instrument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DestroyGlassBlock {

    public static void onAmmoHitBlock(AmmoHitBlockEvent event) {
        World level = event.getLevel();
        BlockState state = event.getState();
        BlockPos pos = event.getHitResult().getBlockPos();
        EntityKineticBullet ammo = event.getAmmo();
        Block stateBlock = state.getBlock();
        Instrument instrument = state.getInstrument();
        if (AmmoConfig.DESTROY_GLASS.get() && (stateBlock instanceof AbstractGlassBlock ||
                stateBlock instanceof StainedGlassPaneBlock ||
                (stateBlock instanceof PaneBlock && instrument.equals(Instrument.HAT)))) {
            level.breakBlock(pos, false, ammo.getOwner());
        }
    }
}
