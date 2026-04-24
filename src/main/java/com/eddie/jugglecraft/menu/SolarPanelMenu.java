package com.eddie.jugglecraft.menu;

import com.eddie.jugglecraft.block.entity.SolarPanelBlockEntity;
import com.eddie.jugglecraft.registry.ModBlocks;
import com.eddie.jugglecraft.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SolarPanelMenu extends AbstractContainerMenu {
    public static final int DATA_COUNT = 10;
    public static final int TOGGLE_PANEL_BUTTON = 0;
    public static final int IP_OCTET_DECREMENT_BASE = 10;
    public static final int IP_OCTET_INCREMENT_BASE = 20;
    private static final int PLAYER_INV_START = 0;
    private static final int PLAYER_INV_END = 27;
    private static final int HOTBAR_START = 27;
    private static final int HOTBAR_END = 36;

    private final SolarPanelBlockEntity blockEntity;
    private final ContainerData data;

    public SolarPanelMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, getBlockEntity(inventory, buffer), new SimpleContainerData(DATA_COUNT));
    }

    public SolarPanelMenu(int containerId, Inventory inventory, SolarPanelBlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.SOLAR_PANEL.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        checkContainerDataCount(data, DATA_COUNT);
        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);
        addDataSlots(data);
    }

    public int getEnergyStored() {
        return data.get(0);
    }

    public int getEnergyCapacity() {
        return data.get(1);
    }

    public int getCurrentGeneration() {
        return data.get(2);
    }

    public int getMaxGeneration() {
        return data.get(3);
    }

    public boolean isPanelEnabled() {
        return data.get(4) != 0;
    }

    public int getIpAddress() {
        return data.get(5);
    }

    public int getSunFactorPercent() {
        return data.get(6);
    }

    public int getHeightFactorPercent() {
        return data.get(7);
    }

    public int getWeatherFactorPercent() {
        return data.get(8);
    }

    public int getSkyFactorPercent() {
        return data.get(9);
    }

    public int getEnergyProgress() {
        int capacity = getEnergyCapacity();
        if (capacity <= 0) {
            return 0;
        }

        return Math.min(60, getEnergyStored() * 60 / capacity);
    }

    public int getGenerationProgress() {
        int maxGeneration = getMaxGeneration();
        if (maxGeneration <= 0) {
            return 0;
        }

        return Math.min(60, getCurrentGeneration() * 60 / maxGeneration);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == TOGGLE_PANEL_BUTTON) {
            blockEntity.togglePanelEnabled();
            return true;
        }

        if (id >= IP_OCTET_DECREMENT_BASE && id < IP_OCTET_DECREMENT_BASE + 4) {
            blockEntity.adjustIpOctet(id - IP_OCTET_DECREMENT_BASE, -1);
            return true;
        }

        if (id >= IP_OCTET_INCREMENT_BASE && id < IP_OCTET_INCREMENT_BASE + 4) {
            blockEntity.adjustIpOctet(id - IP_OCTET_INCREMENT_BASE, 1);
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

        if (index < HOTBAR_START) {
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
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ModBlocks.SOLAR_PANEL.get());
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 142 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory inventory) {
        for (int slot = 0; slot < 9; slot++) {
            addSlot(new Slot(inventory, slot, 8 + slot * 18, 200));
        }
    }

    private static SolarPanelBlockEntity getBlockEntity(Inventory inventory, FriendlyByteBuf buffer) {
        if (inventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof SolarPanelBlockEntity solarPanelBlockEntity) {
            return solarPanelBlockEntity;
        }

        throw new IllegalStateException("Missing solar panel block entity for menu");
    }
}