package com.imguns.guns.client.resource.loader.index;

import com.imguns.guns.GunMod;
import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.client.resource.index.ClientAttachmentIndex;
import com.imguns.guns.resource.pojo.AttachmentIndexPOJO;
import net.minecraft.util.Identifier;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import static com.imguns.guns.client.resource.ClientGunPackLoader.ATTACHMENT_INDEX;

public final class ClientAttachmentIndexLoader {
    private static final Marker MARKER = MarkerFactory.getMarker("ClientAttachmentIndexLoader");

    public static void loadAttachmentIndex() {
        TimelessAPI.getAllCommonAttachmentIndex().forEach(index -> {
            Identifier id = index.getKey();
            AttachmentIndexPOJO pojo = index.getValue().getPojo();
            try {
                ATTACHMENT_INDEX.put(id, ClientAttachmentIndex.getInstance(id, pojo));
            } catch (IllegalArgumentException exception) {
                GunMod.LOGGER.warn(MARKER, "{} index file read fail!", id);
                exception.printStackTrace();
            }
        });
    }
}
