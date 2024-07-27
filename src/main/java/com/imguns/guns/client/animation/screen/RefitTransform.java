package com.imguns.guns.client.animation.screen;

import com.imguns.guns.api.client.event.RenderTickEvent;
import com.imguns.guns.api.item.attachment.AttachmentType;
import com.imguns.guns.client.gui.GunRefitScreen;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RefitTransform {
    private static final float REFIT_SCREEN_TRANSFORM_TIMES = 0.25f;
    private static float refitScreenTransformProgress = 1;
    private static long refitScreenTransformTimestamp = -1;
    private static AttachmentType oldTransformType = AttachmentType.NONE;
    private static AttachmentType currentTransformType = AttachmentType.NONE;
    private static float refitScreenOpeningProgress = 0;
    private static long refitScreenOpeningTimestamp = -1;

    public static void init() {
        refitScreenTransformProgress = 1;
        refitScreenTransformTimestamp = System.currentTimeMillis();
        oldTransformType = AttachmentType.NONE;
        currentTransformType = AttachmentType.NONE;
    }

    public static float getOpeningProgress() {
        return refitScreenOpeningProgress;
    }

    @NotNull
    public static AttachmentType getOldTransformType() {
        return Objects.requireNonNullElse(oldTransformType, AttachmentType.NONE);
    }

    @NotNull
    public static AttachmentType getCurrentTransformType() {
        return Objects.requireNonNullElse(currentTransformType, AttachmentType.NONE);
    }

    public static float getTransformProgress() {
        return refitScreenTransformProgress;
    }

    public static boolean changeRefitScreenView(AttachmentType attachmentType) {
        if (refitScreenTransformProgress != 1 || refitScreenOpeningProgress != 1) {
            return false;
        }
        oldTransformType = currentTransformType;
        currentTransformType = attachmentType;
        refitScreenTransformProgress = 0;
        refitScreenTransformTimestamp = System.currentTimeMillis();
        return true;
    }

    public static void tickInterpolation(RenderTickEvent ignoredEvent) {
        // tick opening progress
        if (refitScreenOpeningTimestamp == -1) {
            refitScreenOpeningTimestamp = System.currentTimeMillis();
        }
        if (MinecraftClient.getInstance().currentScreen instanceof GunRefitScreen) {
            refitScreenOpeningProgress += (System.currentTimeMillis() - refitScreenOpeningTimestamp) / (REFIT_SCREEN_TRANSFORM_TIMES * 1000);
            if (refitScreenOpeningProgress > 1) {
                refitScreenOpeningProgress = 1;
            }
        } else {
            refitScreenOpeningProgress -= (System.currentTimeMillis() - refitScreenOpeningTimestamp) / (REFIT_SCREEN_TRANSFORM_TIMES * 1000);
            if (refitScreenOpeningProgress < 0) {
                refitScreenOpeningProgress = 0;
            }
        }
        refitScreenOpeningTimestamp = System.currentTimeMillis();
        // tick transform progress
        if (refitScreenTransformTimestamp == -1) {
            refitScreenTransformTimestamp = System.currentTimeMillis();
        }
        refitScreenTransformProgress += (System.currentTimeMillis() - refitScreenTransformTimestamp) / (REFIT_SCREEN_TRANSFORM_TIMES * 1000);
        if (refitScreenTransformProgress > 1) {
            refitScreenTransformProgress = 1;
        }
        refitScreenTransformTimestamp = System.currentTimeMillis();
    }
}
