package com.eddie.jugglepower.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;

public final class PowerNetworkHelper {
    @FunctionalInterface
    interface NeighborLookup {
        BlockEntity getBlockEntity(BlockPos pos);
    }

    private PowerNetworkHelper() {
    }

    public static int pushToTierMatchedNeighbors(Level level, BlockPos origin, TieredPowerNode sourceNode, int maxTransfer) {
        if (level == null) {
            return 0;
        }

        return pushToTierMatchedNeighbors(level::getBlockEntity, origin, sourceNode, maxTransfer);
    }

    static int pushToTierMatchedNeighbors(NeighborLookup lookup, BlockPos origin, TieredPowerNode sourceNode, int maxTransfer) {
        if (lookup == null || maxTransfer <= 0 || sourceNode.isTierFaulted()) {
            return 0;
        }

        IEnergyStorage sourceStorage = sourceNode.getTieredEnergyStorage();
        if (!sourceStorage.canExtract()) {
            return 0;
        }

        int remaining = Math.min(maxTransfer, sourceStorage.extractEnergy(maxTransfer, true));
        if (remaining <= 0) {
            return 0;
        }

        int transferred = 0;
        List<TransferTarget> targets = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = lookup.getBlockEntity(origin.relative(direction));
            if (neighbor == null) {
                continue;
            }

            if (neighbor instanceof TieredPowerNode targetNode) {
                if (!sourceNode.getPowerTier().isCompatibleWith(targetNode.getPowerTier())) {
                    sourceNode.markTierFault(targetNode.getPowerTier());
                    continue;
                }

                IEnergyStorage targetStorage = targetNode.getTieredEnergyStorage();
                if (!targetStorage.canReceive()) {
                    continue;
                }

                int demand = targetStorage.receiveEnergy(remaining, true);
                if (demand <= 0) {
                    continue;
                }

                targets.add(new TransferTarget(targetStorage, demand));
                continue;
            }

            IEnergyStorage foreignStorage = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).resolve().orElse(null);
            if (foreignStorage != null) {
                sourceNode.markTierFault(null);
            }
        }

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

    private static final class TransferTarget {
        private final IEnergyStorage storage;
        private int remainingDemand;

        private TransferTarget(IEnergyStorage storage, int remainingDemand) {
            this.storage = storage;
            this.remainingDemand = remainingDemand;
        }
    }
}
