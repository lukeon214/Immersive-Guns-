package com.imguns.guns.client.resource;

import com.google.common.collect.Maps;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.imguns.guns.GunMod;
import com.imguns.guns.api.client.animation.Animations;
import com.imguns.guns.api.client.animation.ObjectAnimation;
import com.imguns.guns.client.model.bedrock.BedrockModel;
import com.imguns.guns.client.resource.pojo.animation.bedrock.BedrockAnimationFile;
import com.imguns.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.imguns.guns.client.resource.pojo.model.BedrockVersion;
import com.imguns.guns.util.IOReader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InternalAssetLoader {
    // 曳光弹模型
    public static final Identifier DEFAULT_BULLET_TEXTURE = new Identifier(GunMod.MOD_ID, "textures/entity/basic_bullet.png");
    public static final Identifier DEFAULT_BULLET_MODEL = new Identifier(GunMod.MOD_ID, "models/bedrock/basic_bullet.json");
    // 射击标靶车
    public static final Identifier TARGET_MINECART_MODEL_LOCATION = new Identifier(GunMod.MOD_ID, "models/bedrock/target_minecart.json");
    public static final Identifier TARGET_MINECART_TEXTURE_LOCATION = new Identifier(GunMod.MOD_ID, "textures/entity/target_minecart.png");
    public static final Identifier ENTITY_EMPTY_TEXTURE = new Identifier(GunMod.MOD_ID, "textures/entity/empty.png");
    // 射击标靶
    public static final Identifier TARGET_MODEL_LOCATION = new Identifier(GunMod.MOD_ID, "models/bedrock/target.json");
    public static final Identifier TARGET_TEXTURE_LOCATION = new Identifier(GunMod.MOD_ID, "textures/block/target.png");
    // 雕像
    public static final Identifier STATUE_MODEL_LOCATION = new Identifier(GunMod.MOD_ID, "models/bedrock/statue.json");
    public static final Identifier STATUE_TEXTURE_LOCATION = new Identifier(GunMod.MOD_ID, "textures/block/statue.png");
    // 改装台
    public static final Identifier SMITH_TABLE_MODEL_LOCATION = new Identifier(GunMod.MOD_ID, "models/bedrock/gun_smith_table.json");
    public static final Identifier SMITH_TABLE_TEXTURE_LOCATION = new Identifier(GunMod.MOD_ID, "textures/block/gun_smith_table.png");
    // 默认动画
    private static final Identifier DEFAULT_PISTOL_ANIMATIONS_LOC = new Identifier(GunMod.MOD_ID, "animations/pistol_default.animation.json");
    private static final Identifier DEFAULT_RIFLE_ANIMATIONS_LOC = new Identifier(GunMod.MOD_ID, "animations/rifle_default.animation.json");
    // 内部资源缓存
    private static final Map<Identifier, BedrockModel> BEDROCK_MODELS = Maps.newHashMap();
    private static List<ObjectAnimation> defaultPistolAnimations;
    private static List<ObjectAnimation> defaultRifleAnimations;

    public static void onResourceReload() {
        // 加载默认动画文件
        BedrockAnimationFile pistolAnimationFile = loadAnimations(DEFAULT_PISTOL_ANIMATIONS_LOC);
        BedrockAnimationFile rifleAnimationFile = loadAnimations(DEFAULT_RIFLE_ANIMATIONS_LOC);
        defaultPistolAnimations = Animations.createAnimationFromBedrock(pistolAnimationFile);
        defaultRifleAnimations = Animations.createAnimationFromBedrock(rifleAnimationFile);

        // 加载代码直接调用的基岩版模型
        BEDROCK_MODELS.clear();
        loadBedrockModels(InternalAssetLoader.SMITH_TABLE_MODEL_LOCATION);
        loadBedrockModels(InternalAssetLoader.TARGET_MODEL_LOCATION);
        loadBedrockModels(InternalAssetLoader.TARGET_MINECART_MODEL_LOCATION);
        loadBedrockModels(InternalAssetLoader.DEFAULT_BULLET_MODEL);
        loadBedrockModels(InternalAssetLoader.STATUE_MODEL_LOCATION);
    }

    private static BedrockAnimationFile loadAnimations(Identifier resourceLocation) {
        try (InputStream inputStream = MinecraftClient.getInstance().getResourceManager().open(resourceLocation)) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            JsonObject json = JsonParser.parseReader(bufferedReader).getAsJsonObject();
            return ClientGunPackLoader.GSON.fromJson(json, BedrockAnimationFile.class);
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadBedrockModels(Identifier location) {
        try (InputStream stream = MinecraftClient.getInstance().getResourceManager().open(location)) {
            BedrockModelPOJO pojo = ClientGunPackLoader.GSON.fromJson(IOReader.toString(stream, StandardCharsets.UTF_8), BedrockModelPOJO.class);
            BEDROCK_MODELS.put(location, new BedrockModel(pojo, BedrockVersion.NEW));
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            e.fillInStackTrace();
        }
    }

    public static List<ObjectAnimation> getDefaultPistolAnimations() {
        return defaultPistolAnimations;
    }

    public static List<ObjectAnimation> getDefaultRifleAnimations() {
        return defaultRifleAnimations;
    }

    public static Optional<BedrockModel> getBedrockModel(Identifier location) {
        return Optional.ofNullable(BEDROCK_MODELS.get(location));
    }
}