package com.eddie.jugglecraft.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

public final class EnergyTransferHelper {
    private EnergyTransferHelper() {
    }

    public static int pushEnergyToNeighbors(Level level, BlockPos origin, IEnergyStorage source, int maxTransfer) {
        if (level == null || maxTransfer <= 0) {
            return 0;
        }

        int remaining = Math.min(maxTransfer, source.extractEnergy(maxTransfer, true));
        int transferred = 0;

        for (Direction direction : Direction.values()) {
            if (remaining <= 0) {
                break;
            }

            BlockEntity neighbor = level.getBlockEntity(origin.relative(direction));
            if (neighbor == null) {
                continue;
            }

            IEnergyStorage target = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).resolve().orElse(null);
            if (target == null || !target.canReceive()) {
                continue;
            }

            int accepted = target.receiveEnergy(remaining, false);
            if (accepted <= 0) {
                continue;
            }

            source.extractEnergy(accepted, false);
            remaining -= accepted;
            transferred += accepted;
        }

        return transferred;
    }
}