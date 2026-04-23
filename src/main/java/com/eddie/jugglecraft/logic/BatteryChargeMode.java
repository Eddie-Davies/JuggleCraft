package com.eddie.jugglecraft.logic;

import net.minecraft.network.chat.Component;

public enum BatteryChargeMode {
    IDLE("state.jugglecraft.battery.idle"),
    CHARGING("state.jugglecraft.battery.charging"),
    DISCHARGING("state.jugglecraft.battery.discharging");

    private final String translationKey;

    BatteryChargeMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }

    public BatteryChargeMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public static BatteryChargeMode byId(int id) {
        BatteryChargeMode[] values = values();
        if (id < 0 || id >= values.length) {
            return CHARGING;
        }

        return values[id];
    }
}