package com.eddie.jugglepower.block.custom;

import com.eddie.jugglepower.block.entity.PowerCableBlockEntity;
import com.eddie.jugglepower.power.PowerTier;
import com.eddie.jugglepower.registry.PowerBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PowerCableBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 16.0),
            Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0),
            Block.box(0.0, 6.0, 6.0, 16.0, 10.0, 10.0)
    );

    private final PowerTier tier;

    public PowerCableBlock(PowerTier tier, BlockBehaviour.Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public PowerTier getTier() {
        return tier;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PowerCableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide || blockEntityType != PowerBlockEntities.POWER_CABLE.get()) {
            return null;
        }

        return (serverLevel, blockPos, blockState, blockEntity) -> {
            if (blockEntity instanceof PowerCableBlockEntity powerCableBlockEntity) {
                powerCableBlockEntity.serverTick(serverLevel, blockPos, blockState);
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide
                && player instanceof ServerPlayer
                && player.isShiftKeyDown()
                && level.getBlockEntity(pos) instanceof PowerCableBlockEntity powerCableBlockEntity) {
            powerCableBlockEntity.clearFault();
            player.displayClientMessage(Component.translatable("message.jugglepower.cable_fault_reset"), true);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
