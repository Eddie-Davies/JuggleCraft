package com.eddie.jugglepower.power;

import net.minecraft.core.Direction;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public interface TieredPowerNode {
    PowerTier getPowerTier();

    IEnergyStorage getTieredEnergyStorage();

    default boolean canRelayPower() {
        return false;
    }

    default void onPowerReceived(int amount) {
    }

    default void onPowerReceived(@Nullable Direction fromDirection, int amount) {
        onPowerReceived(amount);
    }

    default void onPowerSent(int amount) {
    }

    default void onPowerSent(@Nullable Direction toDirection, int amount) {
        onPowerSent(amount);
    }

    default void markTierFault(@Nullable PowerTier neighborTier) {
    }

    default boolean isTierFaulted() {
        return false;
    }
}
