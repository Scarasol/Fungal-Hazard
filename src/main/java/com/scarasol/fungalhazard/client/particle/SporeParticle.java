package com.scarasol.fungalhazard.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * @author Scarasol
 */
public class SporeParticle extends BasicParticle {

    protected SporeParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, boolean hasPhysics) {
        super(level, x, y, z, vx, vy, vz, hasPhysics);
        willFloat = true;
    }

    @Override
    public void particleEffect() {

    }

    public record Factory(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SporeParticle particle = new SporeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, true);
            particle.pickSprite(this.sprite);

            particle.setColor(0.48F, 0.25F, 0.12F);
            return particle;
        }
    }
}
