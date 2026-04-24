package com.eddie.jugglecraft.block.custom;

import com.eddie.jugglecraft.block.entity.CoalGeneratorBlockEntity;
import com.eddie.jugglecraft.registry.ModBlockEntities;
import com.eddie.jugglecraft.util.ModInventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class CoalGeneratorBlock extends HorizontalDirectionalBlock implements EntityBlock {
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
        Block.box(1.0, 0.0, 1.0, 15.0, 10.0, 15.0),
        Block.box(2.0, 10.0, 2.0, 10.0, 14.0, 14.0),
        Block.box(11.0, 10.0, 4.0, 14.0, 16.0, 8.0),
        Block.box(4.0, 3.0, 0.0, 12.0, 8.0, 1.0)
    );
    private static final VoxelShape EAST_SHAPE = Shapes.or(
        Block.box(1.0, 0.0, 1.0, 15.0, 10.0, 15.0),
        Block.box(2.0, 10.0, 2.0, 14.0, 14.0, 10.0),
        Block.box(8.0, 10.0, 11.0, 12.0, 16.0, 14.0),
        Block.box(15.0, 3.0, 4.0, 16.0, 8.0, 12.0)
    );
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(
        Block.box(1.0, 0.0, 1.0, 15.0, 10.0, 15.0),
        Block.box(6.0, 10.0, 2.0, 14.0, 14.0, 14.0),
        Block.box(2.0, 10.0, 8.0, 5.0, 16.0, 12.0),
        Block.box(4.0, 3.0, 15.0, 12.0, 8.0, 16.0)
    );
    private static final VoxelShape WEST_SHAPE = Shapes.or(
        Block.box(1.0, 0.0, 1.0, 15.0, 10.0, 15.0),
        Block.box(2.0, 10.0, 6.0, 14.0, 14.0, 14.0),
        Block.box(4.0, 10.0, 2.0, 8.0, 16.0, 5.0),
        Block.box(0.0, 3.0, 4.0, 1.0, 8.0, 12.0)
    );

    public CoalGeneratorBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(4.0f, 6.0f)
                .requiresCorrectToolForDrops());

        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CoalGeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide || blockEntityType != ModBlockEntities.COAL_GENERATOR.get()) {
            return null;
        }

        return (serverLevel, blockPos, blockState, blockEntity) -> {
            if (blockEntity instanceof CoalGeneratorBlockEntity coalGeneratorBlockEntity) {
                coalGeneratorBlockEntity.serverTick(serverLevel, blockPos, blockState);
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof CoalGeneratorBlockEntity coalGeneratorBlockEntity) {
            NetworkHooks.openScreen(serverPlayer, coalGeneratorBlockEntity, pos);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CoalGeneratorBlockEntity coalGeneratorBlockEntity) {
            ModInventoryHelper.dropContents(level, pos, coalGeneratorBlockEntity.getItemHandler());
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }
}