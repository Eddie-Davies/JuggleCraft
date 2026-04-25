package com.eddie.jugglepower.power;

import net.minecraft.util.Mth;
import net.minecraftforge.energy.EnergyStorage;

public class TieredEnergyStorage extends EnergyStorage {
    public TieredEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate) {
            onEnergyChanged();
        }

        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (extracted > 0 && !simulate) {
            onEnergyChanged();
        }

        return extracted;
    }

    public void setStoredEnergy(int energy) {
        this.energy = Mth.clamp(energy, 0, capacity);
        onEnergyChanged();
    }

    public int addEnergy(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int accepted = Math.min(capacity - energy, amount);
        if (accepted <= 0) {
            return 0;
        }

        energy += accepted;
        onEnergyChanged();
        return accepted;
    }

    protected void onEnergyChanged() {
    }
}
