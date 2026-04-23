package com.eddie.jugglecraft.block.entity;

import com.eddie.jugglecraft.menu.JboxMenu;
import com.eddie.jugglecraft.registry.ModBlockEntities;
import com.eddie.jugglecraft.util.MountedDeviceHelper;
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
import org.jetbrains.annotations.Nullable;

public class JboxBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity implements MenuProvider {
    private static final int SCAN_INTERVAL_TICKS = 20;
    public static final int DEVICE_NONE = 0;
    public static final int DEVICE_BATTERY = 1;
    public static final int DEVICE_GENERATOR = 2;

    private int attachedDeviceType = DEVICE_NONE;
    private int trackedControlState;
    private int trackedEnergy;
    private int trackedCapacity;

    public JboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.JBOX.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (level.getGameTime() % SCAN_INTERVAL_TICKS != 0) {
            return;
        }

        syncAttachedDevice(level, pos, state);
    }

    private void syncAttachedDevice(Level level, BlockPos pos, BlockState state) {
        int nextDeviceType = DEVICE_NONE;
        int nextControlState = 0;
        int nextEnergy = 0;
        int nextCapacity = 0;

        if (MountedDeviceHelper.getAttachedBlockEntity(level, pos, state) instanceof BasicBatteryBlockEntity basicBatteryBlockEntity) {
            nextDeviceType = DEVICE_BATTERY;
            nextControlState = basicBatteryBlockEntity.getChargeMode().ordinal();
            nextEnergy = basicBatteryBlockEntity.getStoredEnergy();
            nextCapacity = basicBatteryBlockEntity.getEnergyCapacity();
        } else if (MountedDeviceHelper.getAttachedBlockEntity(level, pos, state) instanceof CoalGeneratorBlockEntity coalGeneratorBlockEntity) {
            nextDeviceType = DEVICE_GENERATOR;
            nextControlState = coalGeneratorBlockEntity.isGeneratorEnabled() ? 1 : 0;
            nextEnergy = coalGeneratorBlockEntity.getStoredEnergy();
            nextCapacity = coalGeneratorBlockEntity.getEnergyCapacity();
        }

        if (attachedDeviceType != nextDeviceType || trackedControlState != nextControlState
                || trackedEnergy != nextEnergy || trackedCapacity != nextCapacity) {
            attachedDeviceType = nextDeviceType;
            trackedControlState = nextControlState;
            trackedEnergy = nextEnergy;
            trackedCapacity = nextCapacity;
            setChanged();
        }
    }

    public String describeStatus() {
        return String.format("J-Box target=%d control=%d energy=%d/%d FE",
                attachedDeviceType, trackedControlState, trackedEnergy, trackedCapacity);
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

    public boolean controlAttachedDevice() {
        if (level == null) {
            return false;
        }

        BlockState state = getBlockState();
        if (MountedDeviceHelper.getAttachedBlockEntity(level, worldPosition, state) instanceof BasicBatteryBlockEntity basicBatteryBlockEntity) {
            basicBatteryBlockEntity.cycleChargeMode();
            syncAttachedDevice(level, worldPosition, state);
            return true;
        }

        if (MountedDeviceHelper.getAttachedBlockEntity(level, worldPosition, state) instanceof CoalGeneratorBlockEntity coalGeneratorBlockEntity) {
            coalGeneratorBlockEntity.toggleGeneratorEnabled();
            syncAttachedDevice(level, worldPosition, state);
            return true;
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
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        attachedDeviceType = tag.getInt("attachedDeviceType");
        trackedControlState = tag.getInt("trackedControlState");
        trackedEnergy = tag.getInt("trackedEnergy");
        trackedCapacity = tag.getInt("trackedCapacity");
    }
}