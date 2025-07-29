package com.scarasol.fungalhazard.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * @author Scarasol
 */
public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> MUTILATION_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Double> MUTILATION_COEFFICIENT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ZOMBIE_REPLACE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> INFECTED_PATROLLING;

    public static final ForgeConfigSpec.ConfigValue<Double> INFECTED_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Double> INFECTED_HEALTH;
    public static final ForgeConfigSpec.ConfigValue<Double> INFECTED_MOVEMENT;
    public static final ForgeConfigSpec.ConfigValue<Double> INFECTED_ARMOR;
    public static final ForgeConfigSpec.ConfigValue<Double> INFECTED_ARMOR_TOUGHNESS;
    public static final ForgeConfigSpec.ConfigValue<Double> INFECTED_RUN_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<Boolean> INFECTED_BURN_IN_SUN;

    public static final ForgeConfigSpec.ConfigValue<Double> SPORER_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Double> SPORER_HEALTH;
    public static final ForgeConfigSpec.ConfigValue<Double> SPORER_MOVEMENT;
    public static final ForgeConfigSpec.ConfigValue<Double> SPORER_ARMOR;
    public static final ForgeConfigSpec.ConfigValue<Double> SPORER_ARMOR_TOUGHNESS;
    public static final ForgeConfigSpec.ConfigValue<Double> SPORER_RANGE;
    public static final ForgeConfigSpec.ConfigValue<Double> SPORER_HEAL_AMOUNT;
    public static final ForgeConfigSpec.ConfigValue<Integer> SPORER_INFECTION_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<Double> SPORER_RANGE_DEATH;
    public static final ForgeConfigSpec.ConfigValue<Double> SPORER_HEAL_AMOUNT_DEATH;
    public static final ForgeConfigSpec.ConfigValue<Integer> SPORER_INFECTION_LEVEL_DEATH;
    public static final ForgeConfigSpec.ConfigValue<Integer> SPORER_ABILITY_TIME_DEATH;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SPORER_BURN_IN_SUN;

    public static final ForgeConfigSpec.ConfigValue<Double> VOLATILE_ATTACK_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Double> VOLATILE_HEALTH;
    public static final ForgeConfigSpec.ConfigValue<Double> VOLATILE_MOVEMENT;
    public static final ForgeConfigSpec.ConfigValue<Double> VOLATILE_ARMOR;
    public static final ForgeConfigSpec.ConfigValue<Double> VOLATILE_ARMOR_TOUGHNESS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> VOLATILE_EQUIPMENT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> VOLATILE_BURN_IN_SUN;
    public static final ForgeConfigSpec.ConfigValue<Boolean> VOLATILE_FLEE_IN_SUN;
    public static final ForgeConfigSpec.ConfigValue<Double> VOLATILE_EXECUTION_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> VOLATILE_EXECUTION_TIME;
    public static final ForgeConfigSpec.ConfigValue<Double> VOLATILE_ESCAPE_DAMAGE;
    public static final ForgeConfigSpec.ConfigValue<Integer> VOLATILE_EXECUTION_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<Integer> VOLATILE_DODGE_COUNT;
    public static final ForgeConfigSpec.ConfigValue<Integer> VOLATILE_DODGE_COOLDOWN;
    public static final ForgeConfigSpec.ConfigValue<Double> VOLATILE_GUARD_MODIFIER;
    public static final ForgeConfigSpec.ConfigValue<Double> VOLATILE_GUARD_STUN_TIME;
    public static final ForgeConfigSpec.ConfigValue<Integer> VOLATILE_GUARD_COOLDOWN;

    static {
        BUILDER.push("Overall");
        MUTILATION_CHANCE = BUILDER.comment("Limb-loss chance on regular infected spawn.")
                .defineInRange("Limb-loss Chance", 0.2, 0, 1.0);
        MUTILATION_COEFFICIENT = BUILDER.comment("The coefficient of leg destruction probability, calculated as coefficient multiplied by indirect damage divided by maximum health.")
                .defineInRange("Leg Destruction Probability Coefficient", 2.5, 0.0, 50.0);
        ZOMBIE_REPLACE = BUILDER.comment("Whether vanilla zombies are replaced by the regular infected from this mod.")
                .define("Zombie Replace", true);
        INFECTED_PATROLLING = BUILDER.comment("Whether regular infected that are outdoors will spontaneously gather and begin migrating.")
                .define("Infected Migrating", true);
        BUILDER.pop();

        BUILDER.push("Infected");
        INFECTED_ATTACK_DAMAGE = BUILDER.comment("The attack damage attribute of Infected. Final value is multiplied by 1.5 in Hard mode.")
                .defineInRange("Infected Attack Damage", 3, 0, 2048D);
        INFECTED_HEALTH = BUILDER.comment("The health attribute of Infected.")
                .defineInRange("Infected Health", 20, 0, 1024D);
        INFECTED_MOVEMENT = BUILDER.comment("The movement attribute of Infected.")
                .defineInRange("Infected Movement", 0.23, 0, 1024D);
        INFECTED_ARMOR = BUILDER.comment("The armor attribute of Infected.")
                .defineInRange("Infected Armor", 2, 0, 30D);
        INFECTED_ARMOR_TOUGHNESS = BUILDER.comment("The armor toughness attribute of Infected.")
                .defineInRange("Infected Armor Toughness", 0, 0, 20D);

        INFECTED_RUN_CHANCE = BUILDER.comment("Chance for an Infected to be able to run.")
                .defineInRange("Infected Run Chance", 0.3, 0, 1.0);
        INFECTED_BURN_IN_SUN = BUILDER.comment("Determines whether Infected will burn when exposed to sunlight.")
                .define("Infected Burn in Sun", true);
        BUILDER.pop();

        BUILDER.push("Sporer");
        SPORER_ATTACK_DAMAGE = BUILDER.comment("The attack damage attribute of Sporer. Final value is multiplied by 1.5 in Hard mode.")
                .defineInRange("Sporer Attack Damage", 2, 0, 2048D);
        SPORER_HEALTH = BUILDER.comment("The health attribute of Sporer.")
                .defineInRange("Sporer Health", 30, 0, 1024D);
        SPORER_MOVEMENT = BUILDER.comment("The movement attribute of Sporer.")
                .defineInRange("Sporer Movement", 0.23, 0, 1024D);
        SPORER_ARMOR = BUILDER.comment("The armor attribute of Sporer.")
                .defineInRange("Sporer Armor", 2, 0, 30D);
        SPORER_ARMOR_TOUGHNESS = BUILDER.comment("The armor toughness attribute of Sporer.")
                .defineInRange("Sporer Armor Toughness", 0, 0, 20D);

        SPORER_RANGE = BUILDER.comment("The effective range of the Sporer's ability.")
                .defineInRange("Sporer Ability Range", 6, 0.0, 50.0);

        SPORER_HEAL_AMOUNT = BUILDER.comment("Sporer healing per second to Undead within ability range.")
                .defineInRange("Sporer Heal Amount", 1.5, 0.0, 5000.0);

        SPORER_INFECTION_LEVEL = BUILDER.comment("The infection level applied by the Sporer to players and susceptible entities within its ability range. Each level applies 0.5 infection per second (requires Sona's infection system to be enabled).")
                .defineInRange("Sporer Infection Level", 1, 1, 100);

        SPORER_RANGE_DEATH = BUILDER.comment("The effective range of the Sporer's ability in death.")
                .defineInRange("Sporer Ability Range in Death", 6, 0.0, 50.0);

        SPORER_HEAL_AMOUNT_DEATH = BUILDER.comment("Sporer healing per second to Undead within ability range in death.")
                .defineInRange("Sporer Death Heal Amount in Death", 3, 0.0, 5000.0);

        SPORER_INFECTION_LEVEL_DEATH = BUILDER.comment("The infection level applied by the Sporer to players and susceptible entities within its ability range in death. Each level applies 0.5 infection per second (requires Sona's infection system to be enabled).")
                .defineInRange("Sporer Death Infection Level in Death", 4, 1, 100);

        SPORER_ABILITY_TIME_DEATH = BUILDER.comment("Duration in seconds of the Sporer's ability in death.")
                .defineInRange("Sporer Ability Time in Death", 5, 3, 5000);

        SPORER_BURN_IN_SUN = BUILDER.comment("Determines whether Sporer will burn when exposed to sunlight.")
                .define("Sporer Burn in Sun", true);
        BUILDER.pop();

        BUILDER.push("Volatile");
        VOLATILE_ATTACK_DAMAGE = BUILDER.comment("The attack damage attribute of Volatile. Final value is multiplied by 1.5 in Hard mode.")
                .defineInRange("Volatile Attack Damage", 13, 0, 2048D);
        VOLATILE_HEALTH = BUILDER.comment("The health attribute of Volatile.")
                .defineInRange("Volatile Health", 85, 0, 1024D);
        VOLATILE_MOVEMENT = BUILDER.comment("The movement attribute of Volatile.")
                .defineInRange("Volatile Movement", 0.45, 0, 1024D);
        VOLATILE_ARMOR = BUILDER.comment("The armor attribute of Volatile.")
                .defineInRange("Volatile Armor", 15, 0, 30D);
        VOLATILE_ARMOR_TOUGHNESS = BUILDER.comment("The armor toughness attribute of Volatile.")
                .defineInRange("Volatile Armor Toughness", 8, 0, 20D);


        VOLATILE_EQUIPMENT = BUILDER.comment("Determines whether Volatile can have equipment.")
                .define("Volatile Equipment", true);

        VOLATILE_BURN_IN_SUN = BUILDER.comment("Determines whether Volatile will burn when exposed to sunlight.")
                .define("Volatile Burn in Sun", true);

        VOLATILE_FLEE_IN_SUN = BUILDER.comment("Determines whether Volatile will flee when exposed to sunlight.")
                .define("Volatile Flee in Sun", true);

        BUILDER.push("Execution");
        VOLATILE_EXECUTION_DAMAGE = BUILDER.comment("Damage multiplier of Volatile execution.")
                .defineInRange("Volatile Execution Damage Multiplier", 2, 1.0, 5000.0);
        VOLATILE_EXECUTION_TIME = BUILDER.comment("How many seconds are allowed to escape when grabbed by Volatile.")
                .defineInRange("Volatile Escape Time", 3, 0, 100);
        VOLATILE_ESCAPE_DAMAGE = BUILDER.comment("The amount of damage a target must deal to escape Volatile's grasp, expressed as a percentage of Volatile's maximum health.")
                .defineInRange("Volatile Escape Damage", 0.3, 0, 1.0);
        VOLATILE_EXECUTION_COOLDOWN = BUILDER.comment("Cooldown time in seconds for Volatile's execution.")
                .defineInRange("Volatile Execution Cooldown", 15, 0, 5000);
        BUILDER.pop();

        BUILDER.push("Dodge");
        VOLATILE_DODGE_COUNT = BUILDER.comment("The number of times Volatile can dodge consecutively per dodge activation.")
                .defineInRange("Volatile Dodge Count", 3, 1, 5000);
        VOLATILE_DODGE_COOLDOWN = BUILDER.comment("Time in seconds required to recover one dodge charge after Volatile has exhausted all available dodges.")
                .defineInRange("Volatile Dodge Time", 7, 0, 5000);
        BUILDER.pop();

        BUILDER.push("Blocking");
        VOLATILE_GUARD_MODIFIER = BUILDER.comment("Fraction of original damage taken from the front while Volatile is blocking.")
                .defineInRange("Volatile Blocking Modifier", 0.2, 0.0, 1.0);
        VOLATILE_GUARD_STUN_TIME = BUILDER.comment("Duration in seconds of the stun effect applied to players hit by Volatile's parry counter.")
                .defineInRange("Volatile Counter Stun Time", 1.5, 0, 50.0);
        VOLATILE_GUARD_COOLDOWN = BUILDER.comment("Cooldown time in seconds for Volatile's blocking.")
                .defineInRange("Volatile Blocking Cooldown", 5, 0, 5000);
        BUILDER.pop();
        BUILDER.pop();


        SPEC = BUILDER.build();
    }
}
