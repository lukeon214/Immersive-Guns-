package com.imguns.guns.api;

import com.imguns.guns.api.client.other.IThirdPersonAnimation;
import com.imguns.guns.api.client.other.ThirdPersonManager;
import com.imguns.guns.client.resource.ClientGunPackLoader;
import com.imguns.guns.client.resource.index.ClientAmmoIndex;
import com.imguns.guns.client.resource.index.ClientAttachmentIndex;
import com.imguns.guns.crafting.GunSmithTableRecipe;
import com.imguns.guns.resource.CommonAssetManager;
import com.imguns.guns.resource.CommonGunPackLoader;
import com.imguns.guns.client.resource.index.ClientGunIndex;
import com.imguns.guns.resource.index.CommonAmmoIndex;
import com.imguns.guns.resource.index.CommonAttachmentIndex;
import com.imguns.guns.resource.index.CommonGunIndex;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class TimelessAPI {
    @Environment(EnvType.CLIENT)
    public static Optional<ClientGunIndex> getClientGunIndex(Identifier gunId) {
        return ClientGunPackLoader.getGunIndex(gunId);
    }

    @Environment(EnvType.CLIENT)
    public static Optional<ClientAttachmentIndex> getClientAttachmentIndex(Identifier attachmentId) {
        return ClientGunPackLoader.getAttachmentIndex(attachmentId);
    }

    @Environment(EnvType.CLIENT)
    public static Optional<ClientAmmoIndex> getClientAmmoIndex(Identifier ammoId) {
        return ClientGunPackLoader.getAmmoIndex(ammoId);
    }

    @Environment(EnvType.CLIENT)
    public static Set<Map.Entry<Identifier, ClientGunIndex>> getAllClientGunIndex() {
        return ClientGunPackLoader.getAllGuns();
    }

    @Environment(EnvType.CLIENT)
    public static Set<Map.Entry<Identifier, ClientAmmoIndex>> getAllClientAmmoIndex() {
        return ClientGunPackLoader.getAllAmmo();
    }

    @Environment(EnvType.CLIENT)
    public static Set<Map.Entry<Identifier, ClientAttachmentIndex>> getAllClientAttachmentIndex() {
        return ClientGunPackLoader.getAllAttachments();
    }
    public static Optional<CommonGunIndex> getCommonGunIndex(Identifier gunId) {
        return CommonGunPackLoader.getGunIndex(gunId);
    }

    public static Optional<CommonAttachmentIndex> getCommonAttachmentIndex(Identifier attachmentId) {
        return CommonGunPackLoader.getAttachmentIndex(attachmentId);
    }

    public static Optional<CommonAmmoIndex> getCommonAmmoIndex(Identifier ammoId) {
        return CommonGunPackLoader.getAmmoIndex(ammoId);
    }

    public static Optional<GunSmithTableRecipe> getRecipe(Identifier recipeId) {
        return CommonAssetManager.INSTANCE.getRecipe(recipeId);
    }

    public static Set<Map.Entry<Identifier, CommonGunIndex>> getAllCommonGunIndex() {
        return CommonGunPackLoader.getAllGuns();
    }

    public static Set<Map.Entry<Identifier, CommonAmmoIndex>> getAllCommonAmmoIndex() {
        return CommonGunPackLoader.getAllAmmo();
    }

    public static Set<Map.Entry<Identifier, CommonAttachmentIndex>> getAllCommonAttachmentIndex() {
        return CommonGunPackLoader.getAllAttachments();
    }

    public static Map<Identifier, GunSmithTableRecipe> getAllRecipes() {
        return CommonAssetManager.INSTANCE.getAllRecipes();
    }

    public static void registerThirdPersonAnimation(String name, IThirdPersonAnimation animation) {
        ThirdPersonManager.register(name, animation);
    }
}
