package com.eddie.jugglecraft.logic;

import com.eddie.jugglecraft.block.entity.BasicBatteryBlockEntity;
import com.eddie.jugglecraft.block.entity.CoalGeneratorBlockEntity;
import com.eddie.jugglecraft.block.entity.JboxBlockEntity;
import com.eddie.jugglecraft.block.entity.SolarPanelBlockEntity;
import com.eddie.jugglecraft.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Predicate;

public final class Rs485NetworkHelper {
    private static final int MAX_NETWORK_NODES = 256;

    private Rs485NetworkHelper() {
    }

    public static @Nullable BlockEntity findConnectedControllableDeviceByIp(Level level, BlockPos origin, int ipAddress) {
        ResolvedNode<BlockEntity> resolvedNode = findBestReachableBlockEntity(level, origin,
                blockEntity -> isControllableDevice(blockEntity)
                        && blockEntity instanceof IpAddressableDevice ipAddressableDevice
                        && ipAddressableDevice.getIpAddress() == ipAddress,
                BlockEntity.class);
        return resolvedNode != null ? resolvedNode.blockEntity() : null;
    }

    public static @Nullable BlockEntity findConnectedControllableDeviceByIp(Level level, BlockPos origin, int ipAddress,
                                                                             JboxBlockEntity requester) {
        ResolvedNode<BlockEntity> resolvedNode = findBestReachableBlockEntity(level, origin,
                blockEntity -> isControllableDevice(blockEntity)
                        && blockEntity instanceof IpAddressableDevice ipAddressableDevice
                        && ipAddressableDevice.getIpAddress() == ipAddress,
                BlockEntity.class);
        if (resolvedNode == null || !isPreferredControllerForDevice(level, resolvedNode.blockEntity(), ipAddress, requester)) {
            return null;
        }

        return resolvedNode.blockEntity();
    }

    public static boolean isControllableDevice(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof BasicBatteryBlockEntity
                || blockEntity instanceof CoalGeneratorBlockEntity
                || blockEntity instanceof SolarPanelBlockEntity;
    }

    private static boolean isPreferredControllerForDevice(Level level, BlockEntity device, int ipAddress, JboxBlockEntity requester) {
        ResolvedNode<JboxBlockEntity> resolvedNode = findBestReachableBlockEntity(level, device.getBlockPos(),
                blockEntity -> blockEntity instanceof JboxBlockEntity jboxBlockEntity && jboxBlockEntity.hasRegisteredIp(ipAddress),
                JboxBlockEntity.class);
        return resolvedNode != null && resolvedNode.blockEntity().getBlockPos().equals(requester.getBlockPos());
    }

    private static <T extends BlockEntity> @Nullable ResolvedNode<T> findBestReachableBlockEntity(Level level, BlockPos origin,
                                                                                                   Predicate<BlockEntity> predicate,
                                                                                                   Class<T> blockEntityClass) {
        if (level == null) {
            return null;
        }

        Queue<SearchNode> frontier = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        ResolvedNode<T> bestMatch = null;
        BlockPos originPos = origin.immutable();
        frontier.add(new SearchNode(originPos, 0));
        visited.add(originPos);

        while (!frontier.isEmpty() && visited.size() <= MAX_NETWORK_NODES) {
            SearchNode currentNode = frontier.remove();

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = currentNode.position().relative(direction).immutable();
                if (visited.contains(neighborPos)) {
                    continue;
                }

                int nextDistance = currentNode.distance() + 1;
                if (level.getBlockState(neighborPos).is(ModBlocks.RS485_WIRE.get())) {
                    visited.add(neighborPos);
                    frontier.add(new SearchNode(neighborPos, nextDistance));
                    continue;
                }

                BlockEntity blockEntity = level.getBlockEntity(neighborPos);
                if (blockEntity == null || !predicate.test(blockEntity) || !blockEntityClass.isInstance(blockEntity)) {
                    continue;
                }

                visited.add(neighborPos);
                ResolvedNode<T> candidate = new ResolvedNode<>(blockEntityClass.cast(blockEntity), nextDistance);
                if (isBetterCandidate(candidate, bestMatch)) {
                    bestMatch = candidate;
                }
            }
        }

        return bestMatch;
    }

    private static boolean isBetterCandidate(ResolvedNode<?> candidate, @Nullable ResolvedNode<?> currentBest) {
        if (currentBest == null) {
            return true;
        }

        if (candidate.distance() != currentBest.distance()) {
            return candidate.distance() < currentBest.distance();
        }

        long candidatePlacement = getPlacementOrder(candidate.blockEntity());
        long currentPlacement = getPlacementOrder(currentBest.blockEntity());
        if (candidatePlacement != currentPlacement) {
            return candidatePlacement < currentPlacement;
        }

        return candidate.blockEntity().getBlockPos().asLong() < currentBest.blockEntity().getBlockPos().asLong();
    }

    private static long getPlacementOrder(BlockEntity blockEntity) {
        if (blockEntity instanceof PlacementOrderedNode placementOrderedNode && placementOrderedNode.getPlacementOrder() >= 0) {
            return placementOrderedNode.getPlacementOrder();
        }

        return Long.MAX_VALUE;
    }

    private record SearchNode(BlockPos position, int distance) {
    }

    private record ResolvedNode<T extends BlockEntity>(T blockEntity, int distance) {
    }
}