package com.eddie.jugglepower.block.entity;

import com.eddie.jugglepower.power.PowerTier;
import com.eddie.jugglepower.registry.PowerBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ElectricFurnaceBlockEntity extends AbstractTieredPowerBlockEntity {
    private static final int CAPACITY = 12000;
    private static final int ENERGY_PER_TICK = 20;
    private static final int REQUIRED_PROGRESS = 200;

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> itemOptional = LazyOptional.empty();
    private int progress;
    private boolean enabled = true;
    private int faultTicks;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(PowerBlockEntities.ELECTRIC_FURNACE.get(), pos, state, PowerTier.LV,
                CAPACITY, PowerTier.LV.getDefaultMaxTransfer(), 0);
        itemOptional = LazyOptional.of(() -> itemHandler);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean changed = false;

        if (faultTicks > 0) {
            faultTicks--;
            changed = true;
        }

        Optional<SmeltingRecipe> recipeOptional = getCurrentRecipe(level);
        if (!enabled || faultTicks > 0 || recipeOptional.isEmpty() || !canOutput(recipeOptional.get(), level)) {
            if (progress != 0) {
                progress = 0;
                changed = true;
            }

            if (changed) {
                setChanged();
            }
            return;
        }

        if (energyStorage.extractEnergy(ENERGY_PER_TICK, true) < ENERGY_PER_TICK) {
            if (progress != 0) {
                progress = 0;
                changed = true;
            }

            if (changed) {
                setChanged();
            }
            return;
        }

        energyStorage.extractEnergy(ENERGY_PER_TICK, false);
        progress++;
        changed = true;

        if (progress >= REQUIRED_PROGRESS) {
            craft(recipeOptional.get(), level);
            progress = 0;
        }

        if (changed) {
            setChanged();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggleEnabled() {
        enabled = !enabled;
        setChanged();
    }

    public int getProgress() {
        return progress;
    }

    public int getRequiredProgress() {
        return REQUIRED_PROGRESS;
    }

    @Override
    public void appendPowerStateLines(List<Component> lines) {
        super.appendPowerStateLines(lines);
        lines.add(Component.literal("State: " + (enabled ? "ON" : "OFF")));
        lines.add(Component.literal("Progress: " + progress + "/" + REQUIRED_PROGRESS));
    }

    public boolean tryInsertInput(Player player, ItemStack heldStack) {
        if (heldStack.isEmpty()) {
            return false;
        }

        if (!itemHandler.getStackInSlot(0).isEmpty()) {
            return false;
        }

        ItemStack insert = heldStack.copy();
        insert.setCount(1);
        itemHandler.setStackInSlot(0, insert);
        heldStack.shrink(1);
        setChanged();
        return true;
    }

    public boolean tryExtractOutput(Player player) {
        ItemStack output = itemHandler.getStackInSlot(1);
        if (output.isEmpty()) {
            return false;
        }

        ItemStack toGive = output.copy();
        itemHandler.setStackInSlot(1, ItemStack.EMPTY);
        if (!player.addItem(toGive)) {
            ItemEntity itemEntity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), toGive);
            player.level().addFreshEntity(itemEntity);
        }

        setChanged();
        return true;
    }

    @Override
    public void markTierFault(@Nullable PowerTier neighborTier) {
        if (faultTicks == 0) {
            faultTicks = 40;
            setChanged();
        }
    }

    @Override
    public boolean isTierFaulted() {
        return faultTicks > 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("progress", progress);
        tag.putBoolean("enabled", enabled);
        tag.putInt("faultTicks", faultTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        enabled = !tag.contains("enabled") || tag.getBoolean("enabled");
        faultTicks = tag.getInt("faultTicks");
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable net.minecraft.core.Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemOptional.cast();
        }

        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemOptional = LazyOptional.of(() -> itemHandler);
    }

    private Optional<SmeltingRecipe> getCurrentRecipe(Level level) {
        ItemStack input = itemHandler.getStackInSlot(0);
        if (input.isEmpty()) {
            return Optional.empty();
        }

        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(input), level);
    }

    private boolean canOutput(SmeltingRecipe recipe, Level level) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        if (result.isEmpty()) {
            return false;
        }

        ItemStack output = itemHandler.getStackInSlot(1);
        if (output.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameTags(output, result)) {
            return false;
        }

        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void craft(SmeltingRecipe recipe, Level level) {
        ItemStack input = itemHandler.getStackInSlot(0);
        ItemStack result = recipe.getResultItem(level.registryAccess()).copy();
        ItemStack output = itemHandler.getStackInSlot(1);

        input.shrink(1);
        itemHandler.setStackInSlot(0, input);

        if (output.isEmpty()) {
            itemHandler.setStackInSlot(1, result);
            return;
        }

        output.grow(result.getCount());
        itemHandler.setStackInSlot(1, output);
    }
}
