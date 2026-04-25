package com.eddie.jugglepower.power;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface PowerStateDisplayProvider {
    void appendPowerStateLines(List<Component> lines);
}
