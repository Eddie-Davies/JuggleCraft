package com.eddie.jugglecraft.block.custom;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * RS485 Terminal/Switch block that acts as a connection point for RS485 wires.
 * Allows adjacent blocks to connect to RS485 network infrastructure.
 * Compact connector block with no UI.
 */
public class Rs485TerminalBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0)
    );

    public Rs485TerminalBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_ORANGE)
                .strength(1.5f, 2.0f)
                .noOcclusion());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, net.minecraft.core.BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
