package com.scarasol.fungalhazard.event;

import com.scarasol.fungalhazard.configuration.CommonConfig;
import com.scarasol.fungalhazard.entity.AbstractFungalZombie;
import com.scarasol.fungalhazard.init.FungalHazardEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber
public class EventHandler {

//    @SubscribeEvent
//    public static void changeLevel(EntityJoinLevelEvent event) {
//
//        if (event.getEntity() instanceof Enemy && !(event.getEntity() instanceof AbstractIllager) && !(event.getEntity() instanceof AbstractFungalZombie || event.getEntity() instanceof Player || event.getEntity() instanceof AbstractVillager) && event.getEntity() instanceof LivingEntity) {
//            event.setCanceled(true);
//        }
//    }

    @SubscribeEvent
    public static void zombieReplace(MobSpawnEvent.FinalizeSpawn event) {
        if (CommonConfig.ZOMBIE_REPLACE.get()) {
            MobSpawnType mobSpawnType = event.getSpawnType();
            if (mobSpawnType == MobSpawnType.NATURAL || mobSpawnType == MobSpawnType.JOCKEY) {
                if (event.getLevel() instanceof ServerLevel serverLevel) {
                    EntityType<?> entityType = event.getEntity().getType();
                    if (entityType.equals(EntityType.ZOMBIE)) {
                        event.setSpawnCancelled(true);
                        AbstractFungalZombie infected = FungalHazardEntities.INFECTED.get().create(serverLevel);
                        if (infected != null) {
                            BlockPos blockPos = event.getEntity().blockPosition();
                            infected.setPos(blockPos.getCenter());
                            infected.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockPos), MobSpawnType.NATURAL, null, null);
                            serverLevel.addFreshEntity(infected);
                        }

                    }
                }
            }
        }

    }

    @SubscribeEvent
    public static void zombieAid(ZombieEvent.SummonAidEvent event) {
        if (CommonConfig.ZOMBIE_REPLACE.get()) {
            Level level = event.getLevel();
            if (level.random.nextDouble() < event.getSummonChance()) {
                event.setCustomSummonedAid(FungalHazardEntities.INFECTED.get().create(level));
                event.setResult(Event.Result.ALLOW);
            }else {
                event.setResult(Event.Result.DENY);
            }
        }
    }

}
