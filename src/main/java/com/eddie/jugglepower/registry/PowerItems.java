package com.eddie.jugglepower.registry;

import com.eddie.jugglepower.JugglePower;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class PowerItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, JugglePower.MODID);

    public static final RegistryObject<Item> LV_CABLE = registerBlockItem("lv_cable", PowerBlocks.LV_CABLE);
    public static final RegistryObject<Item> HV_CABLE = registerBlockItem("hv_cable", PowerBlocks.HV_CABLE);
    public static final RegistryObject<Item> DC_CABLE = registerBlockItem("dc_cable", PowerBlocks.DC_CABLE);
    public static final RegistryObject<Item> SIGNAL_CABLE = registerBlockItem("signal_cable", PowerBlocks.SIGNAL_CABLE);
    public static final RegistryObject<Item> BATTERY_CELL = registerBlockItem("battery_cell", PowerBlocks.BATTERY_CELL);
    public static final RegistryObject<Item> PV_CELL = registerBlockItem("pv_cell", PowerBlocks.PV_CELL);
    public static final RegistryObject<Item> WIND_TURBINE = registerBlockItem("wind_turbine", PowerBlocks.WIND_TURBINE);
    public static final RegistryObject<Item> ELECTRIC_FURNACE = registerBlockItem("electric_furnace", PowerBlocks.ELECTRIC_FURNACE);
    public static final RegistryObject<Item> MOSFET_SWITCH = registerBlockItem("mosfet_switch", PowerBlocks.MOSFET_SWITCH);

    private PowerItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private static RegistryObject<Item> registerBlockItem(String name, RegistryObject<net.minecraft.world.level.block.Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
