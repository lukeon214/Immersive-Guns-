package com.imguns.guns.client.event;

import com.imguns.guns.api.DefaultAssets;
import com.imguns.guns.api.LogicalSide;
import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.api.client.event.RenderHandEvent;
import com.imguns.guns.api.client.event.RenderItemInHandBobEvent;
import com.imguns.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.imguns.guns.api.client.other.KeepingItemRenderer;
import com.imguns.guns.api.event.common.GunFireEvent;
import com.imguns.guns.api.item.IGun;
import com.imguns.guns.api.item.attachment.AttachmentType;
import com.imguns.guns.client.animation.screen.RefitTransform;
import com.imguns.guns.client.animation.statemachine.GunAnimationStateMachine;
import com.imguns.guns.client.model.BedrockAttachmentModel;
import com.imguns.guns.client.model.BedrockGunModel;
import com.imguns.guns.client.model.bedrock.BedrockModel;
import com.imguns.guns.client.model.bedrock.BedrockPart;
import com.imguns.guns.client.model.functional.MuzzleFlashRender;
import com.imguns.guns.client.model.functional.ShellRender;
import com.imguns.guns.client.resource.InternalAssetLoader;
import com.imguns.guns.client.resource.index.ClientAttachmentIndex;
import com.imguns.guns.client.resource.index.ClientGunIndex;
import com.imguns.guns.config.client.RenderConfig;
import com.imguns.guns.entity.EntityKineticBullet;
import com.imguns.guns.util.math.Easing;
import com.imguns.guns.util.math.MathUtil;
import com.imguns.guns.util.math.PerlinNoise;
import com.imguns.guns.util.math.SecondOrderDynamics;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.minecraft.client.render.model.json.ModelTransformationMode.FIRST_PERSON_LEFT_HAND;
import static net.minecraft.client.render.model.json.ModelTransformationMode.FIRST_PERSON_RIGHT_HAND;

public class FirstPersonRenderGunEvent {
    // Used to generate motion curves for aiming actions to make the action look smoother
    private static final SecondOrderDynamics AIMING_DYNAMICS = new SecondOrderDynamics(1.2f, 1.2f, 0.5f, 0);
    // For smoothing gun movement when opening the modification screen
    private static final SecondOrderDynamics REFIT_OPENING_DYNAMICS = new SecondOrderDynamics(1f, 1.2f, 0.5f, 0);
    // Smoothing for jump lag animation
    private static final SecondOrderDynamics JUMPING_DYNAMICS = new SecondOrderDynamics(0.28f, 1f, 0.65f, 0);
    private static final float JUMPING_Y_SWAY = -2f;
    private static final float JUMPING_SWAY_TIME = 0.3f;
    private static final float LANDING_SWAY_TIME = 0.15f;
    // Program animation for firearm recoil
    private static final PerlinNoise SHOOT_X_SWAY_NOISE = new PerlinNoise(-0.2f, 0.2f, 400);
    private static final PerlinNoise SHOOT_Y_ROTATION_NOISE = new PerlinNoise(-0.0136f, 0.0136f, 100);
    private static final float SHOOT_Y_SWAY = -0.1f;
    private static final float SHOOT_ANIMATION_TIME = 0.3f;

    private static float jumpingSwayProgress = 0;
    private static boolean lastOnGround = false;
    private static long jumpingTimeStamp = -1;
    private static long shootTimeStamp = -1;

    public static void onRenderHand(RenderHandEvent event) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }
        if (event.getHand() == Hand.OFF_HAND) {
            ItemStack stack = KeepingItemRenderer.getRenderer().getCurrentItem();
            if (stack.getItem() instanceof IGun) {
                event.setCanceled(true);
            }
            return;
        }
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof IGun iGun)) {
            return;
        }

        // 获取 TransformType
        ModelTransformationMode transformType;
        if (event.getHand() == Hand.MAIN_HAND) {
            transformType = FIRST_PERSON_RIGHT_HAND;
        } else {
            transformType = FIRST_PERSON_LEFT_HAND;
        }

        Identifier gunId = iGun.getGunId(stack);
        TimelessAPI.getClientGunIndex(gunId).ifPresentOrElse(gunIndex -> {
            BedrockGunModel gunModel = gunIndex.getGunModel();
            GunAnimationStateMachine animationStateMachine = gunIndex.getAnimationStateMachine();
            if (gunModel == null) {
                return;
            }
            // 在渲染之前，先更新动画，让动画数据写入模型
            if (animationStateMachine != null) {
                animationStateMachine.update(event.getPartialTick(), player);
            }

            MatrixStack poseStack = event.getPoseStack();
            poseStack.push();
            // 逆转原版施加在手上的延滞效果，改为写入模型动画数据中
            float xRotOffset = MathHelper.lerp(event.getPartialTick(), player.lastRenderPitch, player.renderPitch);
            float yRotOffset = MathHelper.lerp(event.getPartialTick(), player.lastRenderYaw, player.renderYaw);
            float xRot = player.getPitch(event.getPartialTick()) - xRotOffset;
            float yRot = player.getYaw(event.getPartialTick()) - yRotOffset;
            poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xRot * -0.1F));
            poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRot * -0.1F));
            BedrockPart rootNode = gunModel.getRootNode();
            if (rootNode != null) {
                xRot = (float) Math.tanh(xRot / 25) * 25;
                yRot = (float) Math.tanh(yRot / 25) * 25;
                rootNode.offsetX += yRot * 0.1F / 16F / 3F;
                rootNode.offsetY += -xRot * 0.1F / 16F / 3F;
                rootNode.additionalQuaternion.mul(RotationAxis.POSITIVE_X.rotationDegrees(xRot * 0.05F));
                rootNode.additionalQuaternion.mul(RotationAxis.POSITIVE_Y.rotationDegrees(yRot * 0.05F));
            }
            // 从渲染原点 (0, 24, 0) 移动到模型原点 (0, 0, 0)
            poseStack.translate(0, 1.5f, 0);
            // 基岩版模型是上下颠倒的，需要翻转过来。
            poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180f));
            // 应用持枪姿态变换，如第一人称摄像机定位
            applyFirstPersonGunTransform(player, stack, gunIndex, poseStack, gunModel, event.getPartialTick());

            // 开启第一人称弹壳和火焰渲染
            MuzzleFlashRender.isSelf = true;
            ShellRender.isSelf = true;
            {
                // 如果正在打开改装界面，则取消手臂渲染
                boolean renderHand = gunModel.getRenderHand();
                if (RefitTransform.getOpeningProgress() != 0) {
                    gunModel.setRenderHand(false);
                }
                // 调用枪械模型渲染
                //RenderType renderType = RenderType.itemEntityTranslucentCull(gunIndex.getModelTexture());
                RenderLayer renderType = RenderLayer.getEntityCutout(gunIndex.getModelTexture());
                gunModel.render(poseStack, stack, transformType, renderType, event.getPackedLight(), OverlayTexture.DEFAULT_UV);
                // 调用曳光弹渲染
                renderBulletTracer(player, poseStack, gunModel, event.getPartialTick());
                // 恢复手臂渲染
                gunModel.setRenderHand(renderHand);
                // 渲染完成后，将动画数据从模型中清除，不对其他视角下的模型渲染产生影响
                poseStack.pop();
                gunModel.cleanAnimationTransform();
            }
            // 关闭第一人称弹壳和火焰渲染
            MuzzleFlashRender.isSelf = false;
            ShellRender.isSelf = false;

            // 放这里，只有渲染了枪械，才取消后续（虽然一般来说也没有什么后续了）
            event.setCanceled(true);
        }, () -> renderBulletTracer(player, event.getPoseStack(), null, event.getPartialTick()));
    }

    private static void renderBulletTracer(ClientPlayerEntity player, MatrixStack poseStack, BedrockGunModel gunModel, float partialTicks) {
        if (!RenderConfig.FIRST_PERSON_BULLET_TRACER_ENABLE.get()) {
            return;
        }
        Optional<BedrockModel> modelOptional = InternalAssetLoader.getBedrockModel(InternalAssetLoader.DEFAULT_BULLET_MODEL);
        if (modelOptional.isEmpty()) {
            return;
        }
        BedrockModel model = modelOptional.get();
        World level = player.getWorld();
        Box renderArea = player.getBoundingBox().expand(256, 256, 256);
        for (Entity entity : level.getOtherEntities(player, renderArea, FirstPersonRenderGunEvent::bulletFromPlayer)) {
            EntityKineticBullet entityBullet = (EntityKineticBullet) entity;
            if (!entityBullet.isTracerAmmo()) {
                continue;
            }
            Vec3d deltaMovement = entityBullet.getVelocity().multiply(partialTicks, partialTicks, partialTicks);
            Vec3d entityPosition = entityBullet.getLerpedPos(0).add(deltaMovement);
            Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
            Vec3d cameraPosition = camera.getPos();
            Vec3d originCameraPosition = entityBullet.getOriginCameraPosition();
            if (originCameraPosition == null) {
                if (gunModel == null) {
                    continue;
                }
                if (gunModel.getMuzzleFlashPosPath() != null) {
                    poseStack.push();
                    for (BedrockPart bedrockPart : gunModel.getMuzzleFlashPosPath()) {
                        bedrockPart.translateAndRotateAndScale(poseStack);
                    }
                    Matrix4f pose = poseStack.peek().getPositionMatrix();
                    originCameraPosition = new Vec3d(cameraPosition.x, cameraPosition.y, cameraPosition.z);
                    entityBullet.setOriginCameraPosition(originCameraPosition);
                    entityBullet.setOriginRenderOffset(new Vec3d(pose.m30(), pose.m31(), pose.m32()));
                    poseStack.pop();
                } else {
                    continue;
                }
            }
            Vec3d originRenderOffset = entityBullet.getOriginRenderOffset();
            Vec3d alphaCameraTranslation = originCameraPosition.subtract(cameraPosition);
            double distance = entityPosition.distanceTo(originCameraPosition);
            Vec3d bulletDirection = entityPosition.subtract(originCameraPosition);
            double yRot = MathUtil.getTwoVecAngle(new Vec3d(0, 0, -1), new Vec3d(bulletDirection.x, 0, bulletDirection.z));
            double xRot = MathUtil.getTwoVecAngle(new Vec3d(bulletDirection.x, 0, bulletDirection.z), bulletDirection);
            if (yRot == -1) {
                yRot = Math.toRadians(camera.getYaw() + 180f);
            }
            if (xRot == -1) {
                xRot = Math.toRadians(camera.getPitch());
            }
            xRot *= bulletDirection.y > 0 ? -1 : 1;
            yRot *= bulletDirection.x > 0 ? 1 : -1;
            MatrixStack poseStack1 = new MatrixStack();
            // 逆转摄像机的旋转，回到起始坐标
            poseStack1.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            poseStack1.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180f));
            poseStack1.translate(alphaCameraTranslation.x, alphaCameraTranslation.y, alphaCameraTranslation.z);
            // 恢复旋转角度，应用枪口定位偏移
            poseStack1.multiply(RotationAxis.NEGATIVE_Y.rotation((float) yRot));
            poseStack1.multiply(RotationAxis.NEGATIVE_X.rotation((float) xRot));
            poseStack1.translate(originRenderOffset.x, originRenderOffset.y, originRenderOffset.z - distance);
            float trailLength = 0.5f * (float) entityBullet.getVelocity().length();
            poseStack1.translate(0, 0, -trailLength / 2);
            poseStack1.scale(0.03f, 0.03f, trailLength);

            Identifier gunId = entityBullet.getGunId();
            TimelessAPI.getClientGunIndex(gunId).ifPresent(gunIndex -> {
                float[] gunTracerColor = gunIndex.getTracerColor();
                if (gunTracerColor == null) {
                    // 如果枪械没有添加弋光弹参数，那么调用子弹的
                    Identifier ammoId = entityBullet.getAmmoId();
                    TimelessAPI.getClientAmmoIndex(ammoId).ifPresent(ammoIndex -> {
                        float[] ammoTracerColor = ammoIndex.getTracerColor();
                        RenderLayer type = RenderLayer.getEnergySwirl(InternalAssetLoader.DEFAULT_BULLET_TEXTURE, 15, 15);
                        model.render(poseStack1, ModelTransformationMode.NONE, type, LightmapTextureManager.pack(15, 15),
                                OverlayTexture.DEFAULT_UV, ammoTracerColor[0], ammoTracerColor[1], ammoTracerColor[2], 1);
                    });
                } else {
                    // 否则调用调用枪械的
                    RenderLayer type = RenderLayer.getEnergySwirl(InternalAssetLoader.DEFAULT_BULLET_TEXTURE, 15, 15);
                    model.render(poseStack1, ModelTransformationMode.NONE, type, LightmapTextureManager.pack(15, 15),
                            OverlayTexture.DEFAULT_UV, gunTracerColor[0], gunTracerColor[1], gunTracerColor[2], 1);
                }
            });
        }
    }


    /**
     * 当主手拿着枪械物品的时候，取消应用在它上面的 viewBobbing，以便应用自定义的跑步/走路动画。
     */
    public static void cancelItemInHandViewBobbing(RenderItemInHandBobEvent.BobView event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            return;
        }
        ItemStack itemStack = KeepingItemRenderer.getRenderer().getCurrentItem();
        if (IGun.getIGunOrNull(itemStack) != null) {
            event.setCanceled(true);
        }
    }

    public static void onGunFire(GunFireEvent event) {
        LogicalSide side = event.getLogicalSide();
        if (side.isClient()) {
            Entity shooter = event.getShooter();
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (!shooter.equals(player)) {
                return;
            }
            ItemStack mainhandItem = player.getMainHandStack();
            IGun iGun = IGun.getIGunOrNull(mainhandItem);
            if (iGun == null) {
                return;
            }
            TimelessAPI.getClientGunIndex(iGun.getGunId(mainhandItem)).ifPresent(gunIndex -> {
                // 记录开火时间戳，用于后坐力程序动画
                shootTimeStamp = System.currentTimeMillis();
                // 记录枪口火焰数据
                MuzzleFlashRender.onShoot();
                // 抛壳
                if (gunIndex.getShellEjection() != null) {
                    ShellRender.addShell(gunIndex.getShellEjection().getRandomVelocity());
                }
            });
        }
    }

    private static boolean bulletFromPlayer(Entity entity) {
        if (entity instanceof EntityKineticBullet entityBullet) {
            return entityBullet.getOwner() instanceof ClientPlayerEntity;
        }
        return false;
    }

    private static void applyFirstPersonGunTransform(ClientPlayerEntity player, ItemStack gunItemStack, ClientGunIndex ignoredGunIndex, MatrixStack poseStack, BedrockGunModel model, float partialTicks) {
        // 配合运动曲线，计算改装枪口的打开进度
        float refitScreenOpeningProgress = REFIT_OPENING_DYNAMICS.update(RefitTransform.getOpeningProgress());
        // 配合运动曲线，计算瞄准进度
        float aimingProgress = AIMING_DYNAMICS.update(IClientPlayerGunOperator.fromLocalPlayer(player).getClientAimingProgress(partialTicks));
        // 应用枪械动态，如后坐力、持枪跳跃等
        applyGunMovements(model, aimingProgress, partialTicks);
        // 应用各种摄像机定位组的变换（默认持枪、瞄准、改装界面等）
        applyFirstPersonPositioningTransform(poseStack, model, gunItemStack, aimingProgress, refitScreenOpeningProgress);
        // 应用动画约束变换
        applyAnimationConstraintTransform(poseStack, model, aimingProgress * (1 - refitScreenOpeningProgress));
    }

    private static void applyGunMovements(BedrockGunModel model, float aimingProgress, float partialTicks) {
        applyShootSwayAndRotation(model, aimingProgress);
        applyJumpingSway(model, partialTicks);
    }

    /**
     * 应用瞄具摄像机定位组、机瞄摄像机定位组和 Idle 摄像机定位组的变换。会在几个摄像机定位之间插值。
     */
    private static void applyFirstPersonPositioningTransform(MatrixStack poseStack, BedrockGunModel model, ItemStack stack, float aimingProgress, float refitScreenOpeningProgress) {
        IGun iGun = IGun.getIGunOrNull(stack);
        if (iGun == null) {
            return;
        }
        Matrix4f transformMatrix = new Matrix4f();
        transformMatrix.identity();
        // 应用瞄准定位
        List<BedrockPart> idleNodePath = model.getIdleSightPath();
        List<BedrockPart> aimingNodePath = null;
        Identifier scopeId = iGun.getAttachmentId(stack, AttachmentType.SCOPE);
        if (DefaultAssets.isEmptyAttachmentId(scopeId)) {
            // 未安装瞄具，使用机瞄定位组
            aimingNodePath = model.getIronSightPath();
        } else {
            // 安装瞄具，组合瞄具定位组和瞄具视野定位组
            List<BedrockPart> scopeNodePath = model.getScopePosPath();
            if (scopeNodePath != null) {
                aimingNodePath = new ArrayList<>(scopeNodePath);
                Optional<ClientAttachmentIndex> indexOptional = TimelessAPI.getClientAttachmentIndex(scopeId);
                if (indexOptional.isPresent()) {
                    BedrockAttachmentModel attachmentModel = indexOptional.get().getAttachmentModel();
                    if (attachmentModel != null && attachmentModel.getScopeViewPath() != null) {
                        aimingNodePath.addAll(attachmentModel.getScopeViewPath());
                    }
                }
            }
        }
        MathUtil.applyMatrixLerp(transformMatrix, getPositioningNodeInverse(idleNodePath), transformMatrix, (1 - refitScreenOpeningProgress));
        MathUtil.applyMatrixLerp(transformMatrix, getPositioningNodeInverse(aimingNodePath), transformMatrix, (1 - refitScreenOpeningProgress) * aimingProgress);
        // 应用改装界面开启时的定位
        float refitTransformProgress = (float) Easing.easeOutCubic(RefitTransform.getTransformProgress());
        AttachmentType oldType = RefitTransform.getOldTransformType();
        AttachmentType currentType = RefitTransform.getCurrentTransformType();
        List<BedrockPart> fromNode = model.getRefitAttachmentViewPath(oldType);
        List<BedrockPart> toNode = model.getRefitAttachmentViewPath(currentType);
        MathUtil.applyMatrixLerp(transformMatrix, getPositioningNodeInverse(fromNode), transformMatrix, refitScreenOpeningProgress);
        MathUtil.applyMatrixLerp(transformMatrix, getPositioningNodeInverse(toNode), transformMatrix, refitScreenOpeningProgress * refitTransformProgress);
        // 应用变换到 PoseStack
        poseStack.translate(0, 1.5f, 0);
        poseStack.multiplyPositionMatrix(transformMatrix);
        poseStack.translate(0, -1.5f, 0);
    }

    /**
     * 获取摄像机定位组的反相矩阵
     */
    @NotNull
    private static Matrix4f getPositioningNodeInverse(List<BedrockPart> nodePath) {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.identity();
        if (nodePath != null) {
            for (int i = nodePath.size() - 1; i >= 0; i--) {
                BedrockPart part = nodePath.get(i);
                // 计算反向的旋转
                matrix4f.rotate(RotationAxis.NEGATIVE_X.rotation(part.xRot));
                matrix4f.rotate(RotationAxis.NEGATIVE_Y.rotation(part.yRot));
                matrix4f.rotate(RotationAxis.NEGATIVE_Z.rotation(part.zRot));
                // 计算反向的位移
                if (part.getParent() != null) {
                    matrix4f.translate(-part.x / 16.0F, -part.y / 16.0F, -part.z / 16.0F);
                } else {
                    matrix4f.translate(-part.x / 16.0F, (1.5F - part.y / 16.0F), -part.z / 16.0F);
                }
            }
        }
        return matrix4f;
    }

    private static void applyShootSwayAndRotation(BedrockGunModel model, float aimingProgress) {
        BedrockPart rootNode = model.getRootNode();
        if (rootNode != null) {
            float progress = 1 - (System.currentTimeMillis() - shootTimeStamp) / (SHOOT_ANIMATION_TIME * 1000);
            if (progress < 0) {
                progress = 0;
            }
            progress = (float) Easing.easeOutCubic(progress);
            rootNode.offsetX += SHOOT_X_SWAY_NOISE.getValue() / 16 * progress * (1 - aimingProgress);
            // 基岩版模型 y 轴上下颠倒，sway 值取相反数
            rootNode.offsetY += -SHOOT_Y_SWAY / 16 * progress * (1 - aimingProgress);
            rootNode.additionalQuaternion.mul(RotationAxis.POSITIVE_Y.rotation(SHOOT_Y_ROTATION_NOISE.getValue() * progress));
        }
    }

    private static void applyJumpingSway(BedrockGunModel model, float partialTicks) {
        if (jumpingTimeStamp == -1) {
            jumpingTimeStamp = System.currentTimeMillis();
        }
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            double posY = MathHelper.lerp(partialTicks, MinecraftClient.getInstance().player.lastRenderY, MinecraftClient.getInstance().player.getY());
            float velocityY = (float) (posY - MinecraftClient.getInstance().player.lastRenderY) / partialTicks;
            if (player.isOnGround()) {
                if (!lastOnGround) {
                    jumpingSwayProgress = velocityY / -0.1f;
                    if (jumpingSwayProgress > 1) {
                        jumpingSwayProgress = 1;
                    }
                    lastOnGround = true;
                } else {
                    jumpingSwayProgress -= (System.currentTimeMillis() - jumpingTimeStamp) / (LANDING_SWAY_TIME * 1000);
                    if (jumpingSwayProgress < 0) {
                        jumpingSwayProgress = 0;
                    }
                }
            } else {
                if (lastOnGround) {
                    // 0.42 是玩家自然起跳的速度
                    jumpingSwayProgress = velocityY / 0.42f;
                    if (jumpingSwayProgress > 1) {
                        jumpingSwayProgress = 1;
                    }
                    lastOnGround = false;
                } else {
                    jumpingSwayProgress -= (System.currentTimeMillis() - jumpingTimeStamp) / (JUMPING_SWAY_TIME * 1000);
                    if (jumpingSwayProgress < 0) {
                        jumpingSwayProgress = 0;
                    }
                }
            }
        }
        jumpingTimeStamp = System.currentTimeMillis();
        float ySway = JUMPING_DYNAMICS.update(JUMPING_Y_SWAY * jumpingSwayProgress);
        BedrockPart rootNode = model.getRootNode();
        if (rootNode != null) {
            // 基岩版模型 y 轴上下颠倒，sway 值取相反数
            rootNode.offsetY += -ySway / 16;
        }
    }

    /**
     * 获取动画约束点的变换数据。
     *
     * @param originTranslation   用于输出约束点的原坐标
     * @param animatedTranslation 用于输出约束点经过动画变换之后的坐标
     * @param rotation            用于输出约束点的旋转
     */
    private static void getAnimationConstraintTransform(List<BedrockPart> nodePath, @NotNull Vector3f originTranslation, @NotNull Vector3f animatedTranslation, @NotNull Vector3f rotation) {
        if (nodePath == null) {
            return;
        }
        // 约束点动画变换矩阵
        Matrix4f animeMatrix = new Matrix4f();
        // 约束点初始变换矩阵
        Matrix4f originMatrix = new Matrix4f();
        animeMatrix.identity();
        originMatrix.identity();
        BedrockPart constrainNode = nodePath.get(nodePath.size() - 1);
        for (BedrockPart part : nodePath) {
            // 乘动画位移
            if (part != constrainNode) {
                animeMatrix.translate(part.offsetX, part.offsetY, part.offsetZ);
            }
            // 乘组位移
            if (part.getParent() != null) {
                animeMatrix.translate(part.x / 16.0F, part.y / 16.0F, part.z / 16.0F);
            } else {
                animeMatrix.translate(part.x / 16.0F, (part.y / 16.0F - 1.5F), part.z / 16.0F);
            }
            // 乘动画旋转
            if (part != constrainNode) {
                animeMatrix.rotate(part.additionalQuaternion);
            }
            // 乘组旋转
            animeMatrix.rotate(RotationAxis.POSITIVE_Z.rotation(part.zRot));
            animeMatrix.rotate(RotationAxis.POSITIVE_Y.rotation(part.yRot));
            animeMatrix.rotate(RotationAxis.POSITIVE_Z.rotation(part.xRot));

            // 乘组位移
            if (part.getParent() != null) {
                originMatrix.translate(part.x / 16.0F, part.y / 16.0F, part.z / 16.0F);
            } else {
                originMatrix.translate(part.x / 16.0F, (part.y / 16.0F - 1.5F), part.z / 16.0F);
            }
            // 乘组旋转
            originMatrix.rotate(RotationAxis.POSITIVE_Z.rotation(part.zRot));
            originMatrix.rotate(RotationAxis.POSITIVE_Y.rotation(part.yRot));
            originMatrix.rotate(RotationAxis.POSITIVE_X.rotation(part.xRot));

        }
        // 把变换数据写入输出
        animeMatrix.getTranslation(animatedTranslation);
        originMatrix.getTranslation(originTranslation);
        Vector3f animatedRotation = MathUtil.getEulerAngles(animeMatrix);
        Vector3f originRotation = MathUtil.getEulerAngles(originMatrix);
        animatedRotation.sub(originRotation);
        rotation.set(animatedRotation.x(), animatedRotation.y(), animatedRotation.z());
    }

    /**
     * 应用动画约束变换。
     *
     * @param weight 控制约束变换的权重，用于插值。
     */
    public static void applyAnimationConstraintTransform(MatrixStack poseStack, BedrockGunModel gunModel, float weight) {
        List<BedrockPart> nodePath = gunModel.getConstraintPath();
        if (nodePath == null) {
            return;
        }
        if (gunModel.getConstraintObject() == null) {
            return;
        }
        // 获取动画约束点的变换信息
        Vector3f originTranslation = new Vector3f();
        Vector3f animatedTranslation = new Vector3f();
        Vector3f rotation = new Vector3f();
        Vector3f translationICA = gunModel.getConstraintObject().translationConstraint;
        Vector3f rotationICA = gunModel.getConstraintObject().rotationConstraint;
        getAnimationConstraintTransform(nodePath, originTranslation, animatedTranslation, rotation);
        // 配合约束系数，计算约束位移需要的反向位移
        Vector3f inverseTranslation = new Vector3f(originTranslation);
        inverseTranslation.sub(animatedTranslation);
        inverseTranslation.mul(1 - translationICA.x(), 1 - translationICA.y(), 1 - translationICA.z());
        // 计算约束旋转需要的反向旋转。因需要插值，获取的是欧拉角
        Vector3f inverseRotation = new Vector3f(rotation);
        inverseRotation.mul(rotationICA.x() - 1, rotationICA.y() - 1, rotationICA.z() - 1);
        // 约束旋转
        poseStack.translate(animatedTranslation.x(), animatedTranslation.y() + 1.5f, animatedTranslation.z());
        poseStack.multiply(RotationAxis.POSITIVE_X.rotation(inverseRotation.x() * weight));
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotation(inverseRotation.y() * weight));
        poseStack.multiply(RotationAxis.POSITIVE_Z.rotation(inverseRotation.z() * weight));
        poseStack.translate(-animatedTranslation.x(), -animatedTranslation.y() - 1.5f, -animatedTranslation.z());
        // 约束位移
        Matrix4f poseMatrix = poseStack.peek().getPositionMatrix();
        poseMatrix.m30(poseMatrix.m30() - inverseTranslation.x() * weight);
        poseMatrix.m31(poseMatrix.m31() - inverseTranslation.y() * weight);
        poseMatrix.m32(poseMatrix.m32() + inverseTranslation.z() * weight);
    }
}
