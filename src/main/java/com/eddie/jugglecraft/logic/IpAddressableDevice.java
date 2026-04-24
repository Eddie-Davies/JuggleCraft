package com.eddie.jugglecraft.logic;

public interface IpAddressableDevice {
    int getIpAddress();

    void setIpAddress(int ipAddress);

    default void adjustIpOctet(int octetIndex, int delta) {
        setIpAddress(DeviceAddress.adjustOctet(getIpAddress(), octetIndex, delta));
    }

    default String getIpAddressText() {
        return DeviceAddress.toString(getIpAddress());
    }
}