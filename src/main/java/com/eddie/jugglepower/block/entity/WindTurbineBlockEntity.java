package com.eddie.jugglepower.block.entity;

import com.eddie.jugglepower.power.PowerTier;
import com.eddie.jugglepower.registry.PowerBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WindTurbineBlockEntity extends AbstractTieredPowerBlockEntity {
    private static final int CAPACITY = 48000;
    private static final int MAX_GENERATION = 220;

    private boolean enabled = true;
    private int currentGeneration;
    private int faultTicks;

    public WindTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(PowerBlockEntities.WIND_TURBINE.get(), pos, state, PowerTier.HV,
                CAPACITY, 0, PowerTier.HV.getDefaultMaxTransfer());
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean changed = beginThroughputTick(level);

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
        double generationPercent = MAX_GENERATION <= 0 ? 0.0 : currentGeneration * 100.0 / MAX_GENERATION;
        lines.add(Component.literal("State: " + (enabled ? "ON" : "OFF")));
        lines.add(Component.literal("Generation: " + currentGeneration + " FE/t"));
        lines.add(Component.literal("FE/t Peak: " + MAX_GENERATION));
        lines.add(Component.literal("Output Utilization: " + formatPercent(generationPercent)));
        lines.add(Component.literal("Line Limit: " + getPowerTier().getDefaultMaxTransfer() + " FE/t"));
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
        long gameTime = level.getGameTime();
        double cycle = (Math.sin((gameTime + (pos.asLong() & 1023L)) / 120.0D) + 1.0D) * 0.5D;

        int heightBonus = Math.max(0, pos.getY() - 64);
        float heightFactor = 1.0f + Math.min(1.5f, heightBonus / 80.0f);

        float weatherBoost = level.isThundering() ? 1.3f : level.isRaining() ? 1.1f : 1.0f;
        int generated = Math.round((40.0f + (float) cycle * 90.0f) * heightFactor * weatherBoost);

        return Mth.clamp(generated, 0, MAX_GENERATION);
    }
}
