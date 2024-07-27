package com.imguns.guns.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.imguns.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.imguns.guns.client.model.bedrock.BedrockPart;
import com.imguns.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.imguns.guns.client.resource.pojo.model.BedrockVersion;
import com.imguns.guns.compat.iris.IrisCompat;
import com.imguns.guns.util.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class BedrockAttachmentModel extends BedrockAnimatedModel {
    private static final String SCOPE_VIEW_NODE = "scope_view";
    private static final String SCOPE_BODY_NODE = "scope_body";
    private static final String OCULAR_RING_NODE = "ocular_ring";
    private static final String DIVISION_NODE = "division";
    private static final String OCULAR_NODE = "ocular";

    protected @Nullable List<BedrockPart> scopeViewPath;
    protected @Nullable List<BedrockPart> scopeBodyPath;
    protected @Nullable List<BedrockPart> ocularRingPath;
    protected @Nullable List<BedrockPart> ocularNodePath;
    protected @Nullable List<BedrockPart> divisionNodePath;

    private boolean isScope = true;
    private boolean isSight = false;
    private float scopeViewRadiusModifier = 1;

    public BedrockAttachmentModel(BedrockModelPOJO pojo, BedrockVersion version) {
        super(pojo, version);
        scopeViewPath = getPath(modelMap.get(SCOPE_VIEW_NODE));
        scopeBodyPath = getPath(modelMap.get(SCOPE_BODY_NODE));
        ocularRingPath = getPath(modelMap.get(OCULAR_RING_NODE));
        ocularNodePath = getPath(modelMap.get(OCULAR_NODE));
        divisionNodePath = getPath(modelMap.get(DIVISION_NODE));
        if (divisionNodePath != null) {
            divisionNodePath.get(divisionNodePath.size() - 1).visible = false;
        }
    }

    @Nullable
    public List<BedrockPart> getScopeViewPath() {
        return scopeViewPath;
    }

    public void setIsScope(boolean isScope) {
        if (isScope) {
            this.isSight = false;
        }
        this.isScope = isScope;
    }

    public void setIsSight(boolean isSight) {
        if (isSight) {
            this.isScope = false;
        }
        this.isSight = isSight;
    }

    public boolean isScope() {
        return isScope;
    }

    public boolean isSight() {
        return isSight;
    }

    public void setScopeViewRadiusModifier(float scopeViewRadiusModifier) {
        this.scopeViewRadiusModifier = scopeViewRadiusModifier;
    }

    @Override
    public void render(MatrixStack matrixStack, ModelTransformationMode transformType, RenderLayer renderType, int light, int overlay) {
        if (transformType.isFirstPerson()) {
            if (isScope) {
                renderScope(matrixStack, transformType, renderType, light, overlay);
            } else if (isSight) {
                renderSight(matrixStack, transformType, renderType, light, overlay);
            }
        } else {
            if (scopeBodyPath != null) {
                renderTempPart(matrixStack, transformType, renderType, light, overlay, scopeBodyPath);
            }
            if (ocularRingPath != null) {
                renderTempPart(matrixStack, transformType, renderType, light, overlay, ocularRingPath);
            }
        }
        super.render(matrixStack, transformType, renderType, light, overlay);
    }

    private void renderSight(MatrixStack matrixStack, ModelTransformationMode transformType, RenderLayer renderType, int light, int overlay) {
        RenderHelper.enableItemEntityStencilTest();
        if (ocularNodePath != null) {
            RenderSystem.colorMask(false, false, false, false);
            RenderSystem.depthMask(false);
            // 清空模板缓冲区、准备绘制模板缓冲
            RenderSystem.clearStencil(0);
            RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            RenderSystem.stencilMask(0xFF);
            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
            // 绘制目镜
            renderTempPart(matrixStack, transformType, renderType, light, overlay, ocularNodePath);
            // 恢复渲染状态
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            RenderSystem.depthMask(true);
            RenderSystem.colorMask(true, true, true, true);
        }
        // 渲染划分
        if (divisionNodePath != null) {
            RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            RenderSystem.disableDepthTest();
            renderTempPart(matrixStack, transformType, renderType, light, overlay, divisionNodePath);
            RenderSystem.enableDepthTest();
        }
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        RenderHelper.disableItemEntityStencilTest();
        // 渲染其他部分
        if (scopeBodyPath != null) {
            renderTempPart(matrixStack, transformType, renderType, light, overlay, scopeBodyPath);
        }
        super.render(matrixStack, transformType, renderType, light, overlay);
    }

    private Vector3f getBedrockPartCenter(MatrixStack poseStack, @NotNull List<BedrockPart> path) {
        poseStack.push();
        for (BedrockPart part : path) {
            part.translateAndRotateAndScale(poseStack);
        }
        Vector3f result = new Vector3f(poseStack.peek().getPositionMatrix().m30(), poseStack.peek().getPositionMatrix().m31(), poseStack.peek().getPositionMatrix().m32());
        poseStack.pop();
        return result;
    }

    private void renderTempPart(MatrixStack poseStack, ModelTransformationMode transformType, RenderLayer renderType,
                                int light, int overlay, @NotNull List<BedrockPart> path) {
        poseStack.push();
        for (int i = 0; i < path.size() - 1; ++i) {
            path.get(i).translateAndRotateAndScale(poseStack);
        }
        BedrockPart part = path.get(path.size() - 1);
        part.visible = true;
        VertexConsumerProvider.Immediate bufferSource = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        part.render(poseStack, transformType, vertexConsumer, light, overlay);
        if (IrisCompat.endBatch(bufferSource)) {
            bufferSource.draw(renderType);
        }
        part.visible = false;
        poseStack.pop();
    }

    private void renderScope(MatrixStack matrixStack, ModelTransformationMode transformType, RenderLayer renderType, int light, int overlay) {
        RenderHelper.enableItemEntityStencilTest();
        // 清空模板缓冲区、准备绘制模板缓冲
        RenderSystem.clearStencil(0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
        if (ocularRingPath != null) {
            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            // 渲染目镜外环
            renderTempPart(matrixStack, transformType, renderType, light, overlay, ocularRingPath);
        }
        if (ocularNodePath != null) {
            RenderSystem.colorMask(false, false, false, false);
            RenderSystem.depthMask(false);
            RenderSystem.stencilMask(0xFF);
            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
            // 绘制目镜
            renderTempPart(matrixStack, transformType, renderType, light, overlay, ocularNodePath);
            // 恢复渲染状态
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            RenderSystem.depthMask(true);
            RenderSystem.colorMask(true, true, true, true);
        }
        if (scopeBodyPath != null) {
            RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 1, 0xFF);
            renderTempPart(matrixStack, transformType, renderType, light, overlay, scopeBodyPath);
        }
        BufferBuilder builder = Tessellator.getInstance().getBuffer();
        // 渲染圆形模板层
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(false);
        Vector3f ocularCenter = getBedrockPartCenter(matrixStack, ocularNodePath);
        float centerX = ocularCenter.x() * 16 * 90;
        float centerY = ocularCenter.y() * 16 * 90;
        // 80是一个随便找的大小合适的数值。
        float rad = 80 * scopeViewRadiusModifier;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            rad *= IClientPlayerGunOperator.fromLocalPlayer(player).getClientAimingProgress(MinecraftClient.getInstance().getTickDelta());
        }
        builder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        builder.vertex(centerX, centerY, -90.0D).color(255, 255, 255, 255).next();
        for (int i = 0; i <= 90; i++) {
            float angle = (float) i * ((float) Math.PI * 2F) / 90.0F;
            float sin = MathHelper.sin(angle);
            float cos = MathHelper.cos(angle);
            builder.vertex(centerX + cos * rad, centerY + sin * rad, -90.0D).color(255, 255, 255, 255).next();
        }
        BufferRenderer.drawWithGlobalProgram(builder.end());
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
        // 渲染目镜黑色遮罩
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        renderTempPart(matrixStack, transformType, renderType, light, overlay, ocularNodePath);
        // 渲染划分
        if (divisionNodePath != null) {
            RenderSystem.stencilFunc(GL11.GL_EQUAL, 2, 0xFF);
            renderTempPart(matrixStack, transformType, renderType, light, overlay, divisionNodePath);
        }
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
        RenderHelper.disableItemEntityStencilTest();
        // 渲染其他部分
        super.render(matrixStack, transformType, renderType, light, overlay);
    }
}