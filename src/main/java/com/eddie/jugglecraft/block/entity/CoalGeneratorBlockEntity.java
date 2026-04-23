package com.eddie.jugglecraft.block.entity;

import com.eddie.jugglecraft.energy.EnergyTransferHelper;
import com.eddie.jugglecraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoalGeneratorBlockEntity extends AbstractEnergyBlockEntity {
    private static final int CAPACITY = 16000;
    private static final int GENERATION_PER_TICK = 40;
    private static final int MAX_OUTPUT_PER_TICK = 80;

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> itemHandlerLazyOptional = LazyOptional.empty();
    private int burnTime;
    private int maxBurnTime;

    public CoalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COAL_GENERATOR.get(), pos, state, CAPACITY, 0, MAX_OUTPUT_PER_TICK);
        this.itemHandlerLazyOptional = LazyOptional.of(() -> itemHandler);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean changed = false;

        if (burnTime > 0) {
            burnTime--;
            energyStorage.addEnergy(GENERATION_PER_TICK);
            changed = true;
        }

        if (burnTime <= 0 && energyStorage.getEnergyStored() <= energyStorage.getMaxEnergyStored() - GENERATION_PER_TICK) {
            changed |= tryStartBurningFuel();
        }

        if (energyStorage.getEnergyStored() > 0) {
            changed |= EnergyTransferHelper.pushEnergyToNeighbors(level, pos, energyStorage, MAX_OUTPUT_PER_TICK) > 0;
        }

        if (changed) {
            setChanged();
        }
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    public String describeStatus() {
        return String.format("Coal Generator fuel=%d/%d energy=%d/%d FE",
                burnTime, Math.max(maxBurnTime, 0), getStoredEnergy(), getEnergyCapacity());
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    private boolean tryStartBurningFuel() {
        ItemStack fuelStack = itemHandler.getStackInSlot(0);
        int fuelValue = ForgeHooks.getBurnTime(fuelStack, RecipeType.SMELTING);

        if (fuelStack.isEmpty() || fuelValue <= 0) {
            return false;
        }

        burnTime = fuelValue;
        maxBurnTime = fuelValue;
        fuelStack.shrink(1);
        itemHandler.setStackInSlot(0, fuelStack);
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("burnTime", burnTime);
        tag.putInt("maxBurnTime", maxBurnTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        burnTime = tag.getInt("burnTime");
        maxBurnTime = tag.getInt("maxBurnTime");
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable net.minecraft.core.Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandlerLazyOptional.cast();
        }

        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerLazyOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandlerLazyOptional = LazyOptional.of(() -> itemHandler);
    }
}