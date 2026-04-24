package com.eddie.jugglecraft.network;

import com.eddie.jugglecraft.JuggleCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModMessages {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(JuggleCraft.MODID, "messages"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextId;

    private ModMessages() {
    }

    public static void register() {
        CHANNEL.registerMessage(nextId++, JboxSetIpPacket.class,
                JboxSetIpPacket::encode,
                JboxSetIpPacket::decode,
                JboxSetIpPacket::handle);
    }
}