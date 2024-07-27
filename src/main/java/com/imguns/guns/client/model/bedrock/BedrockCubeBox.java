package com.imguns.guns.client.model.bedrock;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class BedrockCubeBox implements BedrockCube {
    public final float minX;
    public final float minY;
    public final float minZ;
    public final float maxX;
    public final float maxY;
    public final float maxZ;
    private final BedrockPolygon[] polygons;

    public BedrockCubeBox(float texOffX, float texOffY, float x, float y, float z, float width, float height, float depth, float delta, boolean mirror, float texWidth, float texHeight) {
        this.minX = x;
        this.minY = y;
        this.minZ = z;
        this.maxX = x + width;
        this.maxY = y + height;
        this.maxZ = z + depth;
        this.polygons = new BedrockPolygon[6];

        float xEnd = x + width;
        float yEnd = y + height;
        float zEnd = z + depth;
        x = x - delta;
        y = y - delta;
        z = z - delta;
        xEnd = xEnd + delta;
        yEnd = yEnd + delta;
        zEnd = zEnd + delta;

        if (mirror) {
            float tmp = xEnd;
            xEnd = x;
            x = tmp;
        }

        BedrockVertex vertex1 = new BedrockVertex(x, y, z, 0.0F, 0.0F);
        BedrockVertex vertex2 = new BedrockVertex(xEnd, y, z, 0.0F, 8.0F);
        BedrockVertex vertex3 = new BedrockVertex(xEnd, yEnd, z, 8.0F, 8.0F);
        BedrockVertex vertex4 = new BedrockVertex(x, yEnd, z, 8.0F, 0.0F);
        BedrockVertex vertex5 = new BedrockVertex(x, y, zEnd, 0.0F, 0.0F);
        BedrockVertex vertex6 = new BedrockVertex(xEnd, y, zEnd, 0.0F, 8.0F);
        BedrockVertex vertex7 = new BedrockVertex(xEnd, yEnd, zEnd, 8.0F, 8.0F);
        BedrockVertex vertex8 = new BedrockVertex(x, yEnd, zEnd, 8.0F, 0.0F);

        int dx = (int) width;
        int dy = (int) height;
        int dz = (int) depth;

        float p1 = texOffX + dz;
        float p2 = texOffX + dz + dx;
        float p3 = texOffX + dz + dx + dx;
        float p4 = texOffX + dz + dx + dz;
        float p5 = texOffX + dz + dx + dz + dx;
        float p6 = texOffY + dz;
        float p7 = texOffY + dz + dy;
        float p8 = texOffY;
        float p9 = texOffX;

        this.polygons[2] = new BedrockPolygon(new BedrockVertex[]{vertex6, vertex5, vertex1, vertex2}, p1, p8, p2, p6, texWidth, texHeight, mirror, Direction.DOWN);
        this.polygons[3] = new BedrockPolygon(new BedrockVertex[]{vertex3, vertex4, vertex8, vertex7}, p2, p6, p3, p8, texWidth, texHeight, mirror, Direction.UP);
        this.polygons[1] = new BedrockPolygon(new BedrockVertex[]{vertex1, vertex5, vertex8, vertex4}, p9, p6, p1, p7, texWidth, texHeight, mirror, Direction.WEST);
        this.polygons[4] = new BedrockPolygon(new BedrockVertex[]{vertex2, vertex1, vertex4, vertex3}, p1, p6, p2, p7, texWidth, texHeight, mirror, Direction.NORTH);
        this.polygons[0] = new BedrockPolygon(new BedrockVertex[]{vertex6, vertex2, vertex3, vertex7}, p2, p6, p4, p7, texWidth, texHeight, mirror, Direction.EAST);
        this.polygons[5] = new BedrockPolygon(new BedrockVertex[]{vertex5, vertex6, vertex7, vertex8}, p4, p6, p5, p7, texWidth, texHeight, mirror, Direction.SOUTH);
    }

    @Override
    public void compile(MatrixStack.Entry entry, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha) {
        Matrix4f matrix4f = entry.getPositionMatrix();
        Matrix3f matrix3f = entry.getNormalMatrix();

        for (BedrockPolygon polygon : this.polygons) {
            Vector3f vector3f = new Vector3f(polygon.normal);
            vector3f.mul(matrix3f);
            float nx = vector3f.x();
            float ny = vector3f.y();
            float nz = vector3f.z();

            for (BedrockVertex vertex : polygon.vertices) {
                float x = vertex.pos.x() / 16.0F;
                float y = vertex.pos.y() / 16.0F;
                float z = vertex.pos.z() / 16.0F;
                Vector4f vector4f = new Vector4f(x, y, z, 1.0F);
                vector4f.mul(matrix4f);
                consumer.vertex(vector4f.x(), vector4f.y(), vector4f.z(), red, green, blue, alpha, vertex.u, vertex.v, overlay, light, nx, ny, nz);
            }
        }
    }
}
