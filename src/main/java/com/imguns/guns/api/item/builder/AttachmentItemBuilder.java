package com.imguns.guns.api.item.builder;

import com.imguns.guns.api.DefaultAssets;
import com.imguns.guns.api.item.IAttachment;
import com.imguns.guns.init.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class AttachmentItemBuilder {
    private int count = 1;
    private Identifier attachmentId = DefaultAssets.DEFAULT_ATTACHMENT_ID;
    private Identifier skinId = null;

    private AttachmentItemBuilder() {
    }

    public static AttachmentItemBuilder create() {
        return new AttachmentItemBuilder();
    }

    public AttachmentItemBuilder setCount(int count) {
        this.count = Math.max(count, 1);
        return this;
    }

    public AttachmentItemBuilder setId(Identifier id) {
        this.attachmentId = id;
        return this;
    }

    public AttachmentItemBuilder setSkinId(Identifier skinId) {
        this.skinId = skinId;
        return this;
    }

    public ItemStack build() {
        ItemStack attachment = new ItemStack(ModItems.ATTACHMENT, this.count);
        if (attachment.getItem() instanceof IAttachment iAttachment) {
            iAttachment.setAttachmentId(attachment, this.attachmentId);
            iAttachment.setSkinId(attachment, this.skinId);
        }
        return attachment;
    }
}
