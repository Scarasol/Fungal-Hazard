package com.scarasol.fungalhazard;

import com.mojang.logging.LogUtils;
import com.scarasol.fungalhazard.configuration.CommonConfig;
import com.scarasol.fungalhazard.init.FungalHazardEntities;
import com.scarasol.fungalhazard.init.FungalHazardItems;
import com.scarasol.fungalhazard.init.FungalHazardParticleTypes;
import com.scarasol.fungalhazard.init.FungalHazardSounds;
import com.scarasol.fungalhazard.network.NetworkHandler;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * @author Scarasol
 */
@Mod(FungalHazardMod.MODID)
public class FungalHazardMod
{
    public static final String MODID = "fungal_hazard";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FungalHazardMod()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "fungalhazard-common.toml");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::addItemsToTabs);
        FungalHazardEntities.REGISTRY.register(modEventBus);
        FungalHazardParticleTypes.REGISTRY.register(modEventBus);
        FungalHazardItems.REGISTRY.register(modEventBus);
        FungalHazardSounds.REGISTRY.register(modEventBus);

        NetworkHandler.addNetworkMessage();
    }

    public void addItemsToTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(FungalHazardItems.INFECTED_SPAWN_EGG);
            event.accept(FungalHazardItems.SPORER_SPAWN_EGG);
            event.accept(FungalHazardItems.VOLATILE_SPAWN_EGG);
        }
    }

}
