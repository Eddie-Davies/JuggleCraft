package com.eddie.jugglecraft.block.custom;

import com.eddie.jugglecraft.block.entity.FeToRedstoneTransformerBlockEntity;
import com.eddie.jugglecraft.registry.ModBlockEntities;
import com.eddie.jugglecraft.util.MountedDeviceHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class FeToRedstoneTransformerBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    private static final VoxelShape NORTH_SHAPE = Shapes.box(0.0, 0.0, 8.0 / 16.0, 1.0, 1.0, 1.0);
    private static final VoxelShape SOUTH_SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, 8.0 / 16.0);
    private static final VoxelShape EAST_SHAPE = Shapes.box(0.0, 0.0, 0.0, 8.0 / 16.0, 1.0, 1.0);
    private static final VoxelShape WEST_SHAPE = Shapes.box(8.0 / 16.0, 0.0, 0.0, 1.0, 1.0, 1.0);

    public FeToRedstoneTransformerBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .strength(3.0f, 5.0f)
                .requiresCorrectToolForDrops());

        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWER, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        if (!MountedDeviceHelper.canAttachToEnergyDevice(context.getLevel(), context.getClickedPos(), facing)) {
            return null;
        }

        return defaultBlockState().setValue(FACING, facing).setValue(POWER, 0);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return MountedDeviceHelper.canAttachToEnergyDevice(level, pos, state.getValue(FACING));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos,
                                  BlockPos neighborPos) {
        if (!state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter blockGetter, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter blockGetter, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FeToRedstoneTransformerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide || blockEntityType != ModBlockEntities.FE_TO_REDSTONE_TRANSFORMER.get()) {
            return null;
        }

        return (serverLevel, blockPos, blockState, blockEntity) -> {
            if (blockEntity instanceof FeToRedstoneTransformerBlockEntity feToRedstoneTransformerBlockEntity) {
                feToRedstoneTransformerBlockEntity.serverTick(serverLevel, blockPos, blockState);
            }
        };
    }
}