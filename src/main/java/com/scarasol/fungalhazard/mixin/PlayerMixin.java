package com.scarasol.fungalhazard.mixin;

import com.scarasol.fungalhazard.api.IPatrolLeader;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Scarasol
 */
@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements IPatrolLeader {
    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean canBeLeader() {
        return hasEffect(MobEffects.BAD_OMEN);
    }
}
