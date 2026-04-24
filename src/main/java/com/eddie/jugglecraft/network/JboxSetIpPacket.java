package com.eddie.jugglecraft.network;

import com.eddie.jugglecraft.block.entity.JboxBlockEntity;
import com.eddie.jugglecraft.menu.JboxMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record JboxSetIpPacket(BlockPos blockPos, int slotIndex, int ipAddress) {
    public static void encode(JboxSetIpPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.blockPos);
        buffer.writeInt(packet.slotIndex);
        buffer.writeInt(packet.ipAddress);
    }

    public static JboxSetIpPacket decode(FriendlyByteBuf buffer) {
        return new JboxSetIpPacket(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
    }

    public static void handle(JboxSetIpPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) {
                return;
            }

            if (!(sender.containerMenu instanceof JboxMenu jboxMenu)) {
                return;
            }

            JboxBlockEntity blockEntity = jboxMenu.getBlockEntity();
            if (!blockEntity.getBlockPos().equals(packet.blockPos)) {
                return;
            }

            if (packet.slotIndex < 0) {
                blockEntity.setIpAddress(packet.ipAddress);
            } else {
                blockEntity.setSlotIp(packet.slotIndex, packet.ipAddress);
            }
        });
        context.setPacketHandled(true);
    }
}