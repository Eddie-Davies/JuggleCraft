package com.eddie.jugglecraft.block.entity;

import com.eddie.jugglecraft.block.custom.FeToRedstoneTransformerBlock;
import com.eddie.jugglecraft.registry.ModBlockEntities;
import com.eddie.jugglecraft.util.MountedDeviceHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.IEnergyStorage;

public class FeToRedstoneTransformerBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {
    private static final int UPDATE_INTERVAL_TICKS = 5;

    public FeToRedstoneTransformerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FE_TO_REDSTONE_TRANSFORMER.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (level.getGameTime() % UPDATE_INTERVAL_TICKS != 0) {
            return;
        }

        Direction facing = state.getValue(FeToRedstoneTransformerBlock.FACING);
        IEnergyStorage storage = MountedDeviceHelper.getAttachedEnergyStorage(level, pos, facing);
        int signal = 0;

        if (storage != null && storage.getMaxEnergyStored() > 0) {
            signal = Mth.clamp(storage.getEnergyStored() * 15 / storage.getMaxEnergyStored(), 0, 15);
        }

        if (state.getValue(FeToRedstoneTransformerBlock.POWER) != signal) {
            level.setBlock(pos, state.setValue(FeToRedstoneTransformerBlock.POWER, signal), 3);
            level.updateNeighborsAt(pos, state.getBlock());
            for (Direction direction : Direction.values()) {
                level.updateNeighborsAt(pos.relative(direction), state.getBlock());
            }
        }
    }
}