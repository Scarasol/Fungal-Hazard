package com.scarasol.fungalhazard.network;

import com.scarasol.fungalhazard.entity.VolatileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record VolatileGuardPacket(int id) {


    public static void encode(VolatileGuardPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.id);
    }

    public static VolatileGuardPacket decode(FriendlyByteBuf buf){
        return new VolatileGuardPacket(buf.readInt());
    }

    public static void handler(VolatileGuardPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (!context.getDirection().getReceptionSide().isServer()) {
                Entity entity = Minecraft.getInstance().level.getEntity(message.id);
                if (entity instanceof VolatileEntity volatileEntity) {
                    volatileEntity.setSuccessGuard(true);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
