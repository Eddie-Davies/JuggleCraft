package com.eddie.jugglepower.block.entity;

import com.eddie.jugglepower.power.PowerTier;
import com.eddie.jugglepower.registry.PowerBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BatteryCellBlockEntity extends AbstractTieredPowerBlockEntity {
    private static final int CAPACITY = 200000;

    private BatteryCellMode mode = BatteryCellMode.CHARGING;
    private int faultTicks;

    public BatteryCellBlockEntity(BlockPos pos, BlockState state) {
        super(PowerBlockEntities.BATTERY_CELL.get(), pos, state, PowerTier.DC,
                CAPACITY, PowerTier.DC.getDefaultMaxTransfer(), PowerTier.DC.getDefaultMaxTransfer());
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean changed = false;

        if (faultTicks > 0) {
            faultTicks--;
            changed = true;
        }

        if (faultTicks == 0 && mode == BatteryCellMode.DISCHARGING && getStoredEnergy() > 0) {
            int moved = pushToMatchedNeighbors(Math.min(getPowerTier().getDefaultMaxTransfer(), getStoredEnergy()));
            if (moved > 0) {
                changed = true;
            }
        }

        if (changed) {
            setChanged();
        }
    }

    public BatteryCellMode getMode() {
        return mode;
    }

    @Override
    public void appendPowerStateLines(List<Component> lines) {
        super.appendPowerStateLines(lines);
        lines.add(Component.literal("Mode: " + mode.name()));
    }

    public void cycleMode() {
        mode = mode.next();
        setChanged();
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
        tag.putInt("mode", mode.ordinal());
        tag.putInt("faultTicks", faultTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mode = BatteryCellMode.byId(tag.getInt("mode"));
        faultTicks = tag.getInt("faultTicks");
    }
}
