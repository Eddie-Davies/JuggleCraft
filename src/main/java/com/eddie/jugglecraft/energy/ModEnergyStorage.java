package com.eddie.jugglecraft.energy;

import net.minecraft.util.Mth;
import net.minecraftforge.energy.EnergyStorage;

public class ModEnergyStorage extends EnergyStorage {
    public ModEnergyStorage(int capacity, int maxReceive, int maxExtract) {
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

    public void addEnergy(int energyToAdd) {
        setStoredEnergy(this.energy + energyToAdd);
    }

    protected void onEnergyChanged() {
    }
}