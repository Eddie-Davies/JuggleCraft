package com.eddie.jugglecraft.menu;

import com.eddie.jugglecraft.block.entity.JboxBlockEntity;
import com.eddie.jugglecraft.logic.BatteryChargeMode;
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

public class JboxMenu extends AbstractContainerMenu {
    public static final int DATA_COUNT = 10;
    public static final int CONTROL_DEVICE_BUTTON = 0;
    public static final int ADD_DEVICE_BUTTON = 1;
    public static final int SELECT_PREVIOUS_DEVICE_BUTTON = 2;
    public static final int SELECT_NEXT_DEVICE_BUTTON = 3;
    public static final int REMOVE_DEVICE_BUTTON = 4;
    public static final int LOCAL_IP_DECREMENT_BASE = 10;
    public static final int LOCAL_IP_INCREMENT_BASE = 20;
    public static final int TARGET_IP_DECREMENT_BASE = 30;
    public static final int TARGET_IP_INCREMENT_BASE = 40;
    private static final int PLAYER_INV_START = 0;
    private static final int PLAYER_INV_END = 27;
    private static final int HOTBAR_START = 27;
    private static final int HOTBAR_END = 36;

    private final JboxBlockEntity blockEntity;
    private final ContainerData data;

    public JboxMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, getBlockEntity(inventory, buffer), new SimpleContainerData(DATA_COUNT));
    }

    public JboxMenu(int containerId, Inventory inventory, JboxBlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.JBOX.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        checkContainerDataCount(data, DATA_COUNT);
        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);
        addDataSlots(data);
    }

    public int getAttachedDeviceType() {
        return data.get(0);
    }

    public int getTrackedControlState() {
        return data.get(1);
    }

    public int getTrackedEnergy() {
        return data.get(2);
    }

    public int getTrackedCapacity() {
        return data.get(3);
    }

    public int getIpAddress() {
        return data.get(4);
    }

    public int getPendingDeviceIp() {
        return data.get(5);
    }

    public int getSelectedDeviceSlot() {
        return data.get(6);
    }

    public int getRegisteredDeviceCount() {
        return data.get(7);
    }

    public int getSelectedDeviceRegistration() {
        return data.get(8);
    }

    public boolean isTrackedDeviceOnline() {
        return data.get(9) != 0;
    }

    public BatteryChargeMode getBatteryChargeMode() {
        return BatteryChargeMode.byId(getTrackedControlState());
    }

    public boolean isGeneratorEnabled() {
        return getTrackedControlState() != 0;
    }

    public int getEnergyProgress() {
        int capacity = getTrackedCapacity();
        if (capacity <= 0) {
            return 0;
        }

        return Math.min(50, getTrackedEnergy() * 50 / capacity);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == CONTROL_DEVICE_BUTTON) {
            blockEntity.controlAttachedDevice();
            return true;
        }

        if (id == ADD_DEVICE_BUTTON) {
            blockEntity.addPendingDevice();
            return true;
        }

        if (id == SELECT_PREVIOUS_DEVICE_BUTTON) {
            blockEntity.selectPreviousDeviceSlot();
            return true;
        }

        if (id == SELECT_NEXT_DEVICE_BUTTON) {
            blockEntity.selectNextDeviceSlot();
            return true;
        }

        if (id == REMOVE_DEVICE_BUTTON) {
            blockEntity.removeSelectedDevice();
            return true;
        }

        if (id >= LOCAL_IP_DECREMENT_BASE && id < LOCAL_IP_DECREMENT_BASE + 4) {
            blockEntity.adjustIpOctet(id - LOCAL_IP_DECREMENT_BASE, -1);
            return true;
        }

        if (id >= LOCAL_IP_INCREMENT_BASE && id < LOCAL_IP_INCREMENT_BASE + 4) {
            blockEntity.adjustIpOctet(id - LOCAL_IP_INCREMENT_BASE, 1);
            return true;
        }

        if (id >= TARGET_IP_DECREMENT_BASE && id < TARGET_IP_DECREMENT_BASE + 4) {
            blockEntity.adjustPendingDeviceIpOctet(id - TARGET_IP_DECREMENT_BASE, -1);
            return true;
        }

        if (id >= TARGET_IP_INCREMENT_BASE && id < TARGET_IP_INCREMENT_BASE + 4) {
            blockEntity.adjustPendingDeviceIpOctet(id - TARGET_IP_INCREMENT_BASE, 1);
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
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ModBlocks.JBOX.get());
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

    private static JboxBlockEntity getBlockEntity(Inventory inventory, FriendlyByteBuf buffer) {
        if (inventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof JboxBlockEntity jboxBlockEntity) {
            return jboxBlockEntity;
        }

        throw new IllegalStateException("Missing J-Box block entity for menu");
    }
}