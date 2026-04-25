package com.eddie.jugglepower.block.entity;

public enum BatteryCellMode {
    CHARGING,
    IDLE,
    DISCHARGING;

    public BatteryCellMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static BatteryCellMode byId(int id) {
        BatteryCellMode[] values = values();
        if (id < 0 || id >= values.length) {
            return CHARGING;
        }

        return values[id];
    }
}
