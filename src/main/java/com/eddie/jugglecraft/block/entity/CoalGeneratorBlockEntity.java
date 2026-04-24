package com.eddie.jugglecraft.block.entity;

import com.eddie.jugglecraft.energy.EnergyTransferHelper;
import com.eddie.jugglecraft.logic.DeviceAddress;
import com.eddie.jugglecraft.logic.IpAddressableDevice;
import com.eddie.jugglecraft.logic.PlacementOrderedNode;
import com.eddie.jugglecraft.menu.CoalGeneratorMenu;
import com.eddie.jugglecraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
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

public class CoalGeneratorBlockEntity extends AbstractEnergyBlockEntity implements MenuProvider, IpAddressableDevice, PlacementOrderedNode {
    private static final int GENERATION_PER_TICK = 40;
    private static final int MAX_OUTPUT_PER_TICK = GENERATION_PER_TICK;

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> itemHandlerLazyOptional = LazyOptional.empty();
    private int burnTime;
    private int maxBurnTime;
    private boolean generatorEnabled = true;
    private int ipAddress = DeviceAddress.DEVICE_DEFAULT;
    private long placementOrder = -1L;

    public CoalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COAL_GENERATOR.get(), pos, state, 0, 0, 0);
        this.itemHandlerLazyOptional = LazyOptional.of(() -> itemHandler);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        ensurePlacementOrder(level);
        boolean changed = false;

        if (generatorEnabled) {
            if (burnTime <= 0) {
                changed |= tryStartBurningFuel();
            }

            if (burnTime > 0) {
                burnTime--;
                changed = true;
                EnergyTransferHelper.pushEnergyToNeighbors(level, pos, GENERATION_PER_TICK, MAX_OUTPUT_PER_TICK);
            }
        }

        if (changed) {
            setChanged();
        }
    }

    private void ensurePlacementOrder(Level level) {
        if (placementOrder >= 0 || level.isClientSide) {
            return;
        }

        placementOrder = level.getGameTime();
        setChanged();
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    public boolean isBurning() {
        return burnTime > 0;
    }

    public int getCurrentOutputPerTick() {
        return generatorEnabled && isBurning() ? GENERATION_PER_TICK : 0;
    }

    public int getMaxOutputPerTick() {
        return MAX_OUTPUT_PER_TICK;
    }

    @Override
    public int getIpAddress() {
        return ipAddress;
    }

    @Override
    public void setIpAddress(int ipAddress) {
        this.ipAddress = ipAddress;
        setChanged();
    }

    @Override
    public long getPlacementOrder() {
        return placementOrder;
    }

    public boolean isGeneratorEnabled() {
        return generatorEnabled;
    }

    public void toggleGeneratorEnabled() {
        generatorEnabled = !generatorEnabled;
        setChanged();
    }

    public String describeStatus() {
        return String.format("Coal Generator enabled=%s fuel=%d/%d output=%d FE/t ip=%s",
            generatorEnabled, burnTime, Math.max(maxBurnTime, 0), GENERATION_PER_TICK, getIpAddressText());
    }

    public boolean consumeEnergyForMountedTransformer(int energyAmount) {
        if (energyAmount <= 0 || !generatorEnabled || burnTime <= 0) {
            return false;
        }

        int extraBurnTicks = (energyAmount + GENERATION_PER_TICK - 1) / GENERATION_PER_TICK;
        if (burnTime < extraBurnTicks) {
            return false;
        }

        burnTime -= extraBurnTicks;
        setChanged();
        return true;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.jugglecraft.coal_generator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CoalGeneratorMenu(containerId, inventory, this, new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> burnTime;
                    case 1 -> maxBurnTime;
                    case 2 -> getCurrentOutputPerTick();
                    case 3 -> getMaxOutputPerTick();
                    case 4 -> isGeneratorEnabled() ? 1 : 0;
                    case 5 -> getIpAddress();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return CoalGeneratorMenu.DATA_COUNT;
            }
        });
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
        tag.putBoolean("generatorEnabled", generatorEnabled);
        tag.putInt("ipAddress", ipAddress);
        tag.putLong("placementOrder", placementOrder);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        burnTime = tag.getInt("burnTime");
        maxBurnTime = tag.getInt("maxBurnTime");
        generatorEnabled = tag.getBoolean("generatorEnabled");
        ipAddress = tag.contains("ipAddress") ? tag.getInt("ipAddress") : DeviceAddress.DEVICE_DEFAULT;
        placementOrder = tag.contains("placementOrder") ? tag.getLong("placementOrder") : -1L;
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