package com.eddie.jugglepower.registry;

import com.eddie.jugglepower.JugglePower;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class PowerCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JugglePower.MODID);

    public static final RegistryObject<CreativeModeTab> JUGGLEPOWER_TAB = CREATIVE_MODE_TABS.register("jugglepower_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.jugglepower.main"))
                    .icon(() -> PowerItems.WIND_TURBINE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(PowerItems.LV_CABLE.get());
                        output.accept(PowerItems.HV_CABLE.get());
                        output.accept(PowerItems.DC_CABLE.get());
                        output.accept(PowerItems.SIGNAL_CABLE.get());
                        output.accept(PowerItems.BATTERY_CELL.get());
                        output.accept(PowerItems.PV_CELL.get());
                        output.accept(PowerItems.WIND_TURBINE.get());
                        output.accept(PowerItems.ELECTRIC_FURNACE.get());
                        output.accept(PowerItems.MOSFET_SWITCH.get());
                    })
                    .build());

    private PowerCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
