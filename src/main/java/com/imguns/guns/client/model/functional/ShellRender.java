package com.imguns.guns.client.model.functional;

import com.imguns.guns.api.TimelessAPI;
import com.imguns.guns.api.item.IGun;
import com.imguns.guns.client.model.BedrockAmmoModel;
import com.imguns.guns.client.model.BedrockGunModel;
import com.imguns.guns.client.model.IFunctionalRenderer;
import com.imguns.guns.client.resource.pojo.display.gun.ShellEjection;
import com.imguns.guns.compat.iris.IrisCompat;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.concurrent.ConcurrentLinkedDeque;

public class ShellRender implements IFunctionalRenderer {
    // 抛壳队列
    private static final ConcurrentLinkedDeque<Data> SHELL_QUEUE = new ConcurrentLinkedDeque<>();
    public static boolean isSelf = false;

    private final BedrockGunModel bedrockGunModel;

    public ShellRender(BedrockGunModel bedrockGunModel) {
        this.bedrockGunModel = bedrockGunModel;
    }

    public static void addShell(Vector3f randomVelocity) {
        double xRandom = Math.random() * randomVelocity.x();
        double yRandom = Math.random() * randomVelocity.y();
        double zRandom = Math.random() * randomVelocity.z();
        Vector3f vector3f = new Vector3f((float) xRandom, (float) yRandom, (float) zRandom);
        SHELL_QUEUE.offerLast(new Data(System.currentTimeMillis(), vector3f));
    }

    public static void renderShell(Identifier gunId, MatrixStack poseStack, BedrockGunModel gunModel) {
        TimelessAPI.getClientGunIndex(gunId).ifPresent(index -> {
            ShellEjection shellEjection = index.getShellEjection();
            if (shellEjection == null) {
                SHELL_QUEUE.clear();
                return;
            }
            TimelessAPI.getClientAmmoIndex(index.getGunData().getAmmoId()).ifPresent(ammoIndex -> {
                BedrockAmmoModel model = ammoIndex.getShellModel();
                if (model == null) {
                    return;
                }
                Identifier location = ammoIndex.getShellTextureLocation();
                if (location == null) {
                    return;
                }
                long lifeTime = (long) (shellEjection.getLivingTime() * 1000);

                // 检查有没有需要踢出去的队列
                checkShellQueue(lifeTime);

                // 各种参数的获取
                Vector3f initialVelocity = shellEjection.getInitialVelocity();
                Vector3f acceleration = shellEjection.getAcceleration();
                Vector3f angularVelocity = shellEjection.getAngularVelocity();

                // 缓存一下 PoseStack
                for (Data data : SHELL_QUEUE) {
                    if (data.normal == null && data.pose == null) {
                        data.normal = new Matrix3f(poseStack.peek().getNormalMatrix());
                        data.pose = new Matrix4f(poseStack.peek().getPositionMatrix());
                    }
                }

                // 渲染抛壳
                gunModel.delegateRender((poseStack1, vertexConsumer1, transformType1, light, overlay) ->
                        SHELL_QUEUE.forEach(data -> renderSingleShell(transformType1, light, overlay, data, initialVelocity, acceleration, angularVelocity, model, location)));
            });
        });
    }

    private static void renderSingleShell(ModelTransformationMode transformType1, int light, int overlay, Data data, Vector3f initialVelocity, Vector3f acceleration, Vector3f angularVelocity, BedrockAmmoModel model, Identifier location) {
        // 再检查一次
        if (data.normal == null && data.pose == null) {
            return;
        }
        // 先初始化到缓存位置和朝向
        MatrixStack poseStack2 = new MatrixStack();
        poseStack2.peek().getNormalMatrix().mul(data.normal);
        poseStack2.peek().getPositionMatrix().mul(data.pose);

        // 获取存留时间和各种参数
        long remindTime = System.currentTimeMillis() - data.timeStamp;
        double time = remindTime / 1000.0;
        Vector3f randomOffset = data.randomOffset;

        // 位移，满足标准的匀变速直线运动
        double x = (initialVelocity.x() + randomOffset.x()) * time + 0.5 * acceleration.x() * time * time;
        double y = (initialVelocity.y() + randomOffset.y()) * time + 0.5 * acceleration.y() * time * time;
        double z = (initialVelocity.z() + randomOffset.z()) * time + 0.5 * acceleration.z() * time * time;
        poseStack2.translate(-x, -y, z);

        // 旋转
        double xw = time * angularVelocity.x();
        double yw = time * angularVelocity.y();
        double zw = time * angularVelocity.z();
        poseStack2.multiply(RotationAxis.NEGATIVE_X.rotationDegrees((float) xw));
        poseStack2.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float) yw));
        poseStack2.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) zw));
        poseStack2.translate(0, -1.5, 0);

        model.render(poseStack2, transformType1, RenderLayer.getEntityCutout(location), light, overlay);
    }

    private static void checkShellQueue(long lifeTime) {
        if (!SHELL_QUEUE.isEmpty()) {
            Data data = SHELL_QUEUE.peekFirst();
            if ((System.currentTimeMillis() - data.timeStamp) > lifeTime) {
                SHELL_QUEUE.pollFirst();
                checkShellQueue(lifeTime);
            }
        }
    }

    @Override
    public void render(MatrixStack poseStack, VertexConsumer vertexBuffer, ModelTransformationMode transformType, int light, int overlay) {
        if (IrisCompat.isRenderShadow()) {
            return;
        }
        if (!isSelf) {
            return;
        }
        ItemStack currentGunItem = bedrockGunModel.getCurrentGunItem();
        IGun iGun = IGun.getIGunOrNull(currentGunItem);
        if (iGun == null) {
            return;
        }
        Identifier gunId = iGun.getGunId(currentGunItem);
        ShellRender.renderShell(gunId, poseStack, bedrockGunModel);
    }

    public static class Data {
        public final long timeStamp;
        public final Vector3f randomOffset;

        public Matrix3f normal = null;
        public Matrix4f pose = null;

        public Data(long timeStamp, Vector3f randomOffset) {
            this.timeStamp = timeStamp;
            this.randomOffset = randomOffset;
        }
    }
}
