package com.eddie.jugglecraft.menu;

import com.eddie.jugglecraft.block.entity.CoalGeneratorBlockEntity;
import com.eddie.jugglecraft.registry.ModBlocks;
import com.eddie.jugglecraft.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.SlotItemHandler;

public class CoalGeneratorMenu extends AbstractContainerMenu {
    public static final int DATA_COUNT = 5;
    public static final int TOGGLE_GENERATOR_BUTTON = 0;
    private static final int FUEL_SLOT_INDEX = 0;
    private static final int PLAYER_INV_START = 1;
    private static final int PLAYER_INV_END = 28;
    private static final int HOTBAR_START = 28;
    private static final int HOTBAR_END = 37;

    private final CoalGeneratorBlockEntity blockEntity;
    private final ContainerData data;

    public CoalGeneratorMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, getBlockEntity(inventory, buffer), new SimpleContainerData(DATA_COUNT));
    }

    public CoalGeneratorMenu(int containerId, Inventory inventory, CoalGeneratorBlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.COAL_GENERATOR.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        checkContainerDataCount(data, DATA_COUNT);
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), FUEL_SLOT_INDEX, 56, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
            }
        });

        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);
        addDataSlots(data);
    }

    public boolean isBurning() {
        return data.get(0) > 0;
    }

    public int getBurnProgress() {
        int burnTime = data.get(0);
        int maxBurnTime = data.get(1);
        if (burnTime <= 0 || maxBurnTime <= 0) {
            return 0;
        }

        return Math.max(1, burnTime * 13 / maxBurnTime);
    }

    public int getEnergyStored() {
        return data.get(2);
    }

    public int getEnergyCapacity() {
        return data.get(3);
    }

    public boolean isGeneratorEnabled() {
        return data.get(4) != 0;
    }

    public int getEnergyProgress() {
        int capacity = getEnergyCapacity();
        if (capacity <= 0) {
            return 0;
        }

        return Math.min(50, getEnergyStored() * 50 / capacity);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == TOGGLE_GENERATOR_BUTTON) {
            blockEntity.toggleGeneratorEnabled();
            return true;
        }

        return super.clickMenuButton(player, id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack emptyStack = ItemStack.EMPTY;
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem()) {
            return emptyStack;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copy = sourceStack.copy();

        if (index == FUEL_SLOT_INDEX) {
            if (!moveItemStackTo(sourceStack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (ForgeHooks.getBurnTime(sourceStack, RecipeType.SMELTING) > 0) {
            if (!moveItemStackTo(sourceStack, FUEL_SLOT_INDEX, FUEL_SLOT_INDEX + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < HOTBAR_START) {
            if (!moveItemStackTo(sourceStack, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(sourceStack, PLAYER_INV_START, PLAYER_INV_END, false)) {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(player, sourceStack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ModBlocks.COAL_GENERATOR.get());
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory inventory) {
        for (int slot = 0; slot < 9; slot++) {
            addSlot(new Slot(inventory, slot, 8 + slot * 18, 142));
        }
    }

    private static CoalGeneratorBlockEntity getBlockEntity(Inventory inventory, FriendlyByteBuf buffer) {
        if (inventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof CoalGeneratorBlockEntity coalGeneratorBlockEntity) {
            return coalGeneratorBlockEntity;
        }

        throw new IllegalStateException("Missing coal generator block entity for menu");
    }
}