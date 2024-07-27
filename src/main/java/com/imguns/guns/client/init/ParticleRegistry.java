package com.imguns.guns.client.init;

import com.imguns.guns.client.particle.BulletHoleParticle;
import com.imguns.guns.init.ModParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public class ParticleRegistry {
    public static void registerParticleFactory() {
        ParticleFactoryRegistry.getInstance().register(ModParticles.BULLET_HOLE, new BulletHoleParticle.Provider());
    }
}
