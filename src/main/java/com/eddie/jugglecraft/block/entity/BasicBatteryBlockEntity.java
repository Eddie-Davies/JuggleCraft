package com.eddie.jugglecraft.block.entity;

import com.eddie.jugglecraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BasicBatteryBlockEntity extends AbstractEnergyBlockEntity {
    private static final int CAPACITY = 100000;
    private static final int MAX_TRANSFER = 200;

    public BasicBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BASIC_BATTERY.get(), pos, state, CAPACITY, MAX_TRANSFER, MAX_TRANSFER);
    }

    public String describeStatus() {
        return String.format("Basic Battery energy=%d/%d FE", getStoredEnergy(), getEnergyCapacity());
    }
}