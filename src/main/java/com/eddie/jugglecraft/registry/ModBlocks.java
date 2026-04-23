package com.eddie.jugglecraft.registry;

import com.eddie.jugglecraft.JuggleCraft;
import com.eddie.jugglecraft.block.custom.BasicBatteryBlock;
import com.eddie.jugglecraft.block.custom.CoalGeneratorBlock;
import com.eddie.jugglecraft.block.custom.JboxBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, JuggleCraft.MODID);

    public static final RegistryObject<Block> JBOX = BLOCKS.register("jbox", JboxBlock::new);
    public static final RegistryObject<Block> COAL_GENERATOR = BLOCKS.register("coal_generator", CoalGeneratorBlock::new);
    public static final RegistryObject<Block> BASIC_BATTERY = BLOCKS.register("basic_battery", BasicBatteryBlock::new);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}