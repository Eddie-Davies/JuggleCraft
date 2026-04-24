package com.eddie.jugglecraft.block.entity;

import com.eddie.jugglecraft.logic.DeviceAddress;
import com.eddie.jugglecraft.logic.IpAddressableDevice;
import com.eddie.jugglecraft.logic.PlacementOrderedNode;
import com.eddie.jugglecraft.logic.Rs485NetworkHelper;
import com.eddie.jugglecraft.menu.JboxMenu;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class JboxBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity implements MenuProvider, IpAddressableDevice, PlacementOrderedNode {
    private static final int SCAN_INTERVAL_TICKS = 10;
    public static final int DEVICE_NONE = 0;
    public static final int DEVICE_BATTERY = 1;
    public static final int DEVICE_GENERATOR = 2;
    public static final int MAX_CONNECTED_DEVICES = 16;

    private int attachedDeviceType = DEVICE_NONE;
    private int trackedControlState;
    private int trackedEnergy;
    private int trackedCapacity;
    private boolean trackedDeviceOnline;
    private int ipAddress = DeviceAddress.JBOX_DEFAULT;
    private int pendingDeviceIp = DeviceAddress.DEVICE_DEFAULT;
    private int selectedDeviceSlot;
    private final int[] registeredDeviceIps = new int[MAX_CONNECTED_DEVICES];
    private long placementOrder = -1L;

    public JboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.JBOX.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        ensurePlacementOrder(level);

        if (level.getGameTime() % SCAN_INTERVAL_TICKS != 0) {
            return;
        }

        syncTrackedDevice(level, pos);
    }

    private void ensurePlacementOrder(Level level) {
        if (placementOrder >= 0 || level.isClientSide) {
            return;
        }

        placementOrder = level.getGameTime();
        setChanged();
    }

    private void syncTrackedDevice(Level level, BlockPos pos) {
        int nextDeviceType = DEVICE_NONE;
        int nextControlState = 0;
        int nextEnergy = 0;
        int nextCapacity = 0;
        boolean nextOnline = false;

        BlockEntity blockEntity = getSelectedConnectedDevice(level, pos);
        if (blockEntity instanceof BasicBatteryBlockEntity basicBatteryBlockEntity) {
            nextDeviceType = DEVICE_BATTERY;
            nextControlState = basicBatteryBlockEntity.getChargeMode().ordinal();
            nextEnergy = basicBatteryBlockEntity.getStoredEnergy();
            nextCapacity = basicBatteryBlockEntity.getEnergyCapacity();
            nextOnline = true;
        } else if (blockEntity instanceof CoalGeneratorBlockEntity coalGeneratorBlockEntity) {
            nextDeviceType = DEVICE_GENERATOR;
            nextControlState = coalGeneratorBlockEntity.isGeneratorEnabled() ? 1 : 0;
            nextEnergy = coalGeneratorBlockEntity.getCurrentOutputPerTick();
            nextCapacity = coalGeneratorBlockEntity.getMaxOutputPerTick();
            nextOnline = true;
        }

        if (attachedDeviceType != nextDeviceType || trackedControlState != nextControlState
                || trackedEnergy != nextEnergy || trackedCapacity != nextCapacity
                || trackedDeviceOnline != nextOnline) {
            attachedDeviceType = nextDeviceType;
            trackedControlState = nextControlState;
            trackedEnergy = nextEnergy;
            trackedCapacity = nextCapacity;
            trackedDeviceOnline = nextOnline;
            setChanged();
        }
    }

    public String describeStatus() {
        return String.format("J-Box devices=%d selected=%d online=%s ip=%s target=%s",
                getRegisteredDeviceCount(), selectedDeviceSlot + 1, trackedDeviceOnline, getIpAddressText(), DeviceAddress.toString(pendingDeviceIp));
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

    public int getPendingDeviceIp() {
        return pendingDeviceIp;
    }

    public void adjustPendingDeviceIpOctet(int octetIndex, int delta) {
        pendingDeviceIp = DeviceAddress.adjustOctet(pendingDeviceIp, octetIndex, delta);
        setChanged();
    }

    public int getSelectedDeviceSlot() {
        return selectedDeviceSlot;
    }

    public void selectPreviousDeviceSlot() {
        selectedDeviceSlot = (selectedDeviceSlot + MAX_CONNECTED_DEVICES - 1) % MAX_CONNECTED_DEVICES;
        if (level != null) {
            syncTrackedDevice(level, worldPosition);
        }
        setChanged();
    }

    public void selectNextDeviceSlot() {
        selectedDeviceSlot = (selectedDeviceSlot + 1) % MAX_CONNECTED_DEVICES;
        if (level != null) {
            syncTrackedDevice(level, worldPosition);
        }
        setChanged();
    }

    public int getRegisteredDeviceCount() {
        int count = 0;
        for (int registeredDeviceIp : registeredDeviceIps) {
            if (registeredDeviceIp != 0) {
                count++;
            }
        }
        return count;
    }

    public int getSelectedDeviceRegistration() {
        return registeredDeviceIps[selectedDeviceSlot];
    }

    public boolean hasRegisteredIp(int ipAddress) {
        return isRegisteredIp(ipAddress);
    }

    public boolean isTrackedDeviceOnline() {
        return trackedDeviceOnline;
    }

    public int getAttachedDeviceType() {
        return attachedDeviceType;
    }

    public int getTrackedControlState() {
        return trackedControlState;
    }

    public int getTrackedEnergy() {
        return trackedEnergy;
    }

    public int getTrackedCapacity() {
        return trackedCapacity;
    }

    public boolean addPendingDevice() {
        if (level == null || pendingDeviceIp == 0 || isRegisteredIp(pendingDeviceIp)) {
            return false;
        }

        BlockEntity blockEntity = Rs485NetworkHelper.findConnectedControllableDeviceByIp(level, worldPosition, pendingDeviceIp);
        if (!Rs485NetworkHelper.isControllableDevice(blockEntity)) {
            return false;
        }

        for (int slot = 0; slot < registeredDeviceIps.length; slot++) {
            if (registeredDeviceIps[slot] == 0) {
                registeredDeviceIps[slot] = pendingDeviceIp;
                selectedDeviceSlot = slot;
                syncTrackedDevice(level, worldPosition);
                setChanged();
                return true;
            }
        }

        return false;
    }

    public boolean removeSelectedDevice() {
        if (registeredDeviceIps[selectedDeviceSlot] == 0) {
            return false;
        }

        registeredDeviceIps[selectedDeviceSlot] = 0;
        attachedDeviceType = DEVICE_NONE;
        trackedControlState = 0;
        trackedEnergy = 0;
        trackedCapacity = 0;
        trackedDeviceOnline = false;
        setChanged();
        return true;
    }

    public boolean controlAttachedDevice() {
        if (level == null) {
            return false;
        }

        BlockEntity blockEntity = getSelectedConnectedDevice(level, worldPosition);
        if (blockEntity instanceof BasicBatteryBlockEntity basicBatteryBlockEntity) {
            basicBatteryBlockEntity.cycleChargeMode();
            syncTrackedDevice(level, worldPosition);
            return true;
        }

        if (blockEntity instanceof CoalGeneratorBlockEntity coalGeneratorBlockEntity) {
            coalGeneratorBlockEntity.toggleGeneratorEnabled();
            syncTrackedDevice(level, worldPosition);
            return true;
        }

        return false;
    }

    private @Nullable BlockEntity getSelectedConnectedDevice(Level level, BlockPos pos) {
        int selectedIp = getSelectedDeviceRegistration();
        if (selectedIp == 0) {
            return null;
        }

        return Rs485NetworkHelper.findConnectedControllableDeviceByIp(level, pos, selectedIp, this);
    }

    private boolean isRegisteredIp(int ipAddress) {
        for (int registeredDeviceIp : registeredDeviceIps) {
            if (registeredDeviceIp == ipAddress) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.jugglecraft.jbox");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new JboxMenu(containerId, inventory, this, new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> getAttachedDeviceType();
                    case 1 -> getTrackedControlState();
                    case 2 -> getTrackedEnergy();
                    case 3 -> getTrackedCapacity();
                    case 4 -> getIpAddress();
                    case 5 -> getPendingDeviceIp();
                    case 6 -> getSelectedDeviceSlot();
                    case 7 -> getRegisteredDeviceCount();
                    case 8 -> getSelectedDeviceRegistration();
                    case 9 -> isTrackedDeviceOnline() ? 1 : 0;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return JboxMenu.DATA_COUNT;
            }
        });
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("attachedDeviceType", attachedDeviceType);
        tag.putInt("trackedControlState", trackedControlState);
        tag.putInt("trackedEnergy", trackedEnergy);
        tag.putInt("trackedCapacity", trackedCapacity);
        tag.putBoolean("trackedDeviceOnline", trackedDeviceOnline);
        tag.putInt("ipAddress", ipAddress);
        tag.putInt("pendingDeviceIp", pendingDeviceIp);
        tag.putInt("selectedDeviceSlot", selectedDeviceSlot);
        tag.putIntArray("registeredDeviceIps", registeredDeviceIps);
        tag.putLong("placementOrder", placementOrder);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        attachedDeviceType = tag.getInt("attachedDeviceType");
        trackedControlState = tag.getInt("trackedControlState");
        trackedEnergy = tag.getInt("trackedEnergy");
        trackedCapacity = tag.getInt("trackedCapacity");
        trackedDeviceOnline = tag.getBoolean("trackedDeviceOnline");
        ipAddress = tag.contains("ipAddress") ? tag.getInt("ipAddress") : DeviceAddress.JBOX_DEFAULT;
        pendingDeviceIp = tag.contains("pendingDeviceIp") ? tag.getInt("pendingDeviceIp") : DeviceAddress.DEVICE_DEFAULT;
        selectedDeviceSlot = Math.max(0, Math.min(MAX_CONNECTED_DEVICES - 1, tag.getInt("selectedDeviceSlot")));
        placementOrder = tag.contains("placementOrder") ? tag.getLong("placementOrder") : -1L;

        int[] savedIps = tag.getIntArray("registeredDeviceIps");
        for (int i = 0; i < registeredDeviceIps.length; i++) {
            registeredDeviceIps[i] = i < savedIps.length ? savedIps[i] : 0;
        }
    }
}