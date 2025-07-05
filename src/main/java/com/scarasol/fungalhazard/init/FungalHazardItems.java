package com.scarasol.fungalhazard.init;

import com.scarasol.fungalhazard.FungalHazardMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * @author Scarasol
 */
public class FungalHazardItems {

    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, FungalHazardMod.MODID);

    public static final RegistryObject<Item> INFECTED_SPAWN_EGG = REGISTRY.register("infected_spawn_egg", () -> new ForgeSpawnEggItem(FungalHazardEntities.INFECTED, -16750951, -10066330, new Item.Properties()));
    public static final RegistryObject<Item> SPORER_SPAWN_EGG = REGISTRY.register("sporer_spawn_egg", () -> new ForgeSpawnEggItem(FungalHazardEntities.SPORER, -10079488, -39424, new Item.Properties()));
    public static final RegistryObject<Item> VOLATILE_SPAWN_EGG = REGISTRY.register("volatile_spawn_egg", () -> new ForgeSpawnEggItem(FungalHazardEntities.VOLATILE,9044739, 7999, new Item.Properties()));

}
