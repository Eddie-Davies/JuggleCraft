package com.eddie.jugglepower.power;

import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public interface TieredPowerNode {
    PowerTier getPowerTier();

    IEnergyStorage getTieredEnergyStorage();

    default void markTierFault(@Nullable PowerTier neighborTier) {
    }

    default boolean isTierFaulted() {
        return false;
    }
}
