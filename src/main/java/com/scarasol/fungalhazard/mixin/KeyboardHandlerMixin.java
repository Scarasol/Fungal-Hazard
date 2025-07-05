package com.scarasol.fungalhazard.mixin;

import com.scarasol.fungalhazard.entity.AbstractFungalZombie;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", cancellable = true, at = @At("HEAD"))
    private void fungalHazard$KeyPress(long windowPointer, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (modifiers == GLFW.GLFW_KEY_F3 || (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F9) || (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9)) {
            return;
        }
        if (action != 0 && key != GLFW.GLFW_KEY_ESCAPE && player != null && player.getVehicle() instanceof AbstractFungalZombie && minecraft.screen == null && !player.isCreative() && !player.isSpectator()) {
            ci.cancel();
        }
    }
}
