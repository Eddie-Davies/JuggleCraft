package com.eddie.jugglecraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;

public final class ModInventoryHelper {
    private ModInventoryHelper() {
    }

    public static void dropContents(Level level, BlockPos pos, ItemStackHandler itemHandler) {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), itemHandler.getStackInSlot(slot));
        }
    }
}