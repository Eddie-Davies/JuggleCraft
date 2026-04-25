package com.eddie.jugglepower.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class PowerNetworkHelper {
    @FunctionalInterface
    interface NeighborLookup {
        BlockEntity getBlockEntity(BlockPos pos);
    }

    private PowerNetworkHelper() {
    }

    public static int pushToTierMatchedNeighbors(Level level, BlockPos origin, TieredPowerNode sourceNode, int maxTransfer) {
        return pushToTierMatchedNeighbors(level, origin, sourceNode, maxTransfer, (blockEntity, direction) -> true);
    }

    public static int pushToTierMatchedNeighbors(Level level,
                                                 BlockPos origin,
                                                 TieredPowerNode sourceNode,
                                                 int maxTransfer,
                                                 Predicate<BlockEntity> neighborFilter) {
        return pushToTierMatchedNeighbors(level, origin, sourceNode, maxTransfer,
                (blockEntity, direction) -> neighborFilter == null || neighborFilter.test(blockEntity));
    }

    public static int pushToTierMatchedNeighbors(Level level,
                                                 BlockPos origin,
                                                 TieredPowerNode sourceNode,
                                                 int maxTransfer,
                                                 BiPredicate<BlockEntity, Direction> neighborFilter) {
        if (level == null) {
            return 0;
        }

        return pushToTierMatchedNeighbors(level::getBlockEntity, origin, sourceNode, maxTransfer, neighborFilter);
    }

    static int pushToTierMatchedNeighbors(NeighborLookup lookup, BlockPos origin, TieredPowerNode sourceNode, int maxTransfer) {
        return pushToTierMatchedNeighbors(lookup, origin, sourceNode, maxTransfer, (blockEntity, direction) -> true);
    }

    static int pushToTierMatchedNeighbors(NeighborLookup lookup,
                                          BlockPos origin,
                                          TieredPowerNode sourceNode,
                                          int maxTransfer,
                                          Predicate<BlockEntity> neighborFilter) {
        return pushToTierMatchedNeighbors(lookup, origin, sourceNode, maxTransfer,
                (blockEntity, direction) -> neighborFilter == null || neighborFilter.test(blockEntity));
    }

    static int pushToTierMatchedNeighbors(NeighborLookup lookup,
                                          BlockPos origin,
                                          TieredPowerNode sourceNode,
                                          int maxTransfer,
                                          BiPredicate<BlockEntity, Direction> neighborFilter) {
        if (lookup == null || maxTransfer <= 0 || sourceNode.isTierFaulted()) {
            return 0;
        }

        BiPredicate<BlockEntity, Direction> filter = neighborFilter != null ? neighborFilter : (blockEntity, direction) -> true;

        IEnergyStorage sourceStorage = sourceNode.getTieredEnergyStorage();
        if (!sourceStorage.canExtract()) {
            return 0;
        }

        int simulatedExtract = sourceStorage.extractEnergy(maxTransfer, true);
        if (simulatedExtract <= 0) {
            return 0;
        }

        List<TransferTarget> targets = collectReachableTargets(lookup, origin, sourceNode, simulatedExtract, filter);
        if (targets.isEmpty()) {
            return 0;
        }

        int remaining = simulatedExtract;
        int transferred = 0;

        while (remaining > 0 && !targets.isEmpty()) {
            int share = Math.max(1, remaining / targets.size());
            boolean movedInPass = false;

            for (int i = 0; i < targets.size() && remaining > 0; ) {
                TransferTarget target = targets.get(i);
                int requested = Math.min(target.remainingDemand, share);

                if (requested <= 0) {
                    targets.remove(i);
                    continue;
                }

                int accepted = target.storage.receiveEnergy(requested, false);
                if (accepted > 0) {
                    applyTransferPath(target.path, accepted);
                    sourceStorage.extractEnergy(accepted, false);
                    remaining -= accepted;
                    transferred += accepted;
                    target.remainingDemand -= accepted;
                    movedInPass = true;
                } else {
                    target.remainingDemand = 0;
                }

                if (target.remainingDemand <= 0) {
                    targets.remove(i);
                } else {
                    i++;
                }
            }

            if (!movedInPass) {
                break;
            }
        }

        return transferred;
    }

    private static List<TransferTarget> collectReachableTargets(NeighborLookup lookup,
                                                                BlockPos origin,
                                                                TieredPowerNode sourceNode,
                                                                int maxTransfer,
                                                                BiPredicate<BlockEntity, Direction> filter) {
        Queue<RelayPathState> queue = new ArrayDeque<>();
        Set<BlockPos> visitedRelayNodes = new HashSet<>();
        Map<BlockPos, TransferTarget> targetsByPos = new LinkedHashMap<>();

        queue.add(new RelayPathState(origin, sourceNode, List.of(), true));
        visitedRelayNodes.add(origin);

        while (!queue.isEmpty()) {
            RelayPathState state = queue.poll();

            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = state.pos.relative(direction);
                if (neighborPos.equals(origin)) {
                    continue;
                }

                BlockEntity neighbor = lookup.getBlockEntity(neighborPos);
                if (neighbor == null) {
                    continue;
                }

                if (state.firstHop && !filter.test(neighbor, direction)) {
                    continue;
                }

                if (neighbor instanceof TieredPowerNode targetNode) {
                    if (!sourceNode.getPowerTier().isCompatibleWith(targetNode.getPowerTier())) {
                        sourceNode.markTierFault(targetNode.getPowerTier());
                        continue;
                    }

                    List<TransferEdge> path = appendPath(state.path, state.node, targetNode, direction);
                    boolean relayEligible = targetNode.canRelayPower() && !targetNode.isTierFaulted();
                    if (relayEligible && visitedRelayNodes.add(neighborPos)) {
                        queue.add(new RelayPathState(neighborPos, targetNode, path, false));
                    }

                    if (!relayEligible) {
                        IEnergyStorage targetStorage = targetNode.getTieredEnergyStorage();
                        if (targetStorage.canReceive()) {
                            int demand = targetStorage.receiveEnergy(maxTransfer, true);
                            if (demand > 0 && !targetsByPos.containsKey(neighborPos)) {
                                targetsByPos.put(neighborPos, new TransferTarget(targetNode, targetStorage, path, demand));
                            }
                        }
                    }

                    continue;
                }

                IEnergyStorage foreignStorage = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).resolve().orElse(null);
                if (foreignStorage != null) {
                    sourceNode.markTierFault(null);
                }
            }
        }

        return new ArrayList<>(targetsByPos.values());
    }

    private static List<TransferEdge> appendPath(List<TransferEdge> basePath,
                                                 TieredPowerNode source,
                                                 TieredPowerNode target,
                                                 Direction direction) {
        List<TransferEdge> path = new ArrayList<>(basePath.size() + 1);
        path.addAll(basePath);
        path.add(new TransferEdge(source, target, direction));
        return path;
    }

    private static void applyTransferPath(List<TransferEdge> path, int amount) {
        for (TransferEdge edge : path) {
            edge.source.onPowerSent(edge.direction, amount);
            edge.target.onPowerReceived(edge.direction.getOpposite(), amount);
        }
    }

    private record RelayPathState(BlockPos pos,
                                  TieredPowerNode node,
                                  List<TransferEdge> path,
                                  boolean firstHop) {
    }

    private record TransferEdge(TieredPowerNode source,
                                TieredPowerNode target,
                                Direction direction) {
    }

    private static final class TransferTarget {
        private final TieredPowerNode node;
        private final IEnergyStorage storage;
        private final List<TransferEdge> path;
        private int remainingDemand;

        private TransferTarget(TieredPowerNode node,
                               IEnergyStorage storage,
                               List<TransferEdge> path,
                               int remainingDemand) {
            this.node = node;
            this.storage = storage;
            this.path = path;
            this.remainingDemand = remainingDemand;
        }
    }
}
