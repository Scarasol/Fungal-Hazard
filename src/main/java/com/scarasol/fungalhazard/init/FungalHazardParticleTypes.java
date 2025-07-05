package com.scarasol.fungalhazard.init;

import com.scarasol.fungalhazard.FungalHazardMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * @author Scarasol
 */
public class FungalHazardParticleTypes {

    public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(Registries.PARTICLE_TYPE, FungalHazardMod.MODID);

    public static final RegistryObject<ParticleType<SimpleParticleType>> SPORE = REGISTRY.register("fungal_spore", () -> new SimpleParticleType(false));
}
