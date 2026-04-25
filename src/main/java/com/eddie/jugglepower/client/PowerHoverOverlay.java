package com.eddie.jugglepower.client;

import com.eddie.jugglepower.JugglePower;
import com.eddie.jugglepower.block.custom.SignalCableBlock;
import com.eddie.jugglepower.power.PowerStateDisplayProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = JugglePower.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PowerHoverOverlay {
    private PowerHoverOverlay() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }

        HitResult hitResult = minecraft.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockState state = minecraft.level.getBlockState(blockHitResult.getBlockPos());
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!JugglePower.MODID.equals(key.getNamespace())) {
            return;
        }

        List<Component> lines = new ArrayList<>();
        lines.add(state.getBlock().getName());

        BlockEntity blockEntity = minecraft.level.getBlockEntity(blockHitResult.getBlockPos());
        if (blockEntity instanceof PowerStateDisplayProvider powerStateDisplayProvider) {
            powerStateDisplayProvider.appendPowerStateLines(lines);
        } else if (state.getBlock() instanceof SignalCableBlock) {
            lines.add(Component.literal("Tier: SIGNAL"));
            lines.add(Component.literal("Signal: " + state.getValue(SignalCableBlock.POWER)));
        } else {
            lines.add(Component.literal("No telemetry available"));
        }

        renderPanel(event.getGuiGraphics(), minecraft.font, lines);
    }

    private static void renderPanel(GuiGraphics guiGraphics, Font font, List<Component> lines) {
        int padding = 4;
        int lineSpacing = 2;
        int lineHeight = font.lineHeight + lineSpacing;
        int width = 0;
        for (Component line : lines) {
            width = Math.max(width, font.width(line));
        }

        int panelWidth = width + padding * 2;
        int panelHeight = padding * 2 + lineHeight * lines.size();
        int x = 8;
        int y = 8;

        guiGraphics.fill(x - 1, y - 1, x + panelWidth + 1, y + panelHeight + 1, 0xFF2E323A);
        guiGraphics.fill(x, y, x + panelWidth, y + panelHeight, 0xB014161A);

        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawString(font, lines.get(i), x + padding, y + padding + i * lineHeight, 0xFFE8EEF2, false);
        }
    }
}
