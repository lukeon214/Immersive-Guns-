package com.imguns.guns.client.input;

import com.imguns.guns.client.gui.compat.ClothConfigScreen;
import com.imguns.guns.compat.cloth.MenuIntegration;
import com.imguns.guns.api.client.event.InputEvent;
import com.imguns.guns.init.CompatRegistry;
import committee.nova.mkb.api.IKeyBinding;
import committee.nova.mkb.keybinding.KeyModifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import static com.imguns.guns.util.InputExtraCheck.isInGame;

@Environment(EnvType.CLIENT)
public class ConfigKey {
    public static final KeyBinding OPEN_CONFIG_KEY = new KeyBinding("key.immersive_guns.open_config.desc",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "key.category.imguns");

    public static void onOpenConfig(InputEvent.Key event) {
        if (isInGame() && event.getAction() == GLFW.GLFW_PRESS
                && OPEN_CONFIG_KEY.matchesKey(event.getKey(), event.getScanCode())
                && ((IKeyBinding) OPEN_CONFIG_KEY).getKeyModifier().equals(KeyModifier.getActiveModifier())) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player == null || player.isSpectator()) {
                return;
            }
            if (!FabricLoader.getInstance().isModLoaded(CompatRegistry.CLOTH_CONFIG)) {
                ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, ClothConfigScreen.CLOTH_CONFIG_URL);
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("gui.immersive_guns.cloth_config_warning.download"));
                MutableText component = Text.translatable("gui.immersive_guns.cloth_config_warning.tips").styled(style ->
                        style.withFormatting(Formatting.BLUE).withFormatting(Formatting.UNDERLINE).withClickEvent(clickEvent).withHoverEvent(hoverEvent));
                player.sendMessage(component);
            } else {
                CompatRegistry.checkModLoad(CompatRegistry.CLOTH_CONFIG, () -> MinecraftClient.getInstance().setScreen(MenuIntegration.getConfigScreen(null)));
            }
        }
    }
}
