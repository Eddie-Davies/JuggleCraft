package com.eddie.jugglepower.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SignalCableBlock extends Block {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 16.0),
            Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0),
            Block.box(0.0, 6.0, 6.0, 16.0, 10.0, 10.0)
    );

    public SignalCableBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .strength(1.0f, 1.5f)
                .noOcclusion());

        registerDefaultState(stateDefinition.any().setValue(POWER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            updatePower(level, pos, state);
        }

        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            updatePower(level, pos, state);
        }

        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos,
                                  BlockPos neighborPos) {
        if (level instanceof Level realLevel && !realLevel.isClientSide) {
            updatePower(realLevel, currentPos, state);
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    private void updatePower(Level level, BlockPos pos, BlockState state) {
        int nextSignal = level.getBestNeighborSignal(pos);
        if (state.getValue(POWER) != nextSignal) {
            level.setBlock(pos, state.setValue(POWER, nextSignal), 2);
        }
    }
}
