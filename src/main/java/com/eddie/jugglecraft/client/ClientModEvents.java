package com.eddie.jugglecraft.client;

import com.eddie.jugglecraft.client.screen.BasicBatteryScreen;
import com.eddie.jugglecraft.JuggleCraft;
import com.eddie.jugglecraft.client.screen.CoalGeneratorScreen;
import com.eddie.jugglecraft.client.screen.JboxScreen;
import com.eddie.jugglecraft.registry.ModMenuTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterMenuScreensEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JuggleCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.BASIC_BATTERY.get(), BasicBatteryScreen::new);
        event.register(ModMenuTypes.COAL_GENERATOR.get(), CoalGeneratorScreen::new);
        event.register(ModMenuTypes.JBOX.get(), JboxScreen::new);
    }
}