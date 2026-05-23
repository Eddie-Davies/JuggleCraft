package com.eddie.jugglecraft.block.custom;

import com.eddie.jugglecraft.logic.Rs485NetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * RS485 Terminal/Switch block that acts as a connection point for RS485 wires.
 * Allows adjacent blocks to connect to RS485 network infrastructure.
 * Compact connector block with no UI.
 */
public class Rs485TerminalBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WEST = BooleanProperty.create("west");

    private static final VoxelShape NORTH_SHAPE = Shapes.box(0.0, 0.0, 8.0 / 16.0, 1.0, 1.0, 1.0);
    private static final VoxelShape SOUTH_SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, 8.0 / 16.0);
    private static final VoxelShape EAST_SHAPE = Shapes.box(0.0, 0.0, 0.0, 8.0 / 16.0, 1.0, 1.0);
    private static final VoxelShape WEST_SHAPE = Shapes.box(8.0 / 16.0, 0.0, 0.0, 1.0, 1.0, 1.0);

    public Rs485TerminalBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.COLOR_ORANGE)
                .strength(1.5f, 2.0f)
                .noOcclusion());

        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, NORTH, SOUTH, EAST, WEST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        return updateConnections(context.getLevel(), pos, state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                  BlockPos currentPos, BlockPos neighborPos) {
        if (direction == Direction.NORTH || direction == Direction.SOUTH || direction == Direction.EAST || direction == Direction.WEST) {
            return updateConnections(level, currentPos, state);
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    private BlockState updateConnections(BlockGetter level, BlockPos pos, BlockState state) {
        return state
                .setValue(NORTH, Rs485NetworkHelper.isRs485Connectable(level, pos.north()))
                .setValue(SOUTH, Rs485NetworkHelper.isRs485Connectable(level, pos.south()))
                .setValue(EAST, Rs485NetworkHelper.isRs485Connectable(level, pos.east()))
                .setValue(WEST, Rs485NetworkHelper.isRs485Connectable(level, pos.west()));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, net.minecraft.core.BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }
}
