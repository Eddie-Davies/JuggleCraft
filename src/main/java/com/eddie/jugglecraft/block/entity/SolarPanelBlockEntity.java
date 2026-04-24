package com.eddie.jugglecraft.block.entity;

import com.eddie.jugglecraft.energy.EnergyTransferHelper;
import com.eddie.jugglecraft.logic.DeviceAddress;
import com.eddie.jugglecraft.logic.IpAddressableDevice;
import com.eddie.jugglecraft.logic.PlacementOrderedNode;
import com.eddie.jugglecraft.menu.SolarPanelMenu;
import com.eddie.jugglecraft.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SolarPanelBlockEntity extends AbstractEnergyBlockEntity implements MenuProvider, IpAddressableDevice, PlacementOrderedNode {
    private static final int CAPACITY = 12000;
    private static final int MAX_GENERATION_PER_TICK = 120;
    private static final int MAX_OUTPUT_PER_TICK = 120;

    private boolean panelEnabled = true;
    private int ipAddress = DeviceAddress.DEVICE_DEFAULT;
    private long placementOrder = -1L;
    private int currentGeneration;
    private int sunFactorPercent;
    private int heightFactorPercent = 100;
    private int weatherFactorPercent = 100;
    private int skyFactorPercent;

    public SolarPanelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_PANEL.get(), pos, state, CAPACITY, 0, MAX_OUTPUT_PER_TICK);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        ensurePlacementOrder(level);

        int previousGeneration = currentGeneration;
        int previousSunFactor = sunFactorPercent;
        int previousHeightFactor = heightFactorPercent;
        int previousWeatherFactor = weatherFactorPercent;
        int previousSkyFactor = skyFactorPercent;

        currentGeneration = calculateGeneration(level, pos);
        boolean changed = currentGeneration != previousGeneration
                || sunFactorPercent != previousSunFactor
                || heightFactorPercent != previousHeightFactor
                || weatherFactorPercent != previousWeatherFactor
                || skyFactorPercent != previousSkyFactor;

        if (panelEnabled && currentGeneration > 0) {
            int previousEnergy = energyStorage.getEnergyStored();
            energyStorage.receiveEnergy(currentGeneration, false);
            changed |= energyStorage.getEnergyStored() != previousEnergy;
        }

        if (energyStorage.getEnergyStored() > 0) {
            changed |= EnergyTransferHelper.pushEnergyToNeighbors(level, pos, energyStorage, MAX_OUTPUT_PER_TICK) > 0;
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

    private int calculateGeneration(Level level, BlockPos pos) {
        if (!panelEnabled || !level.dimensionType().hasSkyLight()) {
            sunFactorPercent = 0;
            weatherFactorPercent = 100;
            skyFactorPercent = 0;
            heightFactorPercent = calculateHeightFactorPercent(level, pos);
            return 0;
        }

        BlockPos collectionPos = pos.above();
        if (!level.canSeeSky(collectionPos)) {
            sunFactorPercent = 0;
            weatherFactorPercent = level.isThundering() ? 20 : level.isRaining() ? 55 : 100;
            skyFactorPercent = 0;
            heightFactorPercent = calculateHeightFactorPercent(level, pos);
            return 0;
        }

        float sunFactor = calculateSunFactor(level);
        float weatherFactor = level.isThundering() ? 0.2f : level.isRaining() ? 0.55f : 1.0f;
        float skyFactor = Mth.clamp(level.getBrightness(LightLayer.SKY, collectionPos) / 15.0f, 0.0f, 1.0f);
        float heightFactor = calculateHeightFactor(level, pos);

        sunFactorPercent = Math.round(sunFactor * 100.0f);
        weatherFactorPercent = Math.round(weatherFactor * 100.0f);
        skyFactorPercent = Math.round(skyFactor * 100.0f);
        heightFactorPercent = Math.round(heightFactor * 100.0f);

        return Math.round(MAX_GENERATION_PER_TICK * sunFactor * weatherFactor * skyFactor * heightFactor);
    }

    private float calculateSunFactor(Level level) {
        long dayTime = level.getDayTime() % 24000L;
        if (dayTime < 0L) {
            dayTime += 24000L;
        }

        if (dayTime >= 12000L) {
            return 0.0f;
        }

        float normalized = 1.0f - Math.abs(dayTime - 6000L) / 6000.0f;
        return Mth.clamp(normalized, 0.0f, 1.0f);
    }

    private float calculateHeightFactor(Level level, BlockPos pos) {
        int seaLevel = level.getSeaLevel();
        int top = level.getMaxBuildHeight() - 1;
        if (top <= seaLevel) {
            return 1.0f;
        }

        float normalized = Mth.clamp((pos.getY() - seaLevel) / (float) (top - seaLevel), 0.0f, 1.0f);
        return 0.7f + normalized * 0.5f;
    }

    private int calculateHeightFactorPercent(Level level, BlockPos pos) {
        return Math.round(calculateHeightFactor(level, pos) * 100.0f);
    }

    public boolean isPanelEnabled() {
        return panelEnabled;
    }

    public void togglePanelEnabled() {
        panelEnabled = !panelEnabled;
        setChanged();
    }

    public int getCurrentGeneration() {
        return currentGeneration;
    }

    public int getMaxGenerationPerTick() {
        return MAX_GENERATION_PER_TICK;
    }

    public int getSunFactorPercent() {
        return sunFactorPercent;
    }

    public int getHeightFactorPercent() {
        return heightFactorPercent;
    }

    public int getWeatherFactorPercent() {
        return weatherFactorPercent;
    }

    public int getSkyFactorPercent() {
        return skyFactorPercent;
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

    public String describeStatus() {
        return String.format("Solar Panel enabled=%s output=%d/%d FE/t stored=%d/%d FE ip=%s",
                panelEnabled, currentGeneration, MAX_GENERATION_PER_TICK, getStoredEnergy(), getEnergyCapacity(), getIpAddressText());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.jugglecraft.solar_panel");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SolarPanelMenu(containerId, inventory, this, new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> getStoredEnergy();
                    case 1 -> getEnergyCapacity();
                    case 2 -> getCurrentGeneration();
                    case 3 -> getMaxGenerationPerTick();
                    case 4 -> isPanelEnabled() ? 1 : 0;
                    case 5 -> getIpAddress();
                    case 6 -> getSunFactorPercent();
                    case 7 -> getHeightFactorPercent();
                    case 8 -> getWeatherFactorPercent();
                    case 9 -> getSkyFactorPercent();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return SolarPanelMenu.DATA_COUNT;
            }
        });
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("panelEnabled", panelEnabled);
        tag.putInt("ipAddress", ipAddress);
        tag.putLong("placementOrder", placementOrder);
        tag.putInt("currentGeneration", currentGeneration);
        tag.putInt("sunFactorPercent", sunFactorPercent);
        tag.putInt("heightFactorPercent", heightFactorPercent);
        tag.putInt("weatherFactorPercent", weatherFactorPercent);
        tag.putInt("skyFactorPercent", skyFactorPercent);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        panelEnabled = !tag.contains("panelEnabled") || tag.getBoolean("panelEnabled");
        ipAddress = tag.contains("ipAddress") ? tag.getInt("ipAddress") : DeviceAddress.DEVICE_DEFAULT;
        placementOrder = tag.contains("placementOrder") ? tag.getLong("placementOrder") : -1L;
        currentGeneration = tag.getInt("currentGeneration");
        sunFactorPercent = tag.getInt("sunFactorPercent");
        heightFactorPercent = tag.contains("heightFactorPercent") ? tag.getInt("heightFactorPercent") : 100;
        weatherFactorPercent = tag.contains("weatherFactorPercent") ? tag.getInt("weatherFactorPercent") : 100;
        skyFactorPercent = tag.getInt("skyFactorPercent");
    }
}