package com.scarasol.fungalhazard.entity.humanoid;

import com.scarasol.fungalhazard.api.IFungalZombie;
import com.scarasol.fungalhazard.configuration.CommonConfig;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieState;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieStates;
import com.scarasol.fungalhazard.entity.ai.fsm.StateHandler;
import com.scarasol.fungalhazard.init.FungalHazardEntities;
import com.scarasol.fungalhazard.init.FungalHazardParticleTypes;
import com.scarasol.fungalhazard.init.FungalHazardSounds;
import com.scarasol.sona.init.SonaMobEffects;
import com.scarasol.sona.manager.InfectionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PlayMessages;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

/**
 * @author Scarasol
 */
public class SporerEntity extends AbstractMutilatableZombie {

    private int timeTicker;
    private boolean disableAbility;

    public SporerEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public SporerEntity(PlayMessages.SpawnEntity packet, Level world) {
        this(FungalHazardEntities.SPORER.get(), world);
    }

    @Override
    public void registerStateRunner() {
        super.registerStateRunner();
        putStateRunner(FungalZombieStates.RUN, new StateHandler(StateHandler.EMPTY_RUNNER, this::defaultState,StateHandler.EMPTY_RUNNER));
        putStateRunner(FungalZombieStates.IDLE, new StateHandler(StateHandler.EMPTY_RUNNER, this::defaultState,StateHandler.EMPTY_RUNNER));
    }

    @Override
    public void defaultState(FungalZombieState state) {
        if (!level().isClientSide()) {
            setState(defaultStates());
        }
    }

    @Override
    public int fallAnimation() {
        triggerAnim("creepAnimationController", "fall");
        return 30;
    }

    @Override
    public int startCreep() {
        triggerAnim("creepAnimationController", "fall_to_creep");
        return 30;
    }

    @Override
    public boolean isMutilation() {
        FungalZombieState state = getState();
        return !state.equals(FungalZombieStates.RUN) && !state.equals(FungalZombieStates.IDLE);
    }

    public boolean canUseAbility() {
        return !isOnFire() && !hasEffect(SonaMobEffects.SLIMINESS.get()) && !level().isRainingAt(blockPosition()) && !isUnderWater() && !isInPowderSnow;
    }

    @Override
    protected void tickDeath() {
        super.tickDeath();
        deathTime++;
        if (!canUseAbility()) {
            disableAbility = true;
        }
        if (!disableAbility && deathTime > 20 && deathTime % 10 == 0 && level() instanceof ServerLevel serverLevel) {
            level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(CommonConfig.SPORER_RANGE_DEATH.get()), mob -> mob.getMobType() == MobType.UNDEAD && !mob.isUnderWater())
                    .forEach(mob -> heal((float) (CommonConfig.SPORER_HEAL_AMOUNT_DEATH.get() / 2)));
            if (com.scarasol.sona.configuration.CommonConfig.INFECTION_OPEN.get()) {
                serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(CommonConfig.SPORER_RANGE_DEATH.get()), livingEntity -> InfectionManager.canBeInfected(livingEntity) && !livingEntity.hasEffect(SonaMobEffects.IMMUNITY.get()) && !livingEntity.isUnderWater())
                        .forEach(livingEntity -> livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.INFECTION.get(), 60, CommonConfig.SPORER_INFECTION_LEVEL_DEATH.get() - 1, true, false)));
            }
        }
        if (this.deathTime >= 20 + CommonConfig.SPORER_ABILITY_TIME_DEATH.get() * 20 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(Entity.RemovalReason.KILLED);
        }
        setPose(Pose.DYING);
    }

    @Override
    public double getAttackDamageModifier() {
        return CommonConfig.SPORER_ATTACK_DAMAGE.get() - CommonConfig.SPORER_ATTACK_DAMAGE.getDefault();
    }

    @Override
    public double getHealthModifier() {
        return CommonConfig.SPORER_HEALTH.get() - CommonConfig.SPORER_HEALTH.getDefault();
    }

    @Override
    public double getMovementModifier() {
        return CommonConfig.SPORER_MOVEMENT.get() - CommonConfig.SPORER_MOVEMENT.getDefault();
    }

    @Override
    public double getArmorModifier() {
        return CommonConfig.SPORER_ARMOR.get() - CommonConfig.SPORER_ARMOR.getDefault();
    }

    @Override
    public double getArmorToughnessModifier() {
        return CommonConfig.SPORER_ARMOR_TOUGHNESS.get() - CommonConfig.SPORER_ARMOR_TOUGHNESS.getDefault();
    }

    @Override
    public int getAttackCoolDown() {
        return 30;
    }

    @Override
    public int getAttackAnimeTick() {
        if (getTarget() instanceof Player) {
            return 15;
        }
        return 0;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return FungalHazardSounds.SPORER_IDLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return !isMutilation() ? FungalHazardSounds.SPORER_DEATH.get() : FungalHazardSounds.SPORER_DEATH_IN_CREEP.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return FungalHazardSounds.SPORER_HURT.get();
    }

    @Override
    public FungalZombieState defaultStates() {
        if (getTarget() != null) {
            return FungalZombieStates.RUN;
        } else {
            return FungalZombieStates.IDLE;
        }
    }

    @Override
    protected boolean isSunSensitive() {
        return CommonConfig.SPORER_BURN_IN_SUN.get() && super.isSunSensitive();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    public String getModel() {
        return "geo/sporer.geo.json";
    }

    @Override
    public String getTexture() {
        return "textures/entities/sporer.png";
    }

    @Override
    public String getAnimation() {
        return "animations/sporer.animation.json";
    }

    @Override
    public PlayState animationController(AnimationState<AbstractMutilatableZombie> event) {
        AnimationController<AbstractMutilatableZombie> controller = event.getController();
        sporeParticle(event);
        if (event.isCurrentAnimation(WALK)) {
            controller.setAnimationSpeed(2.5);
        } else {
            controller.setAnimationSpeed(1);
        }
        return super.animationController(event);
    }

    @Override
    public <T extends IFungalZombie> void animationSwitch(AnimationState<T> event, AnimationController<T> controller, RawAnimation idle, RawAnimation move) {
        if (event.isMoving() || !(event.getLimbSwingAmount() > -0.15F && event.getLimbSwingAmount() < 0.15F)) {
            if ((!controller.isPlayingTriggeredAnimation())) {
                if (event.isCurrentAnimation(idle) || controller.getCurrentRawAnimation() == null || event.getController().hasAnimationFinished()) {
                    event.setAndContinue(move);
                    this.timeTicker = (int) event.animationTick;
                }
            }
        } else {
            if (!controller.isPlayingTriggeredAnimation()) {
                if (event.isCurrentAnimation(move) || controller.getCurrentRawAnimation() == null || event.getController().hasAnimationFinished()) {
                    event.setAndContinue(idle);
                    this.timeTicker = (int) event.animationTick;
                }
            }
        }
    }

    @Override
    public PlayState deathAnimationController(AnimationState<AbstractMutilatableZombie> event) {
        if (!isAlive()) {
            if (isMutilation()) {
                if (!event.isCurrentAnimation(CREEP_DEATH)) {
                    event.setAndContinue(CREEP_DEATH);
                    this.timeTicker = (int) event.animationTick;
                }
            } else {
                if (!event.isCurrentAnimation(DEATH)) {
                    event.setAndContinue(DEATH);
                    this.timeTicker = (int) event.animationTick;
                }
            }
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }


    @Override
    public void aiStep() {
        if (level() instanceof ServerLevel serverLevel && isAlive() && canUseAbility()) {
            if (timeTicker == 0) {
                level().getEntitiesOfClass(Mob.class, getBoundingBox().inflate(CommonConfig.SPORER_RANGE.get()), mob -> mob.getMobType() == MobType.UNDEAD && !mob.isUnderWater())
                        .forEach(mob -> mob.heal((float) (CommonConfig.SPORER_HEAL_AMOUNT.get() / 2)));
                if (com.scarasol.sona.configuration.CommonConfig.INFECTION_OPEN.get()) {
                    serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(CommonConfig.SPORER_RANGE.get()), livingEntity -> InfectionManager.canBeInfected(livingEntity) && !livingEntity.hasEffect(SonaMobEffects.IMMUNITY.get()) && !livingEntity.isUnderWater())
                            .forEach(livingEntity -> livingEntity.addEffect(new MobEffectInstance(SonaMobEffects.INFECTION.get(), 20, CommonConfig.SPORER_INFECTION_LEVEL.get() - 1, true, false)));
                }
            }
            timeTicker = (timeTicker + 1) % 10;
        }
        super.aiStep();
    }

    public void sporeParticle(AnimationState<AbstractMutilatableZombie> event) {
        if (Minecraft.getInstance().screen == null) {
            if (level() instanceof ClientLevel clientLevel && canUseAbility()) {
                double difference = event.animationTick - this.timeTicker;
                if (isAlive()) {
                    if (!event.isCurrentAnimation(IDLE)) {
                        difference = difference * 2.5;
                    }
                    int time = (int) difference % 120;
                    if (time > 74 && time < 82) {
                        sporeParticle(clientLevel);
                    }
                } else if (!disableAbility) {
                    if (difference > 15 && difference < 25) {
                        sporeParticle(clientLevel);
                    } else if (difference > 25) {
                        RandomSource randomSource = clientLevel.getRandom();
                        clientLevel.addAlwaysVisibleParticle((SimpleParticleType) FungalHazardParticleTypes.SPORE.get(), getX() - 6 + randomSource.nextDouble() * 12, getY() + randomSource.nextDouble(), getZ() - 6 + randomSource.nextDouble() * 12, 0, 0, 0);
                    }
                }
            }
        }
    }

    public void sporeParticle(ClientLevel clientLevel) {
        RandomSource randomSource = clientLevel.random;
        for (int i = 0; i < 2; i++) {
            Vec3 vec3 = new Vec3(randomSource.nextGaussian(), randomSource.nextGaussian(), randomSource.nextGaussian()).normalize().scale(0.1);
            double y = 0.95;
            if (isMutilation()) {
                y = 0.35;
            }
            clientLevel.addAlwaysVisibleParticle((SimpleParticleType) FungalHazardParticleTypes.SPORE.get(), getX() - 0.5 + randomSource.nextDouble(), getY() + y, getZ() - 0.5 + randomSource.nextDouble(), vec3.x, vec3.y, vec3.z);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder.add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.MAX_HEALTH, 30)
                .add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.FOLLOW_RANGE, 35)
                .add(Attributes.ARMOR, 2)
                .add(Attributes.ARMOR_TOUGHNESS)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        return builder;
    }
}
