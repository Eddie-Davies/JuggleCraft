package com.eddie.jugglepower.block.entity;

import com.eddie.jugglepower.power.PowerNetworkHelper;
import com.eddie.jugglepower.power.PowerStateDisplayProvider;
import com.eddie.jugglepower.power.PowerTier;
import com.eddie.jugglepower.power.TieredEnergyStorage;
import com.eddie.jugglepower.power.TieredPowerNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public abstract class AbstractTieredPowerBlockEntity extends BlockEntity implements TieredPowerNode, PowerStateDisplayProvider {
    private final PowerTier powerTier;
    protected final TieredEnergyStorage energyStorage;
    private LazyOptional<IEnergyStorage> energyOptional = LazyOptional.empty();
    private long throughputTick = Long.MIN_VALUE;
    private int currentInputThroughput;
    private int currentOutputThroughput;
    private final long[] lastReceivedTickByDirection = new long[Direction.values().length];

    protected AbstractTieredPowerBlockEntity(BlockEntityType<?> type,
                                             BlockPos pos,
                                             BlockState state,
                                             PowerTier powerTier,
                                             int capacity,
                                             int maxReceive,
                                             int maxExtract) {
        super(type, pos, state);
        this.powerTier = powerTier;
        this.energyStorage = new TieredEnergyStorage(capacity, maxReceive, maxExtract) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
        Arrays.fill(lastReceivedTickByDirection, Long.MIN_VALUE);
        this.energyOptional = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public PowerTier getPowerTier() {
        return powerTier;
    }

    @Override
    public IEnergyStorage getTieredEnergyStorage() {
        return energyStorage;
    }

    public int getStoredEnergy() {
        return energyStorage.getEnergyStored();
    }

    public int getEnergyCapacity() {
        return energyStorage.getMaxEnergyStored();
    }

    public int getCurrentInputThroughput() {
        return currentInputThroughput;
    }

    public int getCurrentOutputThroughput() {
        return currentOutputThroughput;
    }

    public int getCurrentTotalThroughput() {
        return currentInputThroughput + currentOutputThroughput;
    }

    protected double getEnergyFillPercent() {
        if (getEnergyCapacity() <= 0) {
            return 0.0;
        }

        return getStoredEnergy() * 100.0 / getEnergyCapacity();
    }

    protected static String formatPercent(double percent) {
        return String.format(Locale.ROOT, "%.1f%%", percent);
    }

    protected int pushToMatchedNeighbors(int maxTransfer) {
        return pushToMatchedNeighbors(maxTransfer, blockEntity -> true);
    }

    protected int pushToMatchedNeighbors(int maxTransfer, Predicate<BlockEntity> neighborFilter) {
        if (level == null || level.isClientSide || maxTransfer <= 0) {
            return 0;
        }

        return PowerNetworkHelper.pushToTierMatchedNeighbors(
                level,
                worldPosition,
                this,
                maxTransfer,
                (neighbor, direction) -> (neighborFilter == null || neighborFilter.test(neighbor)) && canTransferToDirection(direction)
        );
    }

    protected boolean canTransferToDirection(Direction direction) {
        if (level == null || direction == null) {
            return true;
        }

        long now = level.getGameTime();
        long lastReceivedTick = lastReceivedTickByDirection[direction.ordinal()];
        return lastReceivedTick < now - 1;
    }

    protected boolean beginThroughputTick(Level level) {
        if (level == null || level.isClientSide) {
            return false;
        }

        long gameTime = level.getGameTime();
        if (throughputTick == gameTime) {
            return false;
        }

        boolean hadThroughput = currentInputThroughput > 0 || currentOutputThroughput > 0;
        throughputTick = gameTime;
        currentInputThroughput = 0;
        currentOutputThroughput = 0;
        return hadThroughput;
    }

    @Override
    public void onPowerReceived(int amount) {
        onPowerReceived(null, amount);
    }

    @Override
    public void onPowerReceived(@Nullable Direction fromDirection, int amount) {
        if (amount <= 0 || level == null || level.isClientSide) {
            return;
        }

        alignThroughputToCurrentTick();
        currentInputThroughput += amount;
        if (fromDirection != null) {
            lastReceivedTickByDirection[fromDirection.ordinal()] = level.getGameTime();
        }
        setChanged();
    }

    @Override
    public void onPowerSent(int amount) {
        onPowerSent(null, amount);
    }

    @Override
    public void onPowerSent(@Nullable Direction toDirection, int amount) {
        if (amount <= 0 || level == null || level.isClientSide) {
            return;
        }

        alignThroughputToCurrentTick();
        currentOutputThroughput += amount;
    }

    private void alignThroughputToCurrentTick() {
        if (level == null || level.isClientSide) {
            return;
        }

        long gameTime = level.getGameTime();
        if (throughputTick != gameTime) {
            throughputTick = gameTime;
            currentInputThroughput = 0;
            currentOutputThroughput = 0;
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putInt("throughputIn", currentInputThroughput);
        tag.putInt("throughputOut", currentOutputThroughput);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energyStorage.setStoredEnergy(tag.getInt("energy"));
        currentInputThroughput = tag.getInt("throughputIn");
        currentOutputThroughput = tag.getInt("throughputOut");
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

    @Override
    public void appendPowerStateLines(List<Component> lines) {
        lines.add(Component.literal("Tier: " + getPowerTier().name()));
        lines.add(Component.literal("Energy: " + getStoredEnergy() + "/" + getEnergyCapacity() + " FE"));
        lines.add(Component.literal("Current FE/t: " + getCurrentTotalThroughput()
                + " (In " + getCurrentInputThroughput() + ", Out " + getCurrentOutputThroughput() + ")"));
        lines.add(Component.literal("Buffer Fill: " + formatPercent(getEnergyFillPercent())));
        lines.add(Component.literal(isTierFaulted() ? "Fault: active" : "Fault: clear"));
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) {
            return energyOptional.cast();
        }

        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyOptional.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energyOptional = LazyOptional.of(() -> energyStorage);
    }
}
