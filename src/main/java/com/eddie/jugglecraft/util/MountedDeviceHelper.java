package com.eddie.jugglecraft.util;

import com.eddie.jugglecraft.block.entity.BasicBatteryBlockEntity;
import com.eddie.jugglecraft.block.entity.CoalGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public final class MountedDeviceHelper {
    private MountedDeviceHelper() {
    }

    public static BlockPos getAttachedBlockPos(BlockPos pos, Direction facing) {
        return pos.relative(facing.getOpposite());
    }

    public static @Nullable BlockEntity getAttachedBlockEntity(BlockGetter level, BlockPos pos, BlockState state) {
        if (!state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return null;
        }

        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        return level.getBlockEntity(getAttachedBlockPos(pos, facing));
    }

    public static boolean canAttachToControllableDevice(BlockGetter level, BlockPos pos, Direction facing) {
        BlockEntity blockEntity = level.getBlockEntity(getAttachedBlockPos(pos, facing));
        return blockEntity instanceof CoalGeneratorBlockEntity || blockEntity instanceof BasicBatteryBlockEntity;
    }

    public static boolean canAttachToEnergyDevice(BlockGetter level, BlockPos pos, Direction facing) {
        return getAttachedEnergyStorage(level, pos, facing) != null;
    }

    public static @Nullable IEnergyStorage getAttachedEnergyStorage(BlockGetter level, BlockPos pos, Direction facing) {
        BlockEntity blockEntity = level.getBlockEntity(getAttachedBlockPos(pos, facing));
        if (blockEntity == null) {
            return null;
        }

        return blockEntity.getCapability(ForgeCapabilities.ENERGY, facing).resolve().orElse(null);
    }
}