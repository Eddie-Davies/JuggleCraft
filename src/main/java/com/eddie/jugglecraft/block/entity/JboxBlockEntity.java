package com.eddie.jugglecraft.block.entity;

import com.eddie.jugglecraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

public class JboxBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {
    private static final int SCAN_INTERVAL_TICKS = 20;

    private boolean gridEnabled = true;
    private boolean prioritizeBatteryCharging = true;
    private int trackedEnergy;
    private int trackedCapacity;
    private int connectedBlocks;

    public JboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.JBOX.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (level.getGameTime() % SCAN_INTERVAL_TICKS != 0) {
            return;
        }

        int totalEnergy = 0;
        int totalCapacity = 0;
        int energyBlocks = 0;

        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(pos.relative(direction)) == null) {
                continue;
            }

            IEnergyStorage storage = level.getBlockEntity(pos.relative(direction))
                    .getCapability(ForgeCapabilities.ENERGY, direction.getOpposite())
                    .resolve()
                    .orElse(null);

            if (storage == null) {
                continue;
            }

            totalEnergy += storage.getEnergyStored();
            totalCapacity += storage.getMaxEnergyStored();
            energyBlocks++;
        }

        if (trackedEnergy != totalEnergy || trackedCapacity != totalCapacity || connectedBlocks != energyBlocks) {
            trackedEnergy = totalEnergy;
            trackedCapacity = totalCapacity;
            connectedBlocks = energyBlocks;
            setChanged();
        }
    }

    public String describeStatus() {
        return String.format("J-Box online=%s chargingPriority=%s connected=%d energy=%d/%d FE",
                gridEnabled, prioritizeBatteryCharging, connectedBlocks, trackedEnergy, trackedCapacity);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("gridEnabled", gridEnabled);
        tag.putBoolean("prioritizeBatteryCharging", prioritizeBatteryCharging);
        tag.putInt("trackedEnergy", trackedEnergy);
        tag.putInt("trackedCapacity", trackedCapacity);
        tag.putInt("connectedBlocks", connectedBlocks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        gridEnabled = tag.getBoolean("gridEnabled");
        prioritizeBatteryCharging = tag.getBoolean("prioritizeBatteryCharging");
        trackedEnergy = tag.getInt("trackedEnergy");
        trackedCapacity = tag.getInt("trackedCapacity");
        connectedBlocks = tag.getInt("connectedBlocks");
    }
}