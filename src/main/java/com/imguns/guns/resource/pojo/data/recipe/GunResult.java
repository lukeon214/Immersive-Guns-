package com.imguns.guns.resource.pojo.data.recipe;

import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.imguns.guns.api.item.attachment.AttachmentType;
import net.minecraft.util.Identifier;

import java.util.EnumMap;

public class GunResult {
    @SerializedName("ammo_count")
    private int ammoCount = 0;

    @SerializedName("attachments")
    private EnumMap<AttachmentType, Identifier> attachments = Maps.newEnumMap(AttachmentType.class);

    public int getAmmoCount() {
        return ammoCount;
    }

    public EnumMap<AttachmentType, Identifier> getAttachments() {
        return attachments;
    }
}
