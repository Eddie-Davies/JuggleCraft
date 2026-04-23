package com.eddie.jugglecraft.block.entity;

import com.eddie.jugglecraft.energy.ModEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractEnergyBlockEntity extends BlockEntity {
    protected final ModEnergyStorage energyStorage;
    private LazyOptional<IEnergyStorage> energyLazyOptional = LazyOptional.empty();

    protected AbstractEnergyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                        int capacity, int maxReceive, int maxExtract) {
        super(type, pos, state);
        this.energyStorage = new ModEnergyStorage(capacity, maxReceive, maxExtract) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
        this.energyLazyOptional = LazyOptional.of(() -> this.energyStorage);
    }

    public int getStoredEnergy() {
        return energyStorage.getEnergyStored();
    }

    public int getEnergyCapacity() {
        return energyStorage.getMaxEnergyStored();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("energy", energyStorage.getEnergyStored());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energyStorage.setStoredEnergy(tag.getInt("energy"));
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) {
            return energyLazyOptional.cast();
        }

        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyLazyOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyLazyOptional = LazyOptional.of(() -> energyStorage);
    }
}