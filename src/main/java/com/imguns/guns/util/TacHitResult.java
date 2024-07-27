package com.imguns.guns.util;

import com.imguns.guns.entity.EntityKineticBullet;
import net.minecraft.util.hit.EntityHitResult;

public class TacHitResult extends EntityHitResult {
    private final boolean headshot;

    public TacHitResult(EntityKineticBullet.EntityResult result) {
        super(result.getEntity(), result.getHitPos());
        this.headshot = result.isHeadshot();
    }

    public boolean isHeadshot() {
        return this.headshot;
    }
}
