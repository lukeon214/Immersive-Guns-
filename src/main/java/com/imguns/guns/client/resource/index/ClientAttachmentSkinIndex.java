package com.imguns.guns.client.resource.index;

import com.google.common.base.Preconditions;
import com.imguns.guns.client.model.BedrockAttachmentModel;
import com.imguns.guns.client.resource.ClientAssetManager;
import com.imguns.guns.client.resource.pojo.skin.attachment.AttachmentSkin;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

public class ClientAttachmentSkinIndex {
    private BedrockAttachmentModel model;
    private Identifier texture;
    private String name;

    private ClientAttachmentSkinIndex() {
    }

    public static ClientAttachmentSkinIndex getInstance(AttachmentSkin skinPojo) {
        ClientAttachmentSkinIndex index = new ClientAttachmentSkinIndex();
        checkIndex(skinPojo, index);
        checkTextureAndModel(skinPojo, index);
        return index;
    }

    private static void checkIndex(AttachmentSkin skinPojo, ClientAttachmentSkinIndex index) {
        Preconditions.checkArgument(skinPojo != null, "skin index file is empty");
    }

    private static void checkName(AttachmentSkin skinPojo, ClientAttachmentSkinIndex index) {
        index.name = skinPojo.getName();
        if (StringUtils.isBlank(index.name)) {
            index.name = "custom.immersive_guns.error.no_name";
        }
    }

    private static void checkTextureAndModel(AttachmentSkin skinPojo, ClientAttachmentSkinIndex index) {
        // 检查模型
        Identifier modelLocation = skinPojo.getModel();
        Preconditions.checkArgument(modelLocation != null, "display object missing model field");
        index.model = ClientAssetManager.INSTANCE.getOrLoadAttachmentModel(modelLocation);
        Preconditions.checkArgument(index.model != null, "there is no model data in the model file");
        // 检查默认材质
        Identifier textureLocation = skinPojo.getTexture();
        Preconditions.checkArgument(textureLocation != null, "missing default texture");
        index.texture = textureLocation;
    }

    public BedrockAttachmentModel getModel() {
        return model;
    }

    public Identifier getTexture() {
        return texture;
    }

    public String getName() {
        return name;
    }
}
