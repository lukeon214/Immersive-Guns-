package com.imguns.guns.compat.iris.pbr;

import com.imguns.guns.client.resource.texture.FilePackTexture;
import com.imguns.guns.client.resource.texture.ZipPackTexture;
import net.irisshaders.iris.texture.pbr.loader.PBRTextureLoaderRegistry;

public class PBRRegister {
    public static void registerPBRLoader() {
        PBRTextureLoaderRegistry.INSTANCE.register(FilePackTexture.class, new FilePackTexturePBRLoader());
        PBRTextureLoaderRegistry.INSTANCE.register(ZipPackTexture.class, new ZipPackTexturePBRLoader());
    }
}
