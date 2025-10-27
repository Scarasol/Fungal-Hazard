package com.scarasol.fungalhazard.event;

import com.scarasol.fungalhazard.FungalHazardMod;
import com.scarasol.fungalhazard.api.IFungalContainer;
import com.scarasol.fungalhazard.configuration.CommonConfig;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieStates;
import com.scarasol.fungalhazard.entity.arachnid.LurkerEntity;
import com.scarasol.fungalhazard.entity.humanoid.AbstractHumanoidFungalZombie;
import com.scarasol.fungalhazard.init.FungalHazardEntities;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
                        AbstractHumanoidFungalZombie infected = FungalHazardEntities.INFECTED.get().create(serverLevel);
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

    @SubscribeEvent
    public static void openChest(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            BlockPos blockPos = event.getPos();
            BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
            if (blockEntity instanceof IFungalContainer fungalContainer) {
//                if (fungalContainer.hasLurker()) {
                    BlockPos spawnPos = blockPos.relative(Direction.UP);
                    if (!serverLevel.getBlockState(spawnPos).getCollisionShape(serverLevel, spawnPos).isEmpty()) {
                        spawnPos = null;
                        for (Direction direction : Direction.values()) {
                            spawnPos = blockPos.relative(direction);
                            if (serverLevel.getBlockState(spawnPos).getCollisionShape(serverLevel, spawnPos).isEmpty()) {
                                break;
                            }
                        }
                    }
                    if (spawnPos != null) {
                        Player player = event.getEntity();
                        LurkerEntity lurkerEntity = FungalHazardEntities.LURKER.get().create(serverLevel);
                        if (lurkerEntity != null) {
                            lurkerEntity.setPos(Vec3.atBottomCenterOf(spawnPos));
                            lurkerEntity.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.NATURAL, null, null);
                            lurkerEntity.setTarget(event.getEntity());
                            lurkerEntity.lookAt(EntityAnchorArgument.Anchor.EYES, player.position());
                            serverLevel.addFreshEntity(lurkerEntity);
                            serverLevel.playSound(null, blockPos, SoundEvents.SPIDER_HURT, SoundSource.HOSTILE, 1, 1);
                            fungalContainer.setHasLurker(false);
                            blockEntity.setChanged();
                            if (lurkerEntity.canJump()) {
                                lurkerEntity.setState(FungalZombieStates.JUMP);
                                lurkerEntity.setAnimationTick(10);
                            }
                        }
                    }
//                }
            }
        }
    }

}
