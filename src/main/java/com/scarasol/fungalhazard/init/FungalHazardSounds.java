package com.scarasol.fungalhazard.init;

import com.scarasol.fungalhazard.FungalHazardMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * @author Scarasol
 */
public class FungalHazardSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FungalHazardMod.MODID);

    public static final RegistryObject<SoundEvent> SPORER_IDLE = REGISTRY.register("sporer_idle", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FungalHazardMod.MODID, "sporer_idle")));
    public static final RegistryObject<SoundEvent> SPORER_DEATH = REGISTRY.register("sporer_death", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FungalHazardMod.MODID, "sporer_death")));
    public static final RegistryObject<SoundEvent> SPORER_DEATH_IN_CREEP = REGISTRY.register("sporer_death_in_creep", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FungalHazardMod.MODID, "sporer_death_in_creep")));

    public static final RegistryObject<SoundEvent> VOLATILE_IDLE = REGISTRY.register("volatile_idle", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FungalHazardMod.MODID, "volatile_idle")));
    public static final RegistryObject<SoundEvent> VOLATILE_HURT_LIGHT = REGISTRY.register("volatile_hurt_light", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FungalHazardMod.MODID, "volatile_hurt_light")));
    public static final RegistryObject<SoundEvent> VOLATILE_HURT_HEAVY = REGISTRY.register("volatile_hurt_heavy", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FungalHazardMod.MODID, "volatile_hurt_heavy")));
    public static final RegistryObject<SoundEvent> VOLATILE_JUMP_ATTACK = REGISTRY.register("volatile_jump_attack", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FungalHazardMod.MODID, "volatile_jump_attack")));
    public static final RegistryObject<SoundEvent> VOLATILE_EXECUTION = REGISTRY.register("volatile_execution", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FungalHazardMod.MODID, "volatile_execution")));
    public static final RegistryObject<SoundEvent> VOLATILE_EXECUTION_FAILURE = REGISTRY.register("volatile_execution_failure", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FungalHazardMod.MODID, "volatile_execution_failure")));
    public static final RegistryObject<SoundEvent> VOLATILE_RIDING = REGISTRY.register("volatile_riding", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FungalHazardMod.MODID, "volatile_riding")));
    public static final RegistryObject<SoundEvent> VOLATILE_SPRINT = REGISTRY.register("volatile_sprint", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FungalHazardMod.MODID, "volatile_sprint")));
    public static final RegistryObject<SoundEvent> VOLATILE_ATTACK = REGISTRY.register("volatile_attack", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FungalHazardMod.MODID, "volatile_attack")));
    public static final RegistryObject<SoundEvent> VOLATILE_DEATH = REGISTRY.register("volatile_death", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FungalHazardMod.MODID, "volatile_death")));
}
