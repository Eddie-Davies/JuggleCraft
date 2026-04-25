package com.eddie.jugglepower.block.entity;

import com.eddie.jugglepower.power.PowerNetworkHelper;
import com.eddie.jugglepower.power.PowerTier;
import com.eddie.jugglepower.power.TieredEnergyStorage;
import com.eddie.jugglepower.power.TieredPowerNode;
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

public abstract class AbstractTieredPowerBlockEntity extends BlockEntity implements TieredPowerNode {
    private final PowerTier powerTier;
    protected final TieredEnergyStorage energyStorage;
    private LazyOptional<IEnergyStorage> energyOptional = LazyOptional.empty();

    protected AbstractTieredPowerBlockEntity(BlockEntityType<?> type,
                                             BlockPos pos,
                                             BlockState state,
                                             PowerTier powerTier,
                                             int capacity,
                                             int maxReceive,
                                             int maxExtract) {
        super(type, pos, state);
        this.powerTier = powerTier;
        this.energyStorage = new TieredEnergyStorage(capacity, maxReceive, maxExtract) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
        this.energyOptional = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public PowerTier getPowerTier() {
        return powerTier;
    }

    @Override
    public IEnergyStorage getTieredEnergyStorage() {
        return energyStorage;
    }

    public int getStoredEnergy() {
        return energyStorage.getEnergyStored();
    }

    public int getEnergyCapacity() {
        return energyStorage.getMaxEnergyStored();
    }

    protected int pushToMatchedNeighbors(int maxTransfer) {
        if (level == null || level.isClientSide || maxTransfer <= 0) {
            return 0;
        }

        return PowerNetworkHelper.pushToTierMatchedNeighbors(level, worldPosition, this, maxTransfer);
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
            return energyOptional.cast();
        }

        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyOptional = LazyOptional.of(() -> energyStorage);
    }
}
