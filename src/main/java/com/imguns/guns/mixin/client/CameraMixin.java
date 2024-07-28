package com.imguns.guns.mixin.client;

import com.imguns.guns.api.mixin.CameraAngles;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public class CameraMixin implements CameraAngles {

    @Shadow
    private float yaw;

    @Shadow
    private float pitch;

    @Unique
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public void setAnglesInternal(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }
}