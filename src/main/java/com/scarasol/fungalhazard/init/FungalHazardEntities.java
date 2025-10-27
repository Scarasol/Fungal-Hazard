package com.scarasol.fungalhazard.init;

import com.scarasol.fungalhazard.FungalHazardMod;
import com.scarasol.fungalhazard.entity.arachnid.LurkerEntity;
import com.scarasol.fungalhazard.entity.humanoid.AbstractHumanoidFungalZombie;
import com.scarasol.fungalhazard.entity.humanoid.InfectedEntity;
import com.scarasol.fungalhazard.entity.humanoid.SporerEntity;
import com.scarasol.fungalhazard.entity.humanoid.VolatileEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.DungeonHooks;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FungalHazardEntities {


    public static final UUID SPAWN_MODIFIER = UUID.fromString("3606EAA0-0711-7B20-6FEA-1B6E1EFB535C");
    public static final EntityDimensions DEATH_DIMENSIONS = EntityDimensions.scalable(0.7f, 0.1f);

    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FungalHazardMod.MODID);

    public static final RegistryObject<EntityType<InfectedEntity>> INFECTED = register("infected",
            EntityType.Builder.<InfectedEntity>of(InfectedEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(InfectedEntity::new).sized(0.8f, 1.9f));

    public static final RegistryObject<EntityType<SporerEntity>> SPORER = register("sporer",
            EntityType.Builder.<SporerEntity>of(SporerEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(SporerEntity::new).sized(0.8f, 1.9f));

    public static final RegistryObject<EntityType<VolatileEntity>> VOLATILE = register("volatile",
            EntityType.Builder.<VolatileEntity>of(VolatileEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(VolatileEntity::new).sized(0.9f, 2.0f));

    public static final RegistryObject<EntityType<LurkerEntity>> LURKER = register("lurker",
            EntityType.Builder.<LurkerEntity>of(LurkerEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(LurkerEntity::new).sized(0.9f, 0.5f));

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryName, EntityType.Builder<T> entityTypeBuilder) {
        return REGISTRY.register(registryName, () -> entityTypeBuilder.build(registryName));
    }

    @SubscribeEvent
    public static void spawnRules(SpawnPlacementRegisterEvent event) {
        event.register(INFECTED.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractHumanoidFungalZombie::checkFungalZombieSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
        event.register(SPORER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractHumanoidFungalZombie::checkFungalZombieSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
        event.register(VOLATILE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractHumanoidFungalZombie::checkFungalZombieSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
        DungeonHooks.addDungeonMob(INFECTED.get(), 180);
        DungeonHooks.addDungeonMob(SPORER.get(), 80);
        DungeonHooks.addDungeonMob(VOLATILE.get(), 20);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(INFECTED.get(), InfectedEntity.createAttributes().build());
        event.put(SPORER.get(), SporerEntity.createAttributes().build());
        event.put(VOLATILE.get(), VolatileEntity.createAttributes().build());
        event.put(LURKER.get(), LurkerEntity.createAttributes().build());
    }

}
