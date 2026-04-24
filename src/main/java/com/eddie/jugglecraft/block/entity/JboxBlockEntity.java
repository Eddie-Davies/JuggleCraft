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
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
    public static final int DEVICE_SOLAR = 3;
    public static final int MAX_CONNECTED_DEVICES = 16;

    private int attachedDeviceType = DEVICE_NONE;
    private int trackedControlState;
    private int trackedEnergy;
    private int trackedCapacity;
    private boolean trackedDeviceOnline;
    private int ipAddress = DeviceAddress.JBOX_DEFAULT;
    private int pendingDeviceIp = DeviceAddress.DEVICE_DEFAULT;
    private int selectedDeviceSlot;
    private long placementOrder = -1L;

    private final int[] registeredDeviceIps = new int[MAX_CONNECTED_DEVICES];
    private final int[] slotDeviceTypes = new int[MAX_CONNECTED_DEVICES];
    private final int[] slotControlStates = new int[MAX_CONNECTED_DEVICES];
    private final int[] slotPowerFlows = new int[MAX_CONNECTED_DEVICES];
    private final int[] slotStoredEnergy = new int[MAX_CONNECTED_DEVICES];
    private final int[] slotCapacity = new int[MAX_CONNECTED_DEVICES];
    private final int[] slotStateOfCharge = new int[MAX_CONNECTED_DEVICES];
    private final int[] slotPrimaryMetric = new int[MAX_CONNECTED_DEVICES];
    private final int[] slotSecondaryMetric = new int[MAX_CONNECTED_DEVICES];
    private final int[] slotTertiaryMetric = new int[MAX_CONNECTED_DEVICES];
    private final int[] slotQuaternaryMetric = new int[MAX_CONNECTED_DEVICES];
    private final boolean[] slotOnline = new boolean[MAX_CONNECTED_DEVICES];

    public JboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.JBOX.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        ensurePlacementOrder(level);

        if (level.getGameTime() % SCAN_INTERVAL_TICKS != 0) {
            return;
        }

        if (syncSlotSnapshots(level, pos)) {
            markDirtyAndSync();
        }
    }

    private void ensurePlacementOrder(Level level) {
        if (placementOrder >= 0 || level.isClientSide) {
            return;
        }

        placementOrder = level.getGameTime();
        markDirtyAndSync();
    }

    private boolean syncSlotSnapshots(Level level, BlockPos pos) {
        boolean changed = false;
        for (int slot = 0; slot < MAX_CONNECTED_DEVICES; slot++) {
            changed |= syncSlotSnapshot(level, pos, slot);
        }

        changed |= syncLegacySelectedSnapshot();
        return changed;
    }

    private boolean syncSlotSnapshot(Level level, BlockPos pos, int slot) {
        BlockEntity blockEntity = getConnectedDeviceForSlot(level, pos, slot);
        int nextType = DEVICE_NONE;
        int nextControlState = 0;
        int nextPowerFlow = 0;
        int nextEnergy = 0;
        int nextCapacity = 0;
        int nextStateOfCharge = 0;
        int nextPrimaryMetric = 0;
        int nextSecondaryMetric = 0;
        int nextTertiaryMetric = 0;
        int nextQuaternaryMetric = 0;
        boolean nextOnline = false;

        if (blockEntity instanceof BasicBatteryBlockEntity basicBatteryBlockEntity) {
            nextType = DEVICE_BATTERY;
            nextControlState = basicBatteryBlockEntity.getChargeMode().ordinal();
            nextPowerFlow = basicBatteryBlockEntity.getReportedPowerFlow();
            nextEnergy = basicBatteryBlockEntity.getStoredEnergy();
            nextCapacity = basicBatteryBlockEntity.getEnergyCapacity();
            nextStateOfCharge = calculateStateOfCharge(nextEnergy, nextCapacity);
            nextPrimaryMetric = nextStateOfCharge;
            nextOnline = true;
        } else if (blockEntity instanceof CoalGeneratorBlockEntity coalGeneratorBlockEntity) {
            nextType = DEVICE_GENERATOR;
            nextControlState = coalGeneratorBlockEntity.isGeneratorEnabled() ? 1 : 0;
            nextPowerFlow = coalGeneratorBlockEntity.getCurrentOutputPerTick();
            nextEnergy = coalGeneratorBlockEntity.getCurrentOutputPerTick();
            nextCapacity = coalGeneratorBlockEntity.getMaxOutputPerTick();
            nextPrimaryMetric = coalGeneratorBlockEntity.getBurnTime();
            nextSecondaryMetric = coalGeneratorBlockEntity.getMaxBurnTime();
            nextOnline = true;
        } else if (blockEntity instanceof SolarPanelBlockEntity solarPanelBlockEntity) {
            nextType = DEVICE_SOLAR;
            nextControlState = solarPanelBlockEntity.isPanelEnabled() ? 1 : 0;
            nextPowerFlow = solarPanelBlockEntity.getReportedPowerFlow();
            nextEnergy = solarPanelBlockEntity.getStoredEnergy();
            nextCapacity = solarPanelBlockEntity.getEnergyCapacity();
            nextStateOfCharge = calculateStateOfCharge(nextEnergy, nextCapacity);
            nextPrimaryMetric = solarPanelBlockEntity.getOverallEfficiencyPercent();
            nextSecondaryMetric = solarPanelBlockEntity.getSunFactorPercent();
            nextTertiaryMetric = solarPanelBlockEntity.getHeightFactorPercent();
            nextQuaternaryMetric = Math.min(solarPanelBlockEntity.getWeatherFactorPercent(), solarPanelBlockEntity.getSkyFactorPercent());
            nextOnline = true;
        }

        if (slotDeviceTypes[slot] != nextType
                || slotControlStates[slot] != nextControlState
                || slotPowerFlows[slot] != nextPowerFlow
                || slotStoredEnergy[slot] != nextEnergy
                || slotCapacity[slot] != nextCapacity
                || slotStateOfCharge[slot] != nextStateOfCharge
                || slotPrimaryMetric[slot] != nextPrimaryMetric
                || slotSecondaryMetric[slot] != nextSecondaryMetric
                || slotTertiaryMetric[slot] != nextTertiaryMetric
                || slotQuaternaryMetric[slot] != nextQuaternaryMetric
                || slotOnline[slot] != nextOnline) {
            slotDeviceTypes[slot] = nextType;
            slotControlStates[slot] = nextControlState;
            slotPowerFlows[slot] = nextPowerFlow;
            slotStoredEnergy[slot] = nextEnergy;
            slotCapacity[slot] = nextCapacity;
            slotStateOfCharge[slot] = nextStateOfCharge;
            slotPrimaryMetric[slot] = nextPrimaryMetric;
            slotSecondaryMetric[slot] = nextSecondaryMetric;
            slotTertiaryMetric[slot] = nextTertiaryMetric;
            slotQuaternaryMetric[slot] = nextQuaternaryMetric;
            slotOnline[slot] = nextOnline;
            return true;
        }

        return false;
    }

    private boolean syncLegacySelectedSnapshot() {
        int nextType = getSlotDeviceType(selectedDeviceSlot);
        int nextControlState = getSlotControlState(selectedDeviceSlot);
        int nextEnergy = switch (nextType) {
            case DEVICE_GENERATOR -> getSlotPowerFlow(selectedDeviceSlot);
            case DEVICE_SOLAR -> getSlotPowerFlow(selectedDeviceSlot);
            default -> getSlotStoredEnergy(selectedDeviceSlot);
        };
        int nextCapacity = switch (nextType) {
            case DEVICE_GENERATOR -> getSlotCapacity(selectedDeviceSlot);
            case DEVICE_SOLAR -> getSlotCapacity(selectedDeviceSlot);
            default -> getSlotCapacity(selectedDeviceSlot);
        };
        boolean nextOnline = isSlotOnline(selectedDeviceSlot);

        if (attachedDeviceType != nextType || trackedControlState != nextControlState
                || trackedEnergy != nextEnergy || trackedCapacity != nextCapacity || trackedDeviceOnline != nextOnline) {
            attachedDeviceType = nextType;
            trackedControlState = nextControlState;
            trackedEnergy = nextEnergy;
            trackedCapacity = nextCapacity;
            trackedDeviceOnline = nextOnline;
            return true;
        }

        return false;
    }

    private int calculateStateOfCharge(int energy, int capacity) {
        if (capacity <= 0) {
            return 0;
        }

        return Math.min(100, energy * 100 / capacity);
    }

    public String describeStatus() {
        return String.format("J-Box slots=%d localIp=%s selected=%d",
                getRegisteredDeviceCount(), getIpAddressText(), selectedDeviceSlot + 1);
    }

    @Override
    public int getIpAddress() {
        return ipAddress;
    }

    @Override
    public void setIpAddress(int ipAddress) {
        if (this.ipAddress == ipAddress) {
            return;
        }

        this.ipAddress = ipAddress;
        markDirtyAndSync();
    }

    @Override
    public long getPlacementOrder() {
        return placementOrder;
    }

    public int getPendingDeviceIp() {
        return pendingDeviceIp;
    }

    public void setPendingDeviceIp(int pendingDeviceIp) {
        if (this.pendingDeviceIp == pendingDeviceIp) {
            return;
        }

        this.pendingDeviceIp = pendingDeviceIp;
        markDirtyAndSync();
    }

    public void adjustPendingDeviceIpOctet(int octetIndex, int delta) {
        setPendingDeviceIp(DeviceAddress.adjustOctet(pendingDeviceIp, octetIndex, delta));
    }

    public int getSelectedDeviceSlot() {
        return selectedDeviceSlot;
    }

    public void selectPreviousDeviceSlot() {
        selectedDeviceSlot = (selectedDeviceSlot + MAX_CONNECTED_DEVICES - 1) % MAX_CONNECTED_DEVICES;
        if (syncLegacySelectedSnapshot()) {
            markDirtyAndSync();
        }
    }

    public void selectNextDeviceSlot() {
        selectedDeviceSlot = (selectedDeviceSlot + 1) % MAX_CONNECTED_DEVICES;
        if (syncLegacySelectedSnapshot()) {
            markDirtyAndSync();
        }
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

    public int getSlotIp(int slot) {
        return isValidSlot(slot) ? registeredDeviceIps[slot] : 0;
    }

    public void setSlotIp(int slot, int ipAddress) {
        if (!isValidSlot(slot) || registeredDeviceIps[slot] == ipAddress) {
            return;
        }

        registeredDeviceIps[slot] = ipAddress;
        if (level != null) {
            syncSlotSnapshot(level, worldPosition, slot);
            syncLegacySelectedSnapshot();
        }
        markDirtyAndSync();
    }

    public int getSlotDeviceType(int slot) {
        return isValidSlot(slot) ? slotDeviceTypes[slot] : DEVICE_NONE;
    }

    public int getSlotControlState(int slot) {
        return isValidSlot(slot) ? slotControlStates[slot] : 0;
    }

    public int getSlotPowerFlow(int slot) {
        return isValidSlot(slot) ? slotPowerFlows[slot] : 0;
    }

    public int getSlotStoredEnergy(int slot) {
        return isValidSlot(slot) ? slotStoredEnergy[slot] : 0;
    }

    public int getSlotCapacity(int slot) {
        return isValidSlot(slot) ? slotCapacity[slot] : 0;
    }

    public int getSlotStateOfCharge(int slot) {
        return isValidSlot(slot) ? slotStateOfCharge[slot] : 0;
    }

    public int getSlotPrimaryMetric(int slot) {
        return isValidSlot(slot) ? slotPrimaryMetric[slot] : 0;
    }

    public int getSlotSecondaryMetric(int slot) {
        return isValidSlot(slot) ? slotSecondaryMetric[slot] : 0;
    }

    public int getSlotTertiaryMetric(int slot) {
        return isValidSlot(slot) ? slotTertiaryMetric[slot] : 0;
    }

    public int getSlotQuaternaryMetric(int slot) {
        return isValidSlot(slot) ? slotQuaternaryMetric[slot] : 0;
    }

    public boolean isSlotOnline(int slot) {
        return isValidSlot(slot) && slotOnline[slot];
    }

    public boolean addPendingDevice() {
        if (pendingDeviceIp == 0 || isRegisteredIp(pendingDeviceIp)) {
            return false;
        }

        for (int slot = 0; slot < registeredDeviceIps.length; slot++) {
            if (registeredDeviceIps[slot] == 0) {
                setSlotIp(slot, pendingDeviceIp);
                selectedDeviceSlot = slot;
                syncLegacySelectedSnapshot();
                markDirtyAndSync();
                return true;
            }
        }

        return false;
    }

    public boolean removeSelectedDevice() {
        return removeSlot(selectedDeviceSlot);
    }

    public boolean removeSlot(int slot) {
        if (!isValidSlot(slot) || registeredDeviceIps[slot] == 0) {
            return false;
        }

        registeredDeviceIps[slot] = 0;
        clearSlotSnapshot(slot);
        syncLegacySelectedSnapshot();
        markDirtyAndSync();
        return true;
    }

    public boolean controlAttachedDevice() {
        return controlSlot(selectedDeviceSlot);
    }

    public boolean controlSlot(int slot) {
        if (level == null) {
            return false;
        }

        BlockEntity blockEntity = getConnectedDeviceForSlot(level, worldPosition, slot);
        if (blockEntity instanceof BasicBatteryBlockEntity basicBatteryBlockEntity) {
            basicBatteryBlockEntity.cycleChargeMode();
            if (syncSlotSnapshot(level, worldPosition, slot) | syncLegacySelectedSnapshot()) {
                markDirtyAndSync();
            }
            return true;
        }

        if (blockEntity instanceof CoalGeneratorBlockEntity coalGeneratorBlockEntity) {
            coalGeneratorBlockEntity.toggleGeneratorEnabled();
            if (syncSlotSnapshot(level, worldPosition, slot) | syncLegacySelectedSnapshot()) {
                markDirtyAndSync();
            }
            return true;
        }

        if (blockEntity instanceof SolarPanelBlockEntity solarPanelBlockEntity) {
            solarPanelBlockEntity.togglePanelEnabled();
            if (syncSlotSnapshot(level, worldPosition, slot) | syncLegacySelectedSnapshot()) {
                markDirtyAndSync();
            }
            return true;
        }

        return false;
    }

    private @Nullable BlockEntity getConnectedDeviceForSlot(Level level, BlockPos pos, int slot) {
        int selectedIp = getSlotIp(slot);
        if (selectedIp == 0) {
            return null;
        }

        return Rs485NetworkHelper.findConnectedControllableDeviceByIp(level, pos, selectedIp, this);
    }

    private void clearSlotSnapshot(int slot) {
        slotDeviceTypes[slot] = DEVICE_NONE;
        slotControlStates[slot] = 0;
        slotPowerFlows[slot] = 0;
        slotStoredEnergy[slot] = 0;
        slotCapacity[slot] = 0;
        slotStateOfCharge[slot] = 0;
        slotPrimaryMetric[slot] = 0;
        slotSecondaryMetric[slot] = 0;
        slotTertiaryMetric[slot] = 0;
        slotQuaternaryMetric[slot] = 0;
        slotOnline[slot] = false;
    }

    private boolean isRegisteredIp(int ipAddress) {
        for (int registeredDeviceIp : registeredDeviceIps) {
            if (registeredDeviceIp == ipAddress) {
                return true;
            }
        }

        return false;
    }

    private boolean isValidSlot(int slot) {
        return slot >= 0 && slot < MAX_CONNECTED_DEVICES;
    }

    private void markDirtyAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
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
        tag.putIntArray("slotDeviceTypes", slotDeviceTypes);
        tag.putIntArray("slotControlStates", slotControlStates);
        tag.putIntArray("slotPowerFlows", slotPowerFlows);
        tag.putIntArray("slotStoredEnergy", slotStoredEnergy);
        tag.putIntArray("slotCapacity", slotCapacity);
        tag.putIntArray("slotStateOfCharge", slotStateOfCharge);
        tag.putIntArray("slotPrimaryMetric", slotPrimaryMetric);
        tag.putIntArray("slotSecondaryMetric", slotSecondaryMetric);
        tag.putIntArray("slotTertiaryMetric", slotTertiaryMetric);
        tag.putIntArray("slotQuaternaryMetric", slotQuaternaryMetric);
        tag.putIntArray("slotOnline", encodeBooleanArray(slotOnline));
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

        copyIntoFixedArray(tag.getIntArray("registeredDeviceIps"), registeredDeviceIps);
        copyIntoFixedArray(tag.getIntArray("slotDeviceTypes"), slotDeviceTypes);
        copyIntoFixedArray(tag.getIntArray("slotControlStates"), slotControlStates);
        copyIntoFixedArray(tag.getIntArray("slotPowerFlows"), slotPowerFlows);
        copyIntoFixedArray(tag.getIntArray("slotStoredEnergy"), slotStoredEnergy);
        copyIntoFixedArray(tag.getIntArray("slotCapacity"), slotCapacity);
        copyIntoFixedArray(tag.getIntArray("slotStateOfCharge"), slotStateOfCharge);
        copyIntoFixedArray(tag.getIntArray("slotPrimaryMetric"), slotPrimaryMetric);
        copyIntoFixedArray(tag.getIntArray("slotSecondaryMetric"), slotSecondaryMetric);
        copyIntoFixedArray(tag.getIntArray("slotTertiaryMetric"), slotTertiaryMetric);
        copyIntoFixedArray(tag.getIntArray("slotQuaternaryMetric"), slotQuaternaryMetric);
        decodeBooleanArray(tag.getIntArray("slotOnline"), slotOnline);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void copyIntoFixedArray(int[] source, int[] target) {
        for (int i = 0; i < target.length; i++) {
            target[i] = i < source.length ? source[i] : 0;
        }
    }

    private int[] encodeBooleanArray(boolean[] source) {
        int[] encoded = new int[source.length];
        for (int i = 0; i < source.length; i++) {
            encoded[i] = source[i] ? 1 : 0;
        }
        return encoded;
    }

    private void decodeBooleanArray(int[] source, boolean[] target) {
        for (int i = 0; i < target.length; i++) {
            target[i] = i < source.length && source[i] != 0;
        }
    }
}