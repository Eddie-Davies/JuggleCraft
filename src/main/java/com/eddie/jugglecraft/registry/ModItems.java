package com.eddie.jugglecraft.registry;

import com.eddie.jugglecraft.JuggleCraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, JuggleCraft.MODID);

    public static final RegistryObject<Item> JBOX = ITEMS.register("jbox",
            () -> new BlockItem(ModBlocks.JBOX.get(), new Item.Properties()));
    public static final RegistryObject<Item> COAL_GENERATOR = ITEMS.register("coal_generator",
            () -> new BlockItem(ModBlocks.COAL_GENERATOR.get(), new Item.Properties()));
    public static final RegistryObject<Item> BASIC_BATTERY = ITEMS.register("basic_battery",
            () -> new BlockItem(ModBlocks.BASIC_BATTERY.get(), new Item.Properties()));
    public static final RegistryObject<Item> FE_TO_REDSTONE_TRANSFORMER = ITEMS.register("fe_to_redstone_transformer",
            () -> new BlockItem(ModBlocks.FE_TO_REDSTONE_TRANSFORMER.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}