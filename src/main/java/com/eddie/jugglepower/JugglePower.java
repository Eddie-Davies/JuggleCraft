package com.eddie.jugglepower;

import com.eddie.jugglepower.registry.PowerBlockEntities;
import com.eddie.jugglepower.registry.PowerBlocks;
import com.eddie.jugglepower.registry.PowerCreativeTabs;
import com.eddie.jugglepower.registry.PowerItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(JugglePower.MODID)
public class JugglePower {
    public static final String MODID = "jugglepower";
    public static final Logger LOGGER = LogUtils.getLogger();

    public JugglePower() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        PowerBlocks.register(modEventBus);
        PowerBlockEntities.register(modEventBus);
        PowerItems.register(modEventBus);
        PowerCreativeTabs.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }
}
