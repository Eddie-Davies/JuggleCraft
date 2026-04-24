package com.eddie.jugglecraft.registry;

import com.eddie.jugglecraft.JuggleCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JuggleCraft.MODID);

    public static final RegistryObject<CreativeModeTab> JUGGLECRAFT_TAB = CREATIVE_MODE_TABS.register("jugglecraft_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.jugglecraft.jugglecraft_tab"))
                    .icon(() -> ModItems.JBOX.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.JBOX.get());
                        output.accept(ModItems.COAL_GENERATOR.get());
                        output.accept(ModItems.BASIC_BATTERY.get());
                        output.accept(ModItems.FE_TO_REDSTONE_TRANSFORMER.get());
                        output.accept(ModItems.RS485_WIRE.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}