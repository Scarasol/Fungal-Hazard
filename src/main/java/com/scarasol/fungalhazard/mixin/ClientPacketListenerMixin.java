package com.scarasol.fungalhazard.mixin;

import com.scarasol.fungalhazard.api.IFungalZombie;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Scarasol
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Shadow private ClientLevel level;

    @Inject(method = "handleSetEntityPassengersPacket", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"))
    private void fungalHazard$HandleSetEntityPassengersPacket(ClientboundSetPassengersPacket packet, CallbackInfo ci) {
        Entity entity = this.level.getEntity(packet.getVehicle());
        if (entity instanceof IFungalZombie) {
            ci.cancel();
        }
    }
}
