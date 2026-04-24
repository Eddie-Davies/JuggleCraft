package com.eddie.jugglecraft.logic;

import com.eddie.jugglecraft.block.entity.BasicBatteryBlockEntity;
import com.eddie.jugglecraft.block.entity.CoalGeneratorBlockEntity;
import com.eddie.jugglecraft.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class Rs485NetworkHelper {
    private static final int MAX_NETWORK_NODES = 256;

    private Rs485NetworkHelper() {
    }

    public static List<BlockEntity> findConnectedControllableDevices(Level level, BlockPos origin, int maxDevices) {
        List<BlockEntity> devices = new ArrayList<>();
        if (level == null || maxDevices <= 0) {
            return devices;
        }

        Queue<BlockPos> frontier = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        BlockPos originPos = origin.immutable();
        frontier.add(originPos);
        visited.add(originPos);

        while (!frontier.isEmpty() && visited.size() <= MAX_NETWORK_NODES && devices.size() < maxDevices) {
            BlockPos currentPos = frontier.remove();

            for (Direction direction : Direction.values()) {
                if (devices.size() >= maxDevices) {
                    break;
                }

                BlockPos neighborPos = currentPos.relative(direction).immutable();
                if (visited.contains(neighborPos)) {
                    continue;
                }

                if (level.getBlockState(neighborPos).is(ModBlocks.RS485_WIRE.get())) {
                    visited.add(neighborPos);
                    frontier.add(neighborPos);
                    continue;
                }

                BlockEntity blockEntity = level.getBlockEntity(neighborPos);
                if (isControllableDevice(blockEntity)) {
                    visited.add(neighborPos);
                    devices.add(blockEntity);
                }
            }
        }

        return devices;
    }

    public static @Nullable BlockEntity findConnectedControllableDeviceByIp(Level level, BlockPos origin, int ipAddress) {
        for (BlockEntity blockEntity : findConnectedControllableDevices(level, origin, MAX_NETWORK_NODES)) {
            if (blockEntity instanceof IpAddressableDevice ipAddressableDevice && ipAddressableDevice.getIpAddress() == ipAddress) {
                return blockEntity;
            }
        }

        return null;
    }

    public static boolean isControllableDevice(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof BasicBatteryBlockEntity || blockEntity instanceof CoalGeneratorBlockEntity;
    }
}