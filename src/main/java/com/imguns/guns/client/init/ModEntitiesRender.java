package com.imguns.guns.client.init;

import com.imguns.guns.block.entity.GunSmithTableBlockEntity;
import com.imguns.guns.client.renderer.block.GunSmithTableRenderer;
import com.imguns.guns.client.renderer.entity.EntityBulletRenderer;
import com.imguns.guns.entity.EntityKineticBullet;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class ModEntitiesRender {

    public static void entityRenderers() {
        EntityRendererRegistry.register(EntityKineticBullet.TYPE, EntityBulletRenderer::new);
        BlockEntityRendererFactories.register(GunSmithTableBlockEntity.TYPE, GunSmithTableRenderer::new);
    }
}
