package com.eddie.jugglepower.power;

public enum PowerTier {
    HV(600),
    LV(240),
    DC(120),
    SIGNAL(0);

    private final int defaultMaxTransfer;

    PowerTier(int defaultMaxTransfer) {
        this.defaultMaxTransfer = defaultMaxTransfer;
    }

    public int getDefaultMaxTransfer() {
        return defaultMaxTransfer;
    }

    public boolean isCompatibleWith(PowerTier other) {
        return this == other;
    }
}
