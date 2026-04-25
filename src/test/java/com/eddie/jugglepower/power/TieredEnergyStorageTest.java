package com.eddie.jugglepower.power;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TieredEnergyStorageTest {

    @Test
    void addEnergyBypassesExternalReceiveLimit() {
        TieredEnergyStorage storage = new TieredEnergyStorage(1000, 0, 120);

        int received = storage.receiveEnergy(100, false);
        int added = storage.addEnergy(100);

        assertEquals(0, received);
        assertEquals(100, added);
        assertEquals(100, storage.getEnergyStored());
    }

    @Test
    void addEnergyClampsAtCapacity() {
        TieredEnergyStorage storage = new TieredEnergyStorage(200, 0, 120);
        storage.setStoredEnergy(150);

        int added = storage.addEnergy(100);

        assertEquals(50, added);
        assertEquals(200, storage.getEnergyStored());
    }

    @Test
    void addEnergyIgnoresNonPositiveValues() {
        TieredEnergyStorage storage = new TieredEnergyStorage(500, 0, 120);

        int zeroAdded = storage.addEnergy(0);
        int negativeAdded = storage.addEnergy(-40);

        assertEquals(0, zeroAdded);
        assertEquals(0, negativeAdded);
        assertEquals(0, storage.getEnergyStored());
    }
}
