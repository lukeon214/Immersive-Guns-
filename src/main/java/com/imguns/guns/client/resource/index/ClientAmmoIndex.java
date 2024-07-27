package com.imguns.guns.client.resource.index;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.imguns.guns.api.DefaultAssets;
import com.imguns.guns.client.model.BedrockAmmoModel;
import com.imguns.guns.client.resource.ClientAssetManager;
import com.imguns.guns.client.resource.pojo.display.ammo.*;
import com.imguns.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.imguns.guns.client.resource.pojo.model.BedrockVersion;
import com.imguns.guns.resource.pojo.AmmoIndexPOJO;
import com.imguns.guns.util.ColorHex;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.command.argument.ParticleEffectArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ClientAmmoIndex {
    private String name;
    private @Nullable BedrockAmmoModel ammoModel;
    private @Nullable Identifier modelTextureLocation;
    private Identifier slotTextureLocation;
    private @Nullable BedrockAmmoModel ammoEntityModel;
    private @Nullable Identifier ammoEntityTextureLocation;
    private @Nullable BedrockAmmoModel shellModel;
    private @Nullable Identifier shellTextureLocation;
    private int stackSize;
    private @Nullable AmmoParticle particle;
    private float[] tracerColor = new float[]{1f, 1f, 1f};
    private AmmoTransform transform;
    private @Nullable String tooltipKey;

    private ClientAmmoIndex() {
    }

    public static ClientAmmoIndex getInstance(AmmoIndexPOJO clientPojo) throws IllegalArgumentException {
        ClientAmmoIndex index = new ClientAmmoIndex();
        checkIndex(clientPojo, index);
        AmmoDisplay display = checkDisplay(clientPojo);
        checkName(clientPojo, index);
        checkTextureAndModel(display, index);
        checkSlotTexture(display, index);
        checkStackSize(clientPojo, index);
        checkAmmoEntity(display, index);
        checkShell(display, index);
        checkParticle(display, index);
        checkTracerColor(display, index);
        checkTransform(display, index);
        return index;
    }

    private static void checkIndex(AmmoIndexPOJO ammoIndexPOJO, ClientAmmoIndex index) {
        Preconditions.checkArgument(ammoIndexPOJO != null, "index object file is empty");
        index.tooltipKey = ammoIndexPOJO.getTooltip();
    }

    private static void checkName(AmmoIndexPOJO ammoIndexPOJO, ClientAmmoIndex index) {
        index.name = ammoIndexPOJO.getName();
        if (StringUtils.isBlank(index.name)) {
            index.name = "custom.immersive_guns.error.no_name";
        }
    }

    @NotNull
    private static AmmoDisplay checkDisplay(AmmoIndexPOJO ammoIndexPOJO) {
        Identifier pojoDisplay = ammoIndexPOJO.getDisplay();
        Preconditions.checkArgument(pojoDisplay != null, "index object missing display field");

        AmmoDisplay display = ClientAssetManager.INSTANCE.getAmmoDisplay(pojoDisplay);
        Preconditions.checkArgument(display != null, "there is no corresponding display file");
        return display;
    }

    private static void checkTextureAndModel(AmmoDisplay display, ClientAmmoIndex index) {
        // 检查模型
        Identifier modelLocation = display.getModelLocation();
        if (modelLocation == null) {
            return;
        }
        BedrockModelPOJO modelPOJO = ClientAssetManager.INSTANCE.getModels(modelLocation);
        Preconditions.checkArgument(modelPOJO != null, "there is no corresponding model file");
        // 检查材质
        index.modelTextureLocation = display.getModelTexture();
        // 先判断是不是 1.10.0 版本基岩版模型文件
        if (BedrockVersion.isLegacyVersion(modelPOJO) && modelPOJO.getGeometryModelLegacy() != null) {
            index.ammoModel = new BedrockAmmoModel(modelPOJO, BedrockVersion.LEGACY);
        }
        // 判定是不是 1.12.0 版本基岩版模型文件
        if (BedrockVersion.isNewVersion(modelPOJO) && modelPOJO.getGeometryModelNew() != null) {
            index.ammoModel = new BedrockAmmoModel(modelPOJO, BedrockVersion.NEW);
        }
    }

    private static void checkSlotTexture(AmmoDisplay display, ClientAmmoIndex index) {
        // 加载 GUI 内枪械图标
        index.slotTextureLocation = Objects.requireNonNullElseGet(display.getSlotTextureLocation(), MissingSprite::getMissingSpriteId);
    }

    private static void checkAmmoEntity(AmmoDisplay display, ClientAmmoIndex index) {
        AmmoEntityDisplay ammoEntity = display.getAmmoEntity();
        if (ammoEntity != null && ammoEntity.getModelLocation() != null && ammoEntity.getModelTexture() != null) {
            index.ammoEntityTextureLocation = ammoEntity.getModelTexture();
            Identifier modelLocation = ammoEntity.getModelLocation();
            BedrockModelPOJO modelPOJO = ClientAssetManager.INSTANCE.getModels(modelLocation);
            if (modelPOJO == null) {
                return;
            }
            // 先判断是不是 1.10.0 版本基岩版模型文件
            if (BedrockVersion.isLegacyVersion(modelPOJO) && modelPOJO.getGeometryModelLegacy() != null) {
                index.ammoEntityModel = new BedrockAmmoModel(modelPOJO, BedrockVersion.LEGACY);
            }
            // 判定是不是 1.12.0 版本基岩版模型文件
            if (BedrockVersion.isNewVersion(modelPOJO) && modelPOJO.getGeometryModelNew() != null) {
                index.ammoEntityModel = new BedrockAmmoModel(modelPOJO, BedrockVersion.NEW);
            }
        }
    }

    private static void checkShell(AmmoDisplay display, ClientAmmoIndex index) {
        ShellDisplay shellDisplay = display.getShellDisplay();
        if (shellDisplay != null && shellDisplay.getModelLocation() != null && shellDisplay.getModelTexture() != null) {
            index.shellTextureLocation = shellDisplay.getModelTexture();
            Identifier modelLocation = shellDisplay.getModelLocation();
            BedrockModelPOJO modelPOJO = ClientAssetManager.INSTANCE.getModels(modelLocation);
            if (modelPOJO == null) {
                return;
            }
            // 先判断是不是 1.10.0 版本基岩版模型文件
            if (BedrockVersion.isLegacyVersion(modelPOJO) && modelPOJO.getGeometryModelLegacy() != null) {
                index.shellModel = new BedrockAmmoModel(modelPOJO, BedrockVersion.LEGACY);
            }
            // 判定是不是 1.12.0 版本基岩版模型文件
            if (BedrockVersion.isNewVersion(modelPOJO) && modelPOJO.getGeometryModelNew() != null) {
                index.shellModel = new BedrockAmmoModel(modelPOJO, BedrockVersion.NEW);
            }
        }
    }

    private static void checkParticle(AmmoDisplay display, ClientAmmoIndex index) {
        if (display.getParticle() != null) {
            try {
                AmmoParticle particle = display.getParticle();
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

    private static void checkTracerColor(AmmoDisplay display, ClientAmmoIndex index) {
        String tracerColorText = display.getTracerColor();
        if (StringUtils.isNoneBlank(tracerColorText)) {
            index.tracerColor = ColorHex.colorTextToRbgFloatArray(tracerColorText);
        }
    }

    private static void checkTransform(AmmoDisplay display, ClientAmmoIndex index) {
        AmmoTransform readTransform = display.getTransform();
        AmmoDisplay defaultDisplay = ClientAssetManager.INSTANCE.getAmmoDisplay(DefaultAssets.DEFAULT_AMMO_DISPLAY);
        if (readTransform == null || readTransform.getScale() == null) {
            index.transform = Objects.requireNonNull(defaultDisplay.getTransform());
        } else {
            index.transform = display.getTransform();
        }
    }

    private static void checkStackSize(AmmoIndexPOJO clientPojo, ClientAmmoIndex index) {
        index.stackSize = Math.max(clientPojo.getStackSize(), 1);
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getTooltipKey() {
        return tooltipKey;
    }

    @Nullable
    public BedrockAmmoModel getAmmoModel() {
        return ammoModel;
    }

    @Nullable
    public Identifier getModelTextureLocation() {
        return modelTextureLocation;
    }

    public Identifier getSlotTextureLocation() {
        return slotTextureLocation;
    }

    public int getStackSize() {
        return stackSize;
    }

    @Nullable
    public BedrockAmmoModel getAmmoEntityModel() {
        return ammoEntityModel;
    }

    @Nullable
    public Identifier getAmmoEntityTextureLocation() {
        return ammoEntityTextureLocation;
    }

    @Nullable
    public BedrockAmmoModel getShellModel() {
        return shellModel;
    }

    @Nullable
    public Identifier getShellTextureLocation() {
        return shellTextureLocation;
    }

    @Nullable
    public AmmoParticle getParticle() {
        return particle;
    }

    public float[] getTracerColor() {
        return tracerColor;
    }

    public AmmoTransform getTransform() {
        return transform;
    }
}
