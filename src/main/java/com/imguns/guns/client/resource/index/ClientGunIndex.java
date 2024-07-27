package com.imguns.guns.client.resource.index;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.imguns.guns.GunMod;
import com.imguns.guns.api.DefaultAssets;
import com.imguns.guns.api.client.animation.AnimationController;
import com.imguns.guns.api.client.animation.Animations;
import com.imguns.guns.api.client.animation.ObjectAnimation;
import com.imguns.guns.api.client.animation.gltf.AnimationStructure;
import com.imguns.guns.client.animation.statemachine.GunAnimationStateMachine;
import com.imguns.guns.client.model.BedrockGunModel;
import com.imguns.guns.client.resource.ClientAssetManager;
import com.imguns.guns.client.resource.InternalAssetLoader;
import com.imguns.guns.client.resource.pojo.animation.bedrock.BedrockAnimationFile;
import com.imguns.guns.client.resource.pojo.display.ammo.AmmoParticle;
import com.imguns.guns.client.resource.pojo.display.gun.*;
import com.imguns.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.imguns.guns.client.resource.pojo.model.BedrockVersion;
import com.imguns.guns.resource.CommonAssetManager;
import com.imguns.guns.resource.pojo.GunIndexPOJO;
import com.imguns.guns.resource.pojo.data.gun.GunData;
import com.imguns.guns.sound.SoundManager;
import com.imguns.guns.util.ColorHex;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.command.argument.ParticleEffectArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ClientGunIndex {
    private String name;
    private String thirdPersonAnimation = "empty";
    private BedrockGunModel gunModel;
    private @Nullable Pair<BedrockGunModel, Identifier> lodModel;
    private GunAnimationStateMachine animationStateMachine;
    private @Nullable Identifier playerAnimator3rd = new Identifier(GunMod.MOD_ID, "rifle_default.player_animation");
    private Map<String, Identifier> sounds;
    private GunTransform transform;
    private GunData gunData;
    private Identifier modelTexture;
    private Identifier slotTexture;
    private Identifier hudTexture;
    private @Nullable Identifier hudEmptyTexture;
    private String type;
    private String itemType;
    private @Nullable ShellEjection shellEjection;
    private @Nullable MuzzleFlash muzzleFlash;
    private LayerGunShow offhandShow;
    private @Nullable Int2ObjectArrayMap<LayerGunShow> hotbarShow;
    private float ironZoom;
    private boolean showCrosshair = false;
    private @Nullable AmmoParticle particle;
    private float @Nullable [] tracerColor = null;

    private ClientGunIndex() {
    }

    public static ClientGunIndex getInstance(GunIndexPOJO gunIndexPOJO) throws IllegalArgumentException {
        ClientGunIndex index = new ClientGunIndex();
        checkIndex(gunIndexPOJO, index);
        GunDisplay display = checkDisplay(gunIndexPOJO);
        checkData(gunIndexPOJO, index);
        checkName(gunIndexPOJO, index);
        checkTextureAndModel(display, index);
        checkLod(display, index);
        checkSlotTexture(display, index);
        checkHUDTexture(display, index);
        checkAnimation(display, index);
        checkSounds(display, index);
        checkTransform(display, index);
        checkShellEjection(display, index);
        checkGunAmmo(display, index);
        checkMuzzleFlash(display, index);
        checkLayerGunShow(display, index);
        checkIronZoom(display, index);
        checkTextShow(display, index);
        index.showCrosshair = display.isShowCrosshair();
        return index;
    }

    private static void checkIndex(GunIndexPOJO gunIndexPOJO, ClientGunIndex index) {
        Preconditions.checkArgument(gunIndexPOJO != null, "index object file is empty");
        Preconditions.checkArgument(StringUtils.isNoneBlank(gunIndexPOJO.getType()), "index object missing type field");
        index.type = gunIndexPOJO.getType();
        index.itemType = gunIndexPOJO.getItemType();
    }

    private static void checkName(GunIndexPOJO gunIndexPOJO, ClientGunIndex index) {
        index.name = gunIndexPOJO.getName();
        if (StringUtils.isBlank(index.name)) {
            index.name = "custom.immersive_guns.error.no_name";
        }
    }

    private static void checkData(GunIndexPOJO gunIndexPOJO, ClientGunIndex index) {
        Identifier pojoData = gunIndexPOJO.getData();
        Preconditions.checkArgument(pojoData != null, "index object missing pojoData field");
        GunData data = CommonAssetManager.INSTANCE.getGunData(pojoData);
        Preconditions.checkArgument(data != null, "there is no corresponding data file");
        // 剩下的不需要校验了，Common的读取逻辑中已经校验过了
        index.gunData = data;
    }

    @NotNull
    private static GunDisplay checkDisplay(GunIndexPOJO gunIndexPOJO) {
        Identifier pojoDisplay = gunIndexPOJO.getDisplay();
        Preconditions.checkArgument(pojoDisplay != null, "index object missing display field");
        GunDisplay display = ClientAssetManager.INSTANCE.getGunDisplay(pojoDisplay);
        Preconditions.checkArgument(display != null, "there is no corresponding display file");
        return display;
    }

    private static void checkIronZoom(GunDisplay display, ClientGunIndex index) {
        index.ironZoom = display.getIronZoom();
        if (index.ironZoom < 1) {
            index.ironZoom = 1;
        }
    }

    private static void checkTextShow(GunDisplay display, ClientGunIndex index) {
        Map<String, TextShow> textShowMap = Maps.newHashMap();
        display.getTextShows().forEach((key, value) -> {
            if (StringUtils.isNoneBlank(key)) {
                int color = ColorHex.colorTextToRbgInt(value.getColorText());
                value.setColorInt(color);
                textShowMap.put(key, value);
            }
        });
        index.gunModel.setTextShowList(textShowMap);
    }

    private static void checkTextureAndModel(GunDisplay display, ClientGunIndex index) {
        // 检查模型
        Identifier modelLocation = display.getModelLocation();
        Preconditions.checkArgument(modelLocation != null, "display object missing model field");
        BedrockModelPOJO modelPOJO = ClientAssetManager.INSTANCE.getModels(modelLocation);
        Preconditions.checkArgument(modelPOJO != null, "there is no corresponding model file");
        // 检查默认材质是否存在
        Identifier textureLocation = display.getModelTexture();
        Preconditions.checkArgument(textureLocation != null, "missing default texture");
        index.modelTexture = textureLocation;
        // 先判断是不是 1.10.0 版本基岩版模型文件
        if (BedrockVersion.isLegacyVersion(modelPOJO) && modelPOJO.getGeometryModelLegacy() != null) {
            index.gunModel = new BedrockGunModel(modelPOJO, BedrockVersion.LEGACY);
        }
        // 判定是不是 1.12.0 版本基岩版模型文件
        if (BedrockVersion.isNewVersion(modelPOJO) && modelPOJO.getGeometryModelNew() != null) {
            index.gunModel = new BedrockGunModel(modelPOJO, BedrockVersion.NEW);
        }
        Preconditions.checkArgument(index.gunModel != null, "there is no model data in the model file");
    }

    private static void checkLod(GunDisplay display, ClientGunIndex index) {
        GunLod gunLod = display.getGunLod();
        if (gunLod != null) {
            Identifier texture = gunLod.getModelTexture();
            if (gunLod.getModelLocation() == null) {
                return;
            }
            if (texture == null) {
                return;
            }
            BedrockModelPOJO modelPOJO = ClientAssetManager.INSTANCE.getModels(gunLod.getModelLocation());
            if (modelPOJO == null) {
                return;
            }
            // 先判断是不是 1.10.0 版本基岩版模型文件
            if (BedrockVersion.isLegacyVersion(modelPOJO) && modelPOJO.getGeometryModelLegacy() != null) {
                BedrockGunModel model = new BedrockGunModel(modelPOJO, BedrockVersion.LEGACY);
                index.lodModel = Pair.of(model, texture);
            }
            // 判定是不是 1.12.0 版本基岩版模型文件
            if (BedrockVersion.isNewVersion(modelPOJO) && modelPOJO.getGeometryModelNew() != null) {
                BedrockGunModel model = new BedrockGunModel(modelPOJO, BedrockVersion.NEW);
                index.lodModel = Pair.of(model, texture);
            }
        }
    }

    private static void checkAnimation(GunDisplay display, ClientGunIndex index) {
        Identifier location = display.getAnimationLocation();
        AnimationController controller;
        if (location == null) {
            controller = new AnimationController(Lists.newArrayList(), index.gunModel);
        } else {
            AnimationStructure gltfAnimations = ClientAssetManager.INSTANCE.getGltfAnimations(location);
            BedrockAnimationFile bedrockAnimationFile = ClientAssetManager.INSTANCE.getBedrockAnimations(location);
            if (bedrockAnimationFile != null) {
                // 用 bedrock 动画资源创建动画控制器
                controller = Animations.createControllerFromBedrock(bedrockAnimationFile, index.gunModel);
            } else if (gltfAnimations != null) {
                // 用 gltf 动画资源创建动画控制器
                controller = Animations.createControllerFromGltf(gltfAnimations, index.gunModel);
            } else {
                throw new IllegalArgumentException("animation not found: " + location);
            }
            // 将默认动画填入动画控制器
            DefaultAnimation defaultAnimation = display.getDefaultAnimation();
            if (defaultAnimation != null) {
                switch (defaultAnimation) {
                    case RIFLE -> {
                        for (ObjectAnimation animation : InternalAssetLoader.getDefaultRifleAnimations()) {
                            controller.providePrototypeIfAbsent(animation.name, () -> new ObjectAnimation(animation));
                        }
                    }
                    case PISTOL -> {
                        for (ObjectAnimation animation : InternalAssetLoader.getDefaultPistolAnimations()) {
                            controller.providePrototypeIfAbsent(animation.name, () -> new ObjectAnimation(animation));
                        }
                    }
                }
            }
        }
        // 将动画控制器包装起来
        index.animationStateMachine = new GunAnimationStateMachine(controller);
        // 初始化第三人称动画
        if (StringUtils.isNoneBlank(display.getThirdPersonAnimation())) {
            index.thirdPersonAnimation = display.getThirdPersonAnimation();
        }
        // player animator 兼容动画
        if (display.getPlayerAnimator3rd() != null) {
            index.playerAnimator3rd = display.getPlayerAnimator3rd();
        }
    }

    private static void checkSounds(GunDisplay display, ClientGunIndex index) {
        index.sounds = Maps.newHashMap();
        Map<String, Identifier> soundMaps = display.getSounds();
        if (soundMaps == null || soundMaps.isEmpty()) {
            return;
        }
        // 部分音效为默认音效，不存在则需要添加默认音效
        soundMaps.putIfAbsent(SoundManager.DRY_FIRE_SOUND, new Identifier(GunMod.MOD_ID, SoundManager.DRY_FIRE_SOUND));
        soundMaps.putIfAbsent(SoundManager.FIRE_SELECT, new Identifier(GunMod.MOD_ID, SoundManager.FIRE_SELECT));
        soundMaps.putIfAbsent(SoundManager.HEAD_HIT_SOUND, new Identifier(GunMod.MOD_ID, SoundManager.HEAD_HIT_SOUND));
        soundMaps.putIfAbsent(SoundManager.FLESH_HIT_SOUND, new Identifier(GunMod.MOD_ID, SoundManager.FLESH_HIT_SOUND));
        soundMaps.putIfAbsent(SoundManager.KILL_SOUND, new Identifier(GunMod.MOD_ID, SoundManager.KILL_SOUND));
        soundMaps.putIfAbsent(SoundManager.MELEE_BAYONET, new Identifier(GunMod.MOD_ID, "melee_bayonet/melee_bayonet_01"));
        soundMaps.putIfAbsent(SoundManager.MELEE_STOCK, new Identifier(GunMod.MOD_ID, "melee_stock/melee_stock_01"));
        soundMaps.putIfAbsent(SoundManager.MELEE_PUSH, new Identifier(GunMod.MOD_ID, "melee_stock/melee_stock_02"));
        index.sounds.putAll(soundMaps);
    }

    private static void checkTransform(GunDisplay display, ClientGunIndex index) {
        GunTransform readTransform = display.getTransform();
        GunDisplay defaultDisplay = ClientAssetManager.INSTANCE.getGunDisplay(DefaultAssets.DEFAULT_GUN_DISPLAY);
        if (readTransform == null || readTransform.getScale() == null) {
            index.transform = Objects.requireNonNull(defaultDisplay.getTransform());
        } else {
            index.transform = display.getTransform();
        }
    }

    private static void checkSlotTexture(GunDisplay display, ClientGunIndex index) {
        // 加载 GUI 内枪械图标
        index.slotTexture = Objects.requireNonNullElseGet(display.getSlotTextureLocation(), MissingSprite::getMissingSpriteId);
    }

    private static void checkHUDTexture(GunDisplay display, ClientGunIndex index) {
        index.hudTexture = Objects.requireNonNullElseGet(display.getHudTextureLocation(), MissingSprite::getMissingSpriteId);
        index.hudEmptyTexture = display.getHudEmptyTextureLocation();
    }

    private static void checkShellEjection(GunDisplay display, ClientGunIndex index) {
        index.shellEjection = display.getShellEjection();
    }

    private static void checkGunAmmo(GunDisplay display, ClientGunIndex index) {
        GunAmmo displayGunAmmo = display.getGunAmmo();
        if (displayGunAmmo == null) {
            return;
        }
        String tracerColorText = displayGunAmmo.getTracerColor();
        if (StringUtils.isNoneBlank(tracerColorText)) {
            index.tracerColor = ColorHex.colorTextToRbgFloatArray(tracerColorText);
        }
        AmmoParticle particle = displayGunAmmo.getParticle();
        if (particle != null) {
            try {
                String name = particle.getName();
                if (StringUtils.isNoneBlank()) {
                    particle.setParticleOptions(ParticleEffectArgumentType.readParameters(new StringReader(name), Registries.PARTICLE_TYPE.getReadOnlyWrapper()));
                    Preconditions.checkArgument(particle.getCount() > 0, "particle count must be greater than 0");
                    Preconditions.checkArgument(particle.getLifeTime() > 0, "particle life time must be greater than 0");
                    index.particle = particle;
                }
            } catch (CommandSyntaxException e) {
                e.fillInStackTrace();
            }
        }
    }

    private static void checkMuzzleFlash(GunDisplay display, ClientGunIndex index) {
        index.muzzleFlash = display.getMuzzleFlash();
        if (index.muzzleFlash != null && index.muzzleFlash.getTexture() == null) {
            index.muzzleFlash = null;
        }
    }

    private static void checkLayerGunShow(GunDisplay display, ClientGunIndex index) {
        index.offhandShow = display.getOffhandShow();
        if (index.offhandShow == null) {
            index.offhandShow = new LayerGunShow();
        }
        Map<String, LayerGunShow> show = display.getHotbarShow();
        if (show == null || show.isEmpty()) {
            return;
        }
        index.hotbarShow = new Int2ObjectArrayMap<>();
        for (String key : show.keySet()) {
            try {
                index.hotbarShow.put(Integer.parseInt(key), show.get(key));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("index number is error: " + key);
            }
        }
    }

    public String getType() {
        return type;
    }

    public String getItemType() {
        return itemType;
    }

    public String getName() {
        return name;
    }

    public BedrockGunModel getGunModel() {
        return gunModel;
    }

    @Nullable
    public Pair<BedrockGunModel, Identifier> getLodModel() {
        return lodModel;
    }

    public GunAnimationStateMachine getAnimationStateMachine() {
        return animationStateMachine;
    }

    @Nullable
    public Identifier getSounds(String name) {
        return sounds.get(name);
    }

    public GunTransform getTransform() {
        return transform;
    }

    public Identifier getSlotTexture() {
        return slotTexture;
    }

    public Identifier getHUDTexture() {
        return hudTexture;
    }

    @Nullable
    public Identifier getHudEmptyTexture() {
        return hudEmptyTexture;
    }

    public Identifier getModelTexture() {
        return modelTexture;
    }

    public GunData getGunData() {
        return gunData;
    }

    public String getThirdPersonAnimation() {
        return thirdPersonAnimation;
    }

    @Nullable
    public ShellEjection getShellEjection() {
        return shellEjection;
    }

    @Nullable
    public float[] getTracerColor() {
        return tracerColor;
    }

    @Nullable
    public AmmoParticle getParticle() {
        return particle;
    }

    @Nullable
    public MuzzleFlash getMuzzleFlash() {
        return muzzleFlash;
    }

    public LayerGunShow getOffhandShow() {
        return offhandShow;
    }

    @Nullable
    public Int2ObjectArrayMap<LayerGunShow> getHotbarShow() {
        return hotbarShow;
    }

    public float getIronZoom() {
        return ironZoom;
    }

    public boolean isShowCrosshair() {
        return showCrosshair;
    }

    public @Nullable Identifier getPlayerAnimator3rd() {
        return playerAnimator3rd;
    }
}