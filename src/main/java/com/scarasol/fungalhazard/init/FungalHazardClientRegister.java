package com.scarasol.fungalhazard.init;

import com.scarasol.fungalhazard.client.particle.SporeParticle;
import com.scarasol.fungalhazard.client.renderer.FungalZombieEntityRenderer;
import com.scarasol.fungalhazard.client.renderer.MutilatableZombieEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FungalHazardClientRegister {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FungalHazardEntities.INFECTED.get(), renderManager -> new MutilatableZombieEntityRenderer<>(renderManager, true));
        event.registerEntityRenderer(FungalHazardEntities.SPORER.get(), renderManager -> new MutilatableZombieEntityRenderer<>(renderManager, false));
        event.registerEntityRenderer(FungalHazardEntities.VOLATILE.get(), renderManager -> new FungalZombieEntityRenderer<>(renderManager, true));
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(FungalHazardParticleTypes.SPORE.get(), SporeParticle.Factory::new);
    }

}
