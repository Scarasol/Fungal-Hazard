package com.scarasol.fungalhazard.mixin;

import com.scarasol.fungalhazard.api.IFungalZombie;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Scarasol
 */
@Mixin(Spider.class)
public abstract class SpiderMixin extends Monster {
    protected SpiderMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "finalizeSpawn", cancellable = true, at = @At("HEAD"))
    private void fungalHazard$finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, SpawnGroupData spawnGroupData, CompoundTag compoundTag, CallbackInfoReturnable<SpawnGroupData> cir) {
        if (this instanceof IFungalZombie) {
//            RandomSource randomsource = serverLevelAccessor.getRandom();
            if (spawnGroupData == null) {
                spawnGroupData = new Spider.SpiderEffectsGroupData();
//                if (serverLevelAccessor.getDifficulty() == Difficulty.HARD && randomsource.nextFloat() < 0.1F * difficultyInstance.getSpecialMultiplier()) {
//                    ((Spider.SpiderEffectsGroupData)spawnGroupData).setRandomEffect(randomsource);
//                }
            }
//
//            if (spawnGroupData instanceof Spider.SpiderEffectsGroupData spider$spidereffectsgroupdata) {
//                MobEffect mobeffect = spider$spidereffectsgroupdata.effect;
//                if (mobeffect != null) {
//                    this.addEffect(new MobEffectInstance(mobeffect, -1));
//                }
//            }
            cir.setReturnValue(super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag));
        }
    }
}
