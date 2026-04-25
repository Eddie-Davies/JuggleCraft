package com.eddie.jugglepower.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

public final class PowerNetworkHelper {
    private PowerNetworkHelper() {
    }

    public static int pushToTierMatchedNeighbors(Level level, BlockPos origin, TieredPowerNode sourceNode, int maxTransfer) {
        if (level == null || maxTransfer <= 0 || sourceNode.isTierFaulted()) {
            return 0;
        }

        IEnergyStorage sourceStorage = sourceNode.getTieredEnergyStorage();
        if (!sourceStorage.canExtract()) {
            return 0;
        }

        int remaining = Math.min(maxTransfer, sourceStorage.extractEnergy(maxTransfer, true));
        int transferred = 0;

        for (Direction direction : Direction.values()) {
            if (remaining <= 0) {
                break;
            }

            BlockEntity neighbor = level.getBlockEntity(origin.relative(direction));
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

                int accepted = targetStorage.receiveEnergy(remaining, false);
                if (accepted <= 0) {
                    continue;
                }

                sourceStorage.extractEnergy(accepted, false);
                remaining -= accepted;
                transferred += accepted;
                continue;
            }

            IEnergyStorage foreignStorage = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).resolve().orElse(null);
            if (foreignStorage != null) {
                sourceNode.markTierFault(null);
            }
        }

        return transferred;
    }
}
