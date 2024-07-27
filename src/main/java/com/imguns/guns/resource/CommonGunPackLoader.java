package com.imguns.guns.resource;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.imguns.guns.GunMod;
import com.imguns.guns.api.resource.ResourceManager;
import com.imguns.guns.config.common.OtherConfig;
import com.imguns.guns.crafting.GunSmithTableIngredient;
import com.imguns.guns.crafting.GunSmithTableResult;
import com.imguns.guns.resource.index.CommonAmmoIndex;
import com.imguns.guns.resource.index.CommonAttachmentIndex;
import com.imguns.guns.resource.index.CommonGunIndex;
import com.imguns.guns.resource.loader.asset.*;
import com.imguns.guns.resource.loader.index.CommonAmmoIndexLoader;
import com.imguns.guns.resource.loader.index.CommonAttachmentIndexLoader;
import com.imguns.guns.resource.loader.index.CommonGunIndexLoader;
import com.imguns.guns.resource.network.CommonGunPackNetwork;
import com.imguns.guns.resource.pojo.data.gun.ExtraDamage;
import com.imguns.guns.resource.serialize.*;
import com.imguns.guns.util.GetJarResources;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CommonGunPackLoader {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new Identifier.Serializer())
            .registerTypeAdapter(Pair.class, new PairSerializer())
            .registerTypeAdapter(GunSmithTableIngredient.class, new GunSmithTableIngredientSerializer())
            .registerTypeAdapter(GunSmithTableResult.class, new GunSmithTableResultSerializer())
            .registerTypeAdapter(ExtraDamage.DistanceDamagePair.class, new DistanceDamagePairSerializer())
            .registerTypeAdapter(Vec3d.class, new Vec3Serializer())
            .create();
    /**
     * 放置自定义枪械模型的目录
     */
    public static final Path FOLDER = Paths.get("config", GunMod.MOD_ID, "custom");
    /**
     * 各种 INDEX 缓存
     */
    public static final Map<Identifier, CommonGunIndex> GUN_INDEX = Maps.newHashMap();
    public static final Map<Identifier, CommonAmmoIndex> AMMO_INDEX = Maps.newHashMap();
    public static final Map<Identifier, CommonAttachmentIndex> ATTACHMENT_INDEX = Maps.newHashMap();

    /**
     * 创建存放枪包的文件夹、放入默认枪包，清空网络包缓存
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
        CommonAssetManager.INSTANCE.clearAll();

        File[] files = FOLDER.toFile().listFiles((dir, name) -> true);
        if (files != null) {
            readAsset(files);
        }
    }

    /**
     * 读取所有枪包的定义文件
     */
    public static void reloadIndex() {
        GUN_INDEX.clear();
        AMMO_INDEX.clear();

        File[] files = FOLDER.toFile().listFiles((dir, name) -> true);
        if (files != null) {
            readIndex(files);
        }
    }

    /**
     * 读取所有枪包的定义文件
     */
    public static void reloadRecipes() {
        CommonAssetManager.INSTANCE.clearRecipes();
        File[] files = FOLDER.toFile().listFiles((dir, name) -> true);
        if (files != null) {
            readRecipes(files);
        }
    }

    private static void checkDefaultPack() {
        if (!OtherConfig.DEFAULT_PACK_DEBUG.get()) {
            for (ResourceManager.ExtraEntry entry : ResourceManager.EXTRA_ENTRIES) {
                GetJarResources.copyModDirectory(entry.modMainClass(), entry.srcPath(), FOLDER, entry.extraDirName());
            }
        }
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

    private static void readZipAsset(File file) {
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> iteration = zipFile.entries();
            while (iteration.hasMoreElements()) {
                String path = iteration.nextElement().getName();
                // 加载枪械 data 文件
                GunDataLoader.load(zipFile, path);
                // 加载配件 data 文件
                AttachmentDataLoader.load(zipFile, path);
                // 配件 tag
                AttachmentTagsLoader.load(zipFile, path);
                // 枪械允许的 tag
                AllowAttachmentTagsLoader.load(zipFile, path);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private static void readDirAsset(File root) {
        if (root.isDirectory()) {
            GunDataLoader.load(root);
            AttachmentDataLoader.load(root);
            AttachmentTagsLoader.load(root);
            AllowAttachmentTagsLoader.load(root);
        }
    }

    private static void readIndex(File[] files) {
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".zip")) {
                readZipIndex(file);
            }
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles((dir, name) -> true);
                if (subFiles == null) {
                    return;
                }
                for (File namespaceFile : subFiles) {
                    readDirIndex(namespaceFile);
                }
            }
        }
    }

    private static void readZipIndex(File file) {
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> iteration = zipFile.entries();
            while (iteration.hasMoreElements()) {
                String path = iteration.nextElement().getName();
                CommonAmmoIndexLoader.loadAmmoIndex(path, zipFile);
                CommonGunIndexLoader.loadGunIndex(path, zipFile);
                CommonAttachmentIndexLoader.loadAttachmentIndex(path, zipFile);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private static void readDirIndex(File root) {
        if (root.isDirectory()) {
            try {
                CommonAmmoIndexLoader.loadAmmoIndex(root);
                CommonGunIndexLoader.loadGunIndex(root);
                CommonAttachmentIndexLoader.loadAttachmentIndex(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void readRecipes(File[] files) {
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".zip")) {
                readZipRecipes(file);
            }
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles((dir, name) -> true);
                if (subFiles == null) {
                    return;
                }
                for (File namespaceFile : subFiles) {
                    readDirRecipes(namespaceFile);
                }
            }
        }
    }

    private static void readZipRecipes(File file) {
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> iteration = zipFile.entries();
            while (iteration.hasMoreElements()) {
                String path = iteration.nextElement().getName();
                RecipeLoader.load(zipFile, path);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private static void readDirRecipes(File root) {
        if (root.isDirectory()) {
            RecipeLoader.load(root);
        }
    }

    public static Optional<CommonGunIndex> getGunIndex(Identifier registryName) {
        return Optional.ofNullable(GUN_INDEX.get(registryName));
    }

    public static Optional<CommonAmmoIndex> getAmmoIndex(Identifier registryName) {
        return Optional.ofNullable(AMMO_INDEX.get(registryName));
    }

    public static Optional<CommonAttachmentIndex> getAttachmentIndex(Identifier registryName) {
        return Optional.ofNullable(ATTACHMENT_INDEX.get(registryName));
    }

    public static Set<Map.Entry<Identifier, CommonGunIndex>> getAllGuns() {
        return GUN_INDEX.entrySet();
    }

    public static Set<Map.Entry<Identifier, CommonAmmoIndex>> getAllAmmo() {
        return AMMO_INDEX.entrySet();
    }

    public static Set<Map.Entry<Identifier, CommonAttachmentIndex>> getAllAttachments() {
        return ATTACHMENT_INDEX.entrySet();
    }
}
