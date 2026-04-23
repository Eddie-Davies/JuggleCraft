package com.eddie.jugglecraft;

import com.eddie.jugglecraft.registry.ModBlocks;
import com.eddie.jugglecraft.registry.ModBlockEntities;
import com.eddie.jugglecraft.registry.ModCreativeTabs;
import com.eddie.jugglecraft.registry.ModItems;
import com.eddie.jugglecraft.registry.ModMenuTypes;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(JuggleCraft.MODID)
public class JuggleCraft {
    public static final String MODID = "jugglecraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    public JuggleCraft() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }
}