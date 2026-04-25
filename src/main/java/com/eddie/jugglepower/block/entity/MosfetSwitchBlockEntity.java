package com.eddie.jugglepower.block.entity;

import com.eddie.jugglepower.block.custom.MosfetSwitchBlock;
import com.eddie.jugglepower.power.PowerTier;
import com.eddie.jugglepower.registry.PowerBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MosfetSwitchBlockEntity extends AbstractTieredPowerBlockEntity {
    private static final int CAPACITY = 8000;

    private int faultTicks;

    public MosfetSwitchBlockEntity(BlockPos pos, BlockState state) {
        super(PowerBlockEntities.MOSFET_SWITCH.get(), pos, state, PowerTier.LV,
                CAPACITY, PowerTier.LV.getDefaultMaxTransfer(), PowerTier.LV.getDefaultMaxTransfer());
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean changed = false;

        if (faultTicks > 0) {
            faultTicks--;
            changed = true;
        }

        boolean enabled = state.getValue(MosfetSwitchBlock.ENABLED);
        if (enabled && faultTicks == 0 && getStoredEnergy() > 0) {
            int moved = pushToMatchedNeighbors(Math.min(getPowerTier().getDefaultMaxTransfer(), getStoredEnergy()));
            if (moved > 0) {
                changed = true;
            }
        }

        if (changed) {
            setChanged();
        }
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
        tag.putInt("faultTicks", faultTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        faultTicks = tag.getInt("faultTicks");
    }
}
