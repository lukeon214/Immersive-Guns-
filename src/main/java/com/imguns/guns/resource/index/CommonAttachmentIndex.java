package com.imguns.guns.resource.index;

import com.google.common.base.Preconditions;
import com.imguns.guns.api.item.attachment.AttachmentType;
import com.imguns.guns.resource.CommonAssetManager;
import com.imguns.guns.resource.pojo.AttachmentIndexPOJO;
import com.imguns.guns.resource.pojo.data.attachment.AttachmentData;
import net.minecraft.util.Identifier;

public class CommonAttachmentIndex {
    private AttachmentData data;
    private AttachmentType type;
    private AttachmentIndexPOJO pojo;

    private CommonAttachmentIndex() {
    }

    public static CommonAttachmentIndex getInstance(Identifier id, AttachmentIndexPOJO attachmentIndexPOJO) throws IllegalArgumentException {
        CommonAttachmentIndex index = new CommonAttachmentIndex();
        index.pojo = attachmentIndexPOJO;
        checkIndex(attachmentIndexPOJO, index);
        checkData(id, attachmentIndexPOJO, index);
        return index;
    }

    private static void checkIndex(AttachmentIndexPOJO attachmentIndexPOJO, CommonAttachmentIndex index) {
        Preconditions.checkArgument(attachmentIndexPOJO != null, "index object file is empty");
        Preconditions.checkArgument(attachmentIndexPOJO.getType() != null, "attachment type must be nonnull.");
        index.type = attachmentIndexPOJO.getType();
    }

    private static void checkData(Identifier id, AttachmentIndexPOJO attachmentIndexPOJO, CommonAttachmentIndex index) {
        Identifier pojoData = attachmentIndexPOJO.getData();
        Preconditions.checkArgument(pojoData != null, "index object missing pojoData field");
        AttachmentData data = CommonAssetManager.INSTANCE.getAttachmentData(pojoData);
        Preconditions.checkArgument(data != null, "there is no corresponding data file");
        index.data = data;
    }

    public AttachmentData getData() {
        return data;
    }

    public AttachmentType getType() {
        return type;
    }

    public AttachmentIndexPOJO getPojo() {
        return pojo;
    }
}
