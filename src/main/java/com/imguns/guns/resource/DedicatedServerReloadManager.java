package com.imguns.guns.resource;

import com.mojang.brigadier.context.CommandContext;
import com.imguns.guns.resource.network.CommonGunPackNetwork;
import net.minecraft.server.command.ServerCommandSource;

public class DedicatedServerReloadManager {
    public static void loadGunPack() {
        CommonGunPackLoader.init();
        CommonGunPackLoader.reloadAsset();
        CommonGunPackLoader.reloadIndex();
        CommonGunPackLoader.reloadRecipes();
    }

    public static void reloadFromCommand(CommandContext<ServerCommandSource> context) {
        loadGunPack();
        CommonGunPackNetwork.syncClient(context.getSource().getWorld().getServer());
    }
}
