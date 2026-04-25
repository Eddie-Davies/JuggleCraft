package com.eddie.jugglepower.block.entity;

import com.eddie.jugglepower.power.PowerTier;
import com.eddie.jugglepower.registry.PowerBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PvCellBlockEntity extends AbstractTieredPowerBlockEntity {
    private static final int CAPACITY = 24000;
    private static final int MAX_GENERATION = 100;

    private boolean enabled = true;
    private int currentGeneration;
    private int faultTicks;

    public PvCellBlockEntity(BlockPos pos, BlockState state) {
        super(PowerBlockEntities.PV_CELL.get(), pos, state, PowerTier.DC,
                CAPACITY, 0, PowerTier.DC.getDefaultMaxTransfer());
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean changed = false;

        int nextGeneration = enabled ? calculateGeneration(level, pos) : 0;
        if (nextGeneration != currentGeneration) {
            currentGeneration = nextGeneration;
            changed = true;
        }

        if (currentGeneration > 0) {
            int before = getStoredEnergy();
            energyStorage.addEnergy(currentGeneration);
            changed |= before != getStoredEnergy();
        }

        if (faultTicks > 0) {
            faultTicks--;
            changed = true;
        } else if (getStoredEnergy() > 0) {
            int moved = pushToMatchedNeighbors(Math.min(getPowerTier().getDefaultMaxTransfer(), getStoredEnergy()));
            if (moved > 0) {
                changed = true;
            }
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

    public int getCurrentGeneration() {
        return currentGeneration;
    }

    @Override
    public void appendPowerStateLines(List<Component> lines) {
        super.appendPowerStateLines(lines);
        lines.add(Component.literal("State: " + (enabled ? "ON" : "OFF")));
        lines.add(Component.literal("Generation: " + currentGeneration + " FE/t"));
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
        tag.putBoolean("enabled", enabled);
        tag.putInt("currentGeneration", currentGeneration);
        tag.putInt("faultTicks", faultTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        enabled = !tag.contains("enabled") || tag.getBoolean("enabled");
        currentGeneration = tag.getInt("currentGeneration");
        faultTicks = tag.getInt("faultTicks");
    }

    private int calculateGeneration(Level level, BlockPos pos) {
        if (!level.dimensionType().hasSkyLight() || !level.canSeeSky(pos.above())) {
            return 0;
        }

        long dayTime = level.getDayTime() % 24000L;
        if (dayTime < 0L) {
            dayTime += 24000L;
        }

        if (dayTime >= 12000L) {
            return 0;
        }

        float sunFactor = 1.0f - Math.abs(dayTime - 6000L) / 6000.0f;
        float weatherFactor = level.isThundering() ? 0.25f : level.isRaining() ? 0.55f : 1.0f;
        float skyLightFactor = Mth.clamp(level.getBrightness(LightLayer.SKY, pos.above()) / 15.0f, 0.0f, 1.0f);

        int seaLevel = level.getSeaLevel();
        int top = level.getMaxBuildHeight() - 1;
        float heightNorm = top <= seaLevel ? 0.0f : Mth.clamp((pos.getY() - seaLevel) / (float) (top - seaLevel), 0.0f, 1.0f);
        float heightFactor = 0.8f + heightNorm * 0.4f;

        return Math.round(MAX_GENERATION * sunFactor * weatherFactor * skyLightFactor * heightFactor);
    }
}
