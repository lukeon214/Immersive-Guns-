package com.imguns.guns.client.resource;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.imguns.guns.api.resource.ResourceManager;
import com.imguns.guns.client.resource.index.ClientAmmoIndex;
import com.imguns.guns.client.resource.index.ClientAttachmentIndex;
import com.imguns.guns.client.resource.loader.asset.*;
import com.imguns.guns.client.resource.loader.index.ClientAmmoIndexLoader;
import com.imguns.guns.client.resource.loader.index.ClientAttachmentIndexLoader;
import com.imguns.guns.client.resource.loader.index.ClientGunIndexLoader;
import com.imguns.guns.client.resource.pojo.CommonTransformObject;
import com.imguns.guns.client.resource.pojo.animation.bedrock.AnimationKeyframes;
import com.imguns.guns.client.resource.pojo.animation.bedrock.SoundEffectKeyframes;
import com.imguns.guns.client.resource.pojo.model.CubesItem;
import com.imguns.guns.client.resource.serialize.AnimationKeyframesSerializer;
import com.imguns.guns.client.resource.serialize.ItemStackSerializer;
import com.imguns.guns.client.resource.serialize.SoundEffectKeyframesSerializer;
import com.imguns.guns.client.resource.serialize.Vector3fSerializer;
import com.imguns.guns.compat.playeranimator.PlayerAnimatorCompat;
import com.imguns.guns.config.common.OtherConfig;
import com.imguns.guns.client.resource.index.ClientGunIndex;
import com.imguns.guns.resource.network.CommonGunPackNetwork;
import com.imguns.guns.util.GetJarResources;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.imguns.guns.resource.CommonGunPackLoader.FOLDER;

@Environment(EnvType.CLIENT)
public class ClientGunPackLoader {
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(Identifier.class, new Identifier.Serializer())
            .registerTypeAdapter(CubesItem.class, new CubesItem.Deserializer())
            .registerTypeAdapter(Vector3f.class, new Vector3fSerializer())
            .registerTypeAdapter(CommonTransformObject.class, new CommonTransformObject.Serializer())
            .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
            .registerTypeAdapter(AnimationKeyframes.class, new AnimationKeyframesSerializer())
            .registerTypeAdapter(SoundEffectKeyframes.class, new SoundEffectKeyframesSerializer())
            .create();

    /**
     * 储存修改过的客户端 index
     */
    public static final Map<Identifier, ClientGunIndex> GUN_INDEX = Maps.newHashMap();
    public static final Map<Identifier, ClientAmmoIndex> AMMO_INDEX = Maps.newHashMap();
    public static final Map<Identifier, ClientAttachmentIndex> ATTACHMENT_INDEX = Maps.newHashMap();

    /**
     * 创建存放枪包的文件夹、放入默认枪包
     */
    public static void init() {
        createFolder();
        checkDefaultPack();
        CommonGunPackNetwork.clear();
    }

    /**
     * 读取所有枪包的资源文件
     */
    public static void reloadAsset() {
        ClientAssetManager.INSTANCE.clearAll();

        File[] files = FOLDER.toFile().listFiles((dir, name) -> true);
        if (files != null) {
            readAsset(files);
        }
    }

    /**
     * 读取所有枪包的定义文件
     */
    public static void reloadIndex() {
        AMMO_INDEX.clear();
        GUN_INDEX.clear();
        ATTACHMENT_INDEX.clear();

        ClientAmmoIndexLoader.loadAmmoIndex();
        ClientGunIndexLoader.loadGunIndex();
        ClientAttachmentIndexLoader.loadAttachmentIndex();
    }

    private static void createFolder() {
        File folder = FOLDER.toFile();
        if (!folder.isDirectory()) {
            try {
                Files.createDirectories(folder.toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void checkDefaultPack() {
        if (!OtherConfig.DEFAULT_PACK_DEBUG.get()) {
            for (ResourceManager.ExtraEntry entry : ResourceManager.EXTRA_ENTRIES) {
                GetJarResources.copyModDirectory(entry.modMainClass(), entry.srcPath(), FOLDER, entry.extraDirName());
            }
        }
    }

    private static void readAsset(File[] files) {
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".zip")) {
                readZipAsset(file);
            }
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles((dir, name) -> true);
                if (subFiles == null) {
                    return;
                }
                for (File namespaceFile : subFiles) {
                    readDirAsset(namespaceFile);
                }
            }
        }
    }

    private static void readDirAsset(File root) {
        if (root.isDirectory()) {
            GunDisplayLoader.load(root);
            AmmoDisplayLoader.load(root);
            AttachmentDisplayLoader.load(root);
            AttachmentSkinLoader.load(root);
            AnimationLoader.load(root);
            PlayerAnimatorCompat.loadAnimationFromFile(root);
            BedrockModelLoader.load(root);
            TextureLoader.load(root);
            SoundLoader.load(root);
            LanguageLoader.load(root);
            PackInfoLoader.load(root);
        }
    }

    public static void readZipAsset(File file) {
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> iteration = zipFile.entries();
            while (iteration.hasMoreElements()) {
                String path = iteration.nextElement().getName();
                // 加载全部的 display 文件
                if (GunDisplayLoader.load(zipFile, path)) {
                    continue;
                }
                if (AmmoDisplayLoader.load(zipFile, path)) {
                    continue;
                }
                if (AttachmentDisplayLoader.load(zipFile, path)) {
                    continue;
                }
                // 加载全部的 skin 文件
                if (AttachmentSkinLoader.load(zipFile, path)) {
                    continue;
                }
                // 加载全部的 animation 文件
                if (AnimationLoader.load(zipFile, path)) {
                    continue;
                }
                // 加载 player animator 动画
                if (PlayerAnimatorCompat.loadAnimationFromZip(zipFile, path)) {
                    continue;
                }
                // 加载全部的 model 文件
                if (BedrockModelLoader.load(zipFile, path)) {
                    continue;
                }
                // 加载全部的 texture 文件
                if (TextureLoader.load(zipFile, path)) {
                    continue;
                }
                // 加载全部的 sound 文件
                if (SoundLoader.load(zipFile, path)) {
                    continue;
                }
                // 加载语言文件
                if (LanguageLoader.load(zipFile, path)) {
                    continue;
                }
                // 加载信息文件
                PackInfoLoader.load(zipFile, path);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static Set<Map.Entry<Identifier, ClientGunIndex>> getAllGuns() {
        return GUN_INDEX.entrySet();
    }

    public static Set<Map.Entry<Identifier, ClientAmmoIndex>> getAllAmmo() {
        return AMMO_INDEX.entrySet();
    }

    public static Set<Map.Entry<Identifier, ClientAttachmentIndex>> getAllAttachments() {
        return ATTACHMENT_INDEX.entrySet();
    }

    public static Optional<ClientGunIndex> getGunIndex(Identifier registryName) {
        return Optional.ofNullable(GUN_INDEX.get(registryName));
    }

    public static Optional<ClientAmmoIndex> getAmmoIndex(Identifier registryName) {
        return Optional.ofNullable(AMMO_INDEX.get(registryName));
    }

    public static Optional<ClientAttachmentIndex> getAttachmentIndex(Identifier registryName) {
        return Optional.ofNullable(ATTACHMENT_INDEX.get(registryName));
    }
}
