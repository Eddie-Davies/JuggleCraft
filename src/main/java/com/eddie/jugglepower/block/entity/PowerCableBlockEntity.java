package com.eddie.jugglepower.block.entity;

import com.eddie.jugglepower.block.custom.PowerCableBlock;
import com.eddie.jugglepower.power.PowerTier;
import com.eddie.jugglepower.power.TieredPowerNode;
import com.eddie.jugglepower.registry.PowerBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PowerCableBlockEntity extends AbstractTieredPowerBlockEntity {
    private static final int FAULT_DURATION_TICKS = 60;

    private int faultTicks;

    public PowerCableBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, resolveTier(state));
    }

    private PowerCableBlockEntity(BlockPos pos, BlockState state, PowerTier tier) {
        super(PowerBlockEntities.POWER_CABLE.get(), pos, state, tier,
                getCapacityForTier(tier), tier.getDefaultMaxTransfer(), tier.getDefaultMaxTransfer());
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        boolean changed = beginThroughputTick(level);

        if (faultTicks > 0) {
            faultTicks--;
            changed = true;
        }

        if (faultTicks == 0 && getStoredEnergy() > 0) {
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
            faultTicks = FAULT_DURATION_TICKS;
            setChanged();
        }
    }

    @Override
    public boolean isTierFaulted() {
        return faultTicks > 0;
    }

    public void clearFault() {
        if (faultTicks != 0) {
            faultTicks = 0;
            setChanged();
        }
    }

    public int getFaultTicks() {
        return faultTicks;
    }

    @Override
    public boolean canRelayPower() {
        return faultTicks == 0;
    }

    @Override
    public void appendPowerStateLines(List<Component> lines) {
        super.appendPowerStateLines(lines);
        lines.add(Component.literal("Cable fault ticks: " + faultTicks));
        lines.add(Component.literal("Throughput Limit: " + getPowerTier().getDefaultMaxTransfer() + " FE/t"));
        lines.add(Component.literal("Connected Tier Links: " + countConnectedTierLinks()));
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

    private static int getCapacityForTier(PowerTier tier) {
        return switch (tier) {
            case HV -> 10000;
            case LV -> 6000;
            case DC -> 4000;
            default -> 1000;
        };
    }

    private static PowerTier resolveTier(BlockState state) {
        if (state.getBlock() instanceof PowerCableBlock powerCableBlock) {
            return powerCableBlock.getTier();
        }

        return PowerTier.LV;
    }

    private int countConnectedTierLinks() {
        if (level == null) {
            return 0;
        }

        int count = 0;
        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor instanceof TieredPowerNode tieredPowerNode
                    && getPowerTier().isCompatibleWith(tieredPowerNode.getPowerTier())) {
                count++;
            }
        }

        return count;
    }
}
