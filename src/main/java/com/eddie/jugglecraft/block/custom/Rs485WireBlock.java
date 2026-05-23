package com.eddie.jugglecraft.block.custom;

import com.eddie.jugglecraft.logic.Rs485NetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Rs485WireBlock extends Block {
    public static final BooleanProperty CEILING = BooleanProperty.create("ceiling");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");

    private static final VoxelShape FLOOR_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape CEILING_SHAPE = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);

    public Rs485WireBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .strength(2.0f, 3.0f)
                .noOcclusion());

        registerDefaultState(stateDefinition.any()
                .setValue(CEILING, false)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CEILING, NORTH, SOUTH, EAST, WEST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        boolean ceiling = shouldPlaceOnCeiling(context);
        BlockState state = updateConnections(context.getLevel(), pos, defaultBlockState().setValue(CEILING, ceiling));
        return state.canSurvive(context.getLevel(), pos) ? state : null;
    }

    private boolean shouldPlaceOnCeiling(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace == Direction.DOWN) {
            return true;
        }

        if (clickedFace == Direction.UP) {
            return false;
        }

        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction == Direction.DOWN) {
                return true;
            }
            if (direction == Direction.UP) {
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(CEILING)) {
            BlockPos supportPos = pos.above();
            return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.DOWN);
        }

        BlockPos supportPos = pos.below();
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                  BlockPos currentPos, BlockPos neighborPos) {
        if (!state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }

        if (direction == Direction.NORTH || direction == Direction.SOUTH || direction == Direction.EAST || direction == Direction.WEST) {
            return updateConnections(level, currentPos, state);
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    private BlockState updateConnections(BlockGetter level, BlockPos pos, BlockState state) {
        boolean isCeiling = state.getValue(CEILING);
        return state
                .setValue(NORTH, canConnectTo(level, pos.north(), isCeiling))
                .setValue(SOUTH, canConnectTo(level, pos.south(), isCeiling))
                .setValue(EAST, canConnectTo(level, pos.east(), isCeiling))
                .setValue(WEST, canConnectTo(level, pos.west(), isCeiling));
    }

    private boolean canConnectTo(BlockGetter level, BlockPos pos, boolean isCeiling) {
        BlockState neighborState = level.getBlockState(pos);
        if (neighborState.is(this)) {
            return neighborState.getValue(CEILING) == isCeiling;
        }

        return Rs485NetworkHelper.isRs485Connectable(level, pos);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, net.minecraft.core.BlockPos pos, CollisionContext context) {
        return state.getValue(CEILING) ? CEILING_SHAPE : FLOOR_SHAPE;
    }
}