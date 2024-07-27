package com.imguns.guns.client.resource.loader.index;

import com.imguns.guns.GunMod;
import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.client.resource.index.ClientGunIndex;
import com.imguns.guns.resource.pojo.GunIndexPOJO;
import net.minecraft.util.Identifier;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import static com.imguns.guns.client.resource.ClientGunPackLoader.GUN_INDEX;

public final class ClientGunIndexLoader {
    private static final Marker MARKER = MarkerFactory.getMarker("ClientGunIndexLoader");

    public static void loadGunIndex() {
        TimelessAPI.getAllCommonGunIndex().forEach(index -> {
            Identifier id = index.getKey();
            GunIndexPOJO pojo = index.getValue().getPojo();
            try {
                GUN_INDEX.put(id, ClientGunIndex.getInstance(pojo));
            } catch (IllegalArgumentException exception) {
                GunMod.LOGGER.warn(MARKER, "{} index file read fail!", id);
                exception.printStackTrace();
            }
        });
    }
}
