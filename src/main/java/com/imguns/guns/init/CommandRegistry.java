package com.imguns.guns.init;

import com.imguns.guns.command.RootCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandRegistry {
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RootCommand.register(dispatcher);
        });
    }
}
