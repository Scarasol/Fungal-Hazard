package com.scarasol.fungalhazard.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * @author Scarasol
 */
public class FungalHazardTags {
    public static TagKey<Item> GUN = TagKey.create(Registries.ITEM, new ResourceLocation("forge:gun"));
    public static TagKey<Item> MELEE_WEAPON = TagKey.create(Registries.ITEM, new ResourceLocation("forge:melee_weapon"));
}
