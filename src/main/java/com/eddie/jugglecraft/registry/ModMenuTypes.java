package com.eddie.jugglecraft.registry;

import com.eddie.jugglecraft.menu.BasicBatteryMenu;
import com.eddie.jugglecraft.JuggleCraft;
import com.eddie.jugglecraft.menu.CoalGeneratorMenu;
import com.eddie.jugglecraft.menu.JboxMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, JuggleCraft.MODID);

        public static final RegistryObject<MenuType<BasicBatteryMenu>> BASIC_BATTERY = MENU_TYPES.register("basic_battery",
            () -> IForgeMenuType.create(BasicBatteryMenu::new));

    public static final RegistryObject<MenuType<CoalGeneratorMenu>> COAL_GENERATOR = MENU_TYPES.register("coal_generator",
            () -> IForgeMenuType.create(CoalGeneratorMenu::new));

        public static final RegistryObject<MenuType<JboxMenu>> JBOX = MENU_TYPES.register("jbox",
            () -> IForgeMenuType.create(JboxMenu::new));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}