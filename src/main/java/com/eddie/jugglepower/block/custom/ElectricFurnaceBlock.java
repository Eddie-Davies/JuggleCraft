package com.eddie.jugglepower.block.custom;

import com.eddie.jugglepower.block.entity.ElectricFurnaceBlockEntity;
import com.eddie.jugglepower.registry.PowerBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ElectricFurnaceBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    public ElectricFurnaceBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(4.5f, 6.5f)
                .requiresCorrectToolForDrops());
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
        return new ElectricFurnaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide || blockEntityType != PowerBlockEntities.ELECTRIC_FURNACE.get()) {
            return null;
        }

        return (serverLevel, blockPos, blockState, blockEntity) -> {
            if (blockEntity instanceof ElectricFurnaceBlockEntity electricFurnaceBlockEntity) {
                electricFurnaceBlockEntity.serverTick(serverLevel, blockPos, blockState);
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide
                && player instanceof ServerPlayer
                && level.getBlockEntity(pos) instanceof ElectricFurnaceBlockEntity electricFurnaceBlockEntity) {
            ItemStack held = player.getItemInHand(hand);

            if (player.isShiftKeyDown()) {
                electricFurnaceBlockEntity.toggleEnabled();
                player.displayClientMessage(Component.translatable("message.jugglepower.electric_furnace_state",
                        electricFurnaceBlockEntity.isEnabled() ? "ON" : "OFF"), true);
                return InteractionResult.sidedSuccess(false);
            }

            if (!held.isEmpty() && electricFurnaceBlockEntity.tryInsertInput(player, held)) {
                return InteractionResult.sidedSuccess(false);
            }

            if (electricFurnaceBlockEntity.tryExtractOutput(player)) {
                return InteractionResult.sidedSuccess(false);
            }

            player.displayClientMessage(Component.translatable("message.jugglepower.electric_furnace_status",
                    electricFurnaceBlockEntity.getStoredEnergy(), electricFurnaceBlockEntity.getProgress(), electricFurnaceBlockEntity.getRequiredProgress()), true);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
