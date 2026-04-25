package com.eddie.jugglepower.registry;

import com.eddie.jugglepower.JugglePower;
import com.eddie.jugglepower.block.custom.BatteryCellBlock;
import com.eddie.jugglepower.block.custom.ElectricFurnaceBlock;
import com.eddie.jugglepower.block.custom.MosfetSwitchBlock;
import com.eddie.jugglepower.block.custom.PowerCableBlock;
import com.eddie.jugglepower.block.custom.PvCellBlock;
import com.eddie.jugglepower.block.custom.SignalCableBlock;
import com.eddie.jugglepower.block.custom.WindTurbineBlock;
import com.eddie.jugglepower.power.PowerTier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class PowerBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, JugglePower.MODID);

    public static final RegistryObject<Block> LV_CABLE = BLOCKS.register("lv_cable",
            () -> new PowerCableBlock(PowerTier.LV, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.5f, 2.0f).noOcclusion()));

    public static final RegistryObject<Block> HV_CABLE = BLOCKS.register("hv_cable",
            () -> new PowerCableBlock(PowerTier.HV, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(2.0f, 3.0f).noOcclusion()));

    public static final RegistryObject<Block> DC_CABLE = BLOCKS.register("dc_cable",
            () -> new PowerCableBlock(PowerTier.DC, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.5f, 2.0f).noOcclusion()));

    public static final RegistryObject<Block> SIGNAL_CABLE = BLOCKS.register("signal_cable", SignalCableBlock::new);

    public static final RegistryObject<Block> BATTERY_CELL = BLOCKS.register("battery_cell", BatteryCellBlock::new);
    public static final RegistryObject<Block> PV_CELL = BLOCKS.register("pv_cell", PvCellBlock::new);
    public static final RegistryObject<Block> WIND_TURBINE = BLOCKS.register("wind_turbine", WindTurbineBlock::new);
        public static final RegistryObject<Block> ELECTRIC_FURNACE = BLOCKS.register("electric_furnace", ElectricFurnaceBlock::new);
        public static final RegistryObject<Block> MOSFET_SWITCH = BLOCKS.register("mosfet_switch", MosfetSwitchBlock::new);

    private PowerBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
