package com.eddie.jugglepower.registry;

import com.eddie.jugglepower.JugglePower;
import com.eddie.jugglepower.block.entity.BatteryCellBlockEntity;
import com.eddie.jugglepower.block.entity.ElectricFurnaceBlockEntity;
import com.eddie.jugglepower.block.entity.MosfetSwitchBlockEntity;
import com.eddie.jugglepower.block.entity.PowerCableBlockEntity;
import com.eddie.jugglepower.block.entity.PvCellBlockEntity;
import com.eddie.jugglepower.block.entity.WindTurbineBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class PowerBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, JugglePower.MODID);

    public static final RegistryObject<BlockEntityType<PowerCableBlockEntity>> POWER_CABLE = BLOCK_ENTITY_TYPES.register("power_cable",
            () -> BlockEntityType.Builder.of(PowerCableBlockEntity::new,
                    PowerBlocks.LV_CABLE.get(),
                    PowerBlocks.HV_CABLE.get(),
                    PowerBlocks.DC_CABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<BatteryCellBlockEntity>> BATTERY_CELL = BLOCK_ENTITY_TYPES.register("battery_cell",
            () -> BlockEntityType.Builder.of(BatteryCellBlockEntity::new, PowerBlocks.BATTERY_CELL.get()).build(null));

    public static final RegistryObject<BlockEntityType<PvCellBlockEntity>> PV_CELL = BLOCK_ENTITY_TYPES.register("pv_cell",
            () -> BlockEntityType.Builder.of(PvCellBlockEntity::new, PowerBlocks.PV_CELL.get()).build(null));

    public static final RegistryObject<BlockEntityType<WindTurbineBlockEntity>> WIND_TURBINE = BLOCK_ENTITY_TYPES.register("wind_turbine",
            () -> BlockEntityType.Builder.of(WindTurbineBlockEntity::new, PowerBlocks.WIND_TURBINE.get()).build(null));

    public static final RegistryObject<BlockEntityType<ElectricFurnaceBlockEntity>> ELECTRIC_FURNACE = BLOCK_ENTITY_TYPES.register("electric_furnace",
            () -> BlockEntityType.Builder.of(ElectricFurnaceBlockEntity::new, PowerBlocks.ELECTRIC_FURNACE.get()).build(null));

    public static final RegistryObject<BlockEntityType<MosfetSwitchBlockEntity>> MOSFET_SWITCH = BLOCK_ENTITY_TYPES.register("mosfet_switch",
            () -> BlockEntityType.Builder.of(MosfetSwitchBlockEntity::new, PowerBlocks.MOSFET_SWITCH.get()).build(null));

    private PowerBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
