package com.scarasol.fungalhazard.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;

/**
 * @author Scarasol
 */
public class FungalHazardTags {
    public static TagKey<Item> GUN = TagKey.create(Registries.ITEM, new ResourceLocation("forge:gun"));
    public static TagKey<Item> MELEE_WEAPON = TagKey.create(Registries.ITEM, new ResourceLocation("forge:melee_weapon"));


    public static TagKey<Biome> INFECTED_BLACKLIST = TagKey.create(Registries.BIOME, new ResourceLocation("forge:infected_blacklist"));
}
