package com.eddie.jugglecraft.block.entity;

import com.eddie.jugglecraft.energy.EnergyTransferHelper;
import com.eddie.jugglecraft.logic.BatteryChargeMode;
import com.eddie.jugglecraft.menu.BasicBatteryMenu;
import com.eddie.jugglecraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BasicBatteryBlockEntity extends AbstractEnergyBlockEntity implements MenuProvider {
    private static final int CAPACITY = 100000;
    private static final int MAX_TRANSFER = 200;

    private final IEnergyStorage exposedEnergyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (chargeMode != BatteryChargeMode.CHARGING) {
                return 0;
            }

            return energyStorage.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (chargeMode != BatteryChargeMode.DISCHARGING) {
                return 0;
            }

            return energyStorage.extractEnergy(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return energyStorage.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return energyStorage.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return chargeMode == BatteryChargeMode.DISCHARGING && energyStorage.canExtract();
        }

        @Override
        public boolean canReceive() {
            return chargeMode == BatteryChargeMode.CHARGING && energyStorage.canReceive();
        }
    };

    private LazyOptional<IEnergyStorage> batteryEnergyCapability = LazyOptional.empty();
    private BatteryChargeMode chargeMode = BatteryChargeMode.CHARGING;

    public BasicBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BASIC_BATTERY.get(), pos, state, CAPACITY, MAX_TRANSFER, MAX_TRANSFER);
        batteryEnergyCapability = LazyOptional.of(() -> exposedEnergyStorage);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (chargeMode == BatteryChargeMode.DISCHARGING && energyStorage.getEnergyStored() > 0
                && EnergyTransferHelper.pushEnergyToNeighbors(level, pos, exposedEnergyStorage, MAX_TRANSFER) > 0) {
            setChanged();
        }
    }

    public BatteryChargeMode getChargeMode() {
        return chargeMode;
    }

    public void cycleChargeMode() {
        chargeMode = chargeMode.next();
        setChanged();
    }

    public String describeStatus() {
        return String.format("Basic Battery mode=%s energy=%d/%d FE", chargeMode.name(), getStoredEnergy(), getEnergyCapacity());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.jugglecraft.basic_battery");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new BasicBatteryMenu(containerId, inventory, this, new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> getStoredEnergy();
                    case 1 -> getEnergyCapacity();
                    case 2 -> getChargeMode().ordinal();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return BasicBatteryMenu.DATA_COUNT;
            }
        });
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("chargeMode", chargeMode.ordinal());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        chargeMode = BatteryChargeMode.byId(tag.getInt("chargeMode"));
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable net.minecraft.core.Direction side) {
        if (capability == ForgeCapabilities.ENERGY) {
            return batteryEnergyCapability.cast();
        }

        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        batteryEnergyCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        batteryEnergyCapability = LazyOptional.of(() -> exposedEnergyStorage);
    }
}