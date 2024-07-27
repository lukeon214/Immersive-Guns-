package com.imguns.guns.inventory.tooltip;

import com.imguns.guns.api.item.attachment.AttachmentType;
import net.minecraft.client.item.TooltipData;
import net.minecraft.util.Identifier;

public record AttachmentItemTooltip(Identifier attachmentId, AttachmentType type) implements TooltipData {
}
