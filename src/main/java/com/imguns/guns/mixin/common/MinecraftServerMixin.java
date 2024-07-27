package com.imguns.guns.mixin.common;

import com.imguns.guns.init.CommonRegistry;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "runServer", at = @At("HEAD"))
    private void runServer(CallbackInfo ci) {
        CommonRegistry.onLoadComplete();
    }
}
