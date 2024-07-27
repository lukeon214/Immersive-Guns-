package com.imguns.guns.mixin.client;

import com.imguns.guns.client.resource.ClientAssetManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(TranslationStorage.class)
public class TranslationStorageMixin {

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    public void getCustomLanguage(String key, String defaultValue, CallbackInfoReturnable<String> call) {
        String code = MinecraftClient.getInstance().getLanguageManager().getLanguage();
        Map<String, String> languages = ClientAssetManager.INSTANCE.getLanguages(code);
        Map<String, String> alternative = ClientAssetManager.INSTANCE.getLanguages("en_us");
        if (languages != null && languages.containsKey(key)) {
            call.setReturnValue(languages.get(key));
        } else if (alternative != null && alternative.containsKey(key)) {
            call.setReturnValue(alternative.get(key));
        }
    }

    @Inject(method = "hasTranslation", at = @At("HEAD"), cancellable = true)
    public void hasCustomLanguage(String key, CallbackInfoReturnable<Boolean> call) {
        String code = MinecraftClient.getInstance().getLanguageManager().getLanguage();
        Map<String, String> languages = ClientAssetManager.INSTANCE.getLanguages(code);
        Map<String, String> alternative = ClientAssetManager.INSTANCE.getLanguages("en_us");
        if (languages != null && languages.containsKey(key)) {
            call.setReturnValue(true);
        } else if (alternative != null && alternative.containsKey(key)) {
            call.setReturnValue(true);
        }
    }
}
