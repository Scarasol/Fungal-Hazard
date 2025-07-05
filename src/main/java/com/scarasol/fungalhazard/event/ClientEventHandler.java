package com.scarasol.fungalhazard.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.scarasol.fungalhazard.FungalHazardMod;
import com.scarasol.fungalhazard.entity.AbstractFungalZombie;
import com.scarasol.fungalhazard.entity.ai.fsm.FungalZombieStates;
import com.scarasol.sona.init.SonaMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * @author Scarasol
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void forbiddenMouse(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (event.getAction() != 0 && minecraft.screen == null && player != null && player.getVehicle() instanceof AbstractFungalZombie abstractFungalZombie) {
            if (!player.isCreative() && !player.isSpectator()) {
                event.setCanceled(!(player.getMainHandItem().getItem() instanceof TieredItem) || abstractFungalZombie.getState().equals(FungalZombieStates.EXECUTION));
            }
        }
    }

    @SubscribeEvent
    public static void forbiddenRender(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (minecraft.screen == null && player != null && player.getVehicle() instanceof AbstractFungalZombie abstractFungalZombie) {
            if (!player.isCreative() && !player.isSpectator()) {
                event.setCanceled(!(player.getMainHandItem().getItem() instanceof TieredItem) || abstractFungalZombie.getState().equals(FungalZombieStates.EXECUTION));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderInfectionOverlayPost(RenderGuiOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player != null) {
            if (player.getVehicle() instanceof AbstractFungalZombie) {
                if ("minecraft:mount_health".equals(event.getOverlay().id().toString())) {
                    event.setCanceled(true);
                }
            }
        }
    }

}
