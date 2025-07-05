package com.scarasol.fungalhazard.network;

import com.scarasol.fungalhazard.FungalHazardMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * @author Scarasol
 */
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(FungalHazardMod.MODID, FungalHazardMod.MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int messageID = 0;

    public static <T> void addNetworkMessage() {
        PACKET_HANDLER.registerMessage(messageID++, VolatileGuardPacket.class, VolatileGuardPacket::encode, VolatileGuardPacket::decode, VolatileGuardPacket::handler);

    }
}
