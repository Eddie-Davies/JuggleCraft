package com.eddie.jugglecraft.block.entity;

import com.eddie.jugglecraft.block.custom.FeToRedstoneTransformerBlock;
import com.eddie.jugglecraft.registry.ModBlockEntities;
import com.eddie.jugglecraft.util.MountedDeviceHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.energy.IEnergyStorage;

public class FeToRedstoneTransformerBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {
    private static final TransformerProfile BATTERY_PROFILE = new TransformerProfile(5, 25);
    private static final TransformerProfile COAL_GENERATOR_PROFILE = new TransformerProfile(1, 40);
    private static final TransformerProfile DEFAULT_PROFILE = new TransformerProfile(5, 20);

    public FeToRedstoneTransformerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FE_TO_REDSTONE_TRANSFORMER.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FeToRedstoneTransformerBlock.FACING);
        BlockEntity attachedBlockEntity = level.getBlockEntity(MountedDeviceHelper.getAttachedBlockPos(pos, facing));
        TransformerProfile profile = getProfile(attachedBlockEntity);
        if (level.getGameTime() % profile.updateIntervalTicks() != 0) {
            return;
        }

        int signal = calculateSignal(level, pos, facing, attachedBlockEntity);
        if (signal > 0 && !consumeEnergyForSignal(level, pos, facing, attachedBlockEntity, profile.energyCostPerUpdate())) {
            signal = 0;
        }

        if (state.getValue(FeToRedstoneTransformerBlock.POWER) != signal) {
            level.setBlock(pos, state.setValue(FeToRedstoneTransformerBlock.POWER, signal), 3);
            level.updateNeighborsAt(pos, state.getBlock());
            for (Direction direction : Direction.values()) {
                level.updateNeighborsAt(pos.relative(direction), state.getBlock());
            }
        }
    }

    private int calculateSignal(Level level, BlockPos pos, Direction facing, BlockEntity attachedBlockEntity) {
        if (attachedBlockEntity instanceof CoalGeneratorBlockEntity coalGeneratorBlockEntity) {
            return coalGeneratorBlockEntity.getCurrentOutputPerTick() > 0 ? 15 : 0;
        }

        IEnergyStorage storage = MountedDeviceHelper.getAttachedEnergyStorage(level, pos, facing);
        if (storage == null || storage.getMaxEnergyStored() <= 0) {
            return 0;
        }

        return Mth.clamp(storage.getEnergyStored() * 15 / storage.getMaxEnergyStored(), 0, 15);
    }

    private boolean consumeEnergyForSignal(Level level, BlockPos pos, Direction facing, BlockEntity attachedBlockEntity, int energyCostPerUpdate) {
        if (attachedBlockEntity instanceof BasicBatteryBlockEntity basicBatteryBlockEntity) {
            return basicBatteryBlockEntity.consumeEnergyForMountedTransformer(energyCostPerUpdate);
        }

        if (attachedBlockEntity instanceof CoalGeneratorBlockEntity coalGeneratorBlockEntity) {
            return coalGeneratorBlockEntity.consumeEnergyForMountedTransformer(energyCostPerUpdate);
        }

        IEnergyStorage storage = MountedDeviceHelper.getAttachedEnergyStorage(level, pos, facing);
        if (storage == null || storage.extractEnergy(energyCostPerUpdate, true) < energyCostPerUpdate) {
            return false;
        }

        storage.extractEnergy(energyCostPerUpdate, false);
        return true;
    }

    private TransformerProfile getProfile(BlockEntity attachedBlockEntity) {
        if (attachedBlockEntity instanceof BasicBatteryBlockEntity) {
            return BATTERY_PROFILE;
        }

        if (attachedBlockEntity instanceof CoalGeneratorBlockEntity) {
            return COAL_GENERATOR_PROFILE;
        }

        return DEFAULT_PROFILE;
    }

    private record TransformerProfile(int updateIntervalTicks, int energyCostPerUpdate) {
    }
}