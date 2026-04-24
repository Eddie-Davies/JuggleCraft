package com.eddie.jugglecraft.logic;

import net.minecraft.util.Mth;

public final class DeviceAddress {
    public static final int OCTET_COUNT = 4;
    public static final int JBOX_DEFAULT = pack(10, 172, 152, 1);
    public static final int DEVICE_DEFAULT = pack(192, 168, 1, 1);

    private DeviceAddress() {
    }

    public static int pack(int first, int second, int third, int fourth) {
        return ((first & 0xFF) << 24)
                | ((second & 0xFF) << 16)
                | ((third & 0xFF) << 8)
                | (fourth & 0xFF);
    }

    public static int getOctet(int address, int octetIndex) {
        int shift = (OCTET_COUNT - 1 - Mth.clamp(octetIndex, 0, OCTET_COUNT - 1)) * 8;
        return address >>> shift & 0xFF;
    }

    public static int setOctet(int address, int octetIndex, int value) {
        int clampedIndex = Mth.clamp(octetIndex, 0, OCTET_COUNT - 1);
        int shift = (OCTET_COUNT - 1 - clampedIndex) * 8;
        int mask = 0xFF << shift;
        int clampedValue = Mth.clamp(value, 0, 255);
        return address & ~mask | (clampedValue & 0xFF) << shift;
    }

    public static int adjustOctet(int address, int octetIndex, int delta) {
        return setOctet(address, octetIndex, getOctet(address, octetIndex) + delta);
    }

    public static String toString(int address) {
        return getOctet(address, 0) + "."
                + getOctet(address, 1) + "."
                + getOctet(address, 2) + "."
                + getOctet(address, 3);
    }
}