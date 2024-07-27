package com.imguns.guns.client.resource.loader.index;

import com.imguns.guns.GunMod;
import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.client.resource.index.ClientAmmoIndex;
import com.imguns.guns.resource.pojo.AmmoIndexPOJO;
import net.minecraft.util.Identifier;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import static com.imguns.guns.client.resource.ClientGunPackLoader.AMMO_INDEX;

public final class ClientAmmoIndexLoader {
    private static final Marker MARKER = MarkerFactory.getMarker("ClientGunIndexLoader");

    public static void loadAmmoIndex() {
        TimelessAPI.getAllCommonAmmoIndex().forEach(index -> {
            Identifier id = index.getKey();
            AmmoIndexPOJO pojo = index.getValue().getPojo();
            try {
                AMMO_INDEX.put(id, ClientAmmoIndex.getInstance(pojo));
            } catch (IllegalArgumentException exception) {
                GunMod.LOGGER.warn(MARKER, "{} index file read fail!", id);
                exception.printStackTrace();
            }
        });
    }
}
