package com.eddie.jugglecraft.registry;

import com.eddie.jugglecraft.JuggleCraft;
import com.eddie.jugglecraft.block.entity.BasicBatteryBlockEntity;
import com.eddie.jugglecraft.block.entity.CoalGeneratorBlockEntity;
import com.eddie.jugglecraft.block.entity.JboxBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, JuggleCraft.MODID);

    public static final RegistryObject<BlockEntityType<JboxBlockEntity>> JBOX = BLOCK_ENTITY_TYPES.register("jbox",
            () -> BlockEntityType.Builder.of(JboxBlockEntity::new, ModBlocks.JBOX.get()).build(null));

    public static final RegistryObject<BlockEntityType<CoalGeneratorBlockEntity>> COAL_GENERATOR = BLOCK_ENTITY_TYPES.register("coal_generator",
            () -> BlockEntityType.Builder.of(CoalGeneratorBlockEntity::new, ModBlocks.COAL_GENERATOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<BasicBatteryBlockEntity>> BASIC_BATTERY = BLOCK_ENTITY_TYPES.register("basic_battery",
            () -> BlockEntityType.Builder.of(BasicBatteryBlockEntity::new, ModBlocks.BASIC_BATTERY.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}