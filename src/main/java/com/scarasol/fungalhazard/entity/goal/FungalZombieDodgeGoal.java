package com.scarasol.fungalhazard.entity.goal;

import com.scarasol.fungalhazard.FungalHazardMod;
import com.scarasol.fungalhazard.api.IDodgeableZombie;
import com.scarasol.fungalhazard.entity.AbstractFungalZombie;
import com.scarasol.sona.util.SonaMath;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author Scarasol
 */
public class FungalZombieDodgeGoal<T extends AbstractFungalZombie & IDodgeableZombie> extends Goal {
    private final T zombie;


    public FungalZombieDodgeGoal(T zombie) {
        this.zombie = zombie;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        return this.zombie.canDodge();
    }

    @Override
    public void start() {
        this.zombie.dodge();
        this.zombie.getNavigation().stop();
    }
}
