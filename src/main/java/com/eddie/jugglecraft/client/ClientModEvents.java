package com.eddie.jugglecraft.client;

import com.eddie.jugglecraft.client.screen.BasicBatteryScreen;
import com.eddie.jugglecraft.JuggleCraft;
import com.eddie.jugglecraft.client.screen.CoalGeneratorScreen;
import com.eddie.jugglecraft.client.screen.JboxScreen;
import com.eddie.jugglecraft.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = JuggleCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.BASIC_BATTERY.get(), BasicBatteryScreen::new);
            MenuScreens.register(ModMenuTypes.COAL_GENERATOR.get(), CoalGeneratorScreen::new);
            MenuScreens.register(ModMenuTypes.JBOX.get(), JboxScreen::new);
        });
    }
}