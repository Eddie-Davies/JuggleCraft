package com.eddie.jugglecraft.client.screen;

import com.eddie.jugglecraft.block.entity.JboxBlockEntity;
import com.eddie.jugglecraft.menu.JboxMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class JboxScreen extends AbstractContainerScreen<JboxMenu> {
    private Button controlDeviceButton;

    public JboxScreen(JboxMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;

        controlDeviceButton = addRenderableWidget(Button.builder(getControlButtonLabel(), button -> pressMenuButton(JboxMenu.CONTROL_DEVICE_BUTTON))
                .bounds(leftPos + 10, topPos + 52, 156, 20)
                .build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (controlDeviceButton != null) {
            controlDeviceButton.setMessage(getControlButtonLabel());
            controlDeviceButton.active = menu.getAttachedDeviceType() != JboxBlockEntity.DEVICE_NONE;
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF8AA29E);
        guiGraphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFF3EBDD);
        guiGraphics.fill(x + 8, y + 18, x + imageWidth - 8, y + 78, 0xFF1F2A2E);
        guiGraphics.fill(x + 154, y + 22, x + 166, y + 72, 0xFF516169);

        int energyProgress = menu.getEnergyProgress();
        if (energyProgress > 0) {
            guiGraphics.fill(x + 155, y + 72 - energyProgress, x + 165, y + 72, 0xFF4CAF50);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        if (isHovering(154, 22, 12, 50, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font,
                    Component.literal(menu.getTrackedEnergy() + "/" + menu.getTrackedCapacity() + " FE"),
                    mouseX,
                    mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(font, Component.literal("Attached: " + getAttachedDeviceName()), 12, 24, 0xFFF3EBDD, false);
        guiGraphics.drawString(font, Component.literal("Control: ").append(getControlStateLabel()), 12, 36, 0xFFF3EBDD, false);
        guiGraphics.drawString(font, Component.literal("Stored: " + menu.getTrackedEnergy() + " FE"), 12, 48, 0xFFF3EBDD, false);
        guiGraphics.drawString(font, Component.literal("Capacity: " + menu.getTrackedCapacity() + " FE"), 12, 60, 0xFFF3EBDD, false);
    }

    private void pressMenuButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    private Component getControlButtonLabel() {
        return switch (menu.getAttachedDeviceType()) {
            case JboxBlockEntity.DEVICE_BATTERY -> Component.literal("Cycle Battery Mode");
            case JboxBlockEntity.DEVICE_GENERATOR -> Component.literal(menu.isGeneratorEnabled() ? "Turn Generator Off" : "Turn Generator On");
            default -> Component.literal("No Controllable Device Attached");
        };
    }

    private String getAttachedDeviceName() {
        return switch (menu.getAttachedDeviceType()) {
            case JboxBlockEntity.DEVICE_BATTERY -> "Basic Battery";
            case JboxBlockEntity.DEVICE_GENERATOR -> "Coal Generator";
            default -> "None";
        };
    }

    private Component getControlStateLabel() {
        return switch (menu.getAttachedDeviceType()) {
            case JboxBlockEntity.DEVICE_BATTERY -> menu.getBatteryChargeMode().getDisplayName();
            case JboxBlockEntity.DEVICE_GENERATOR -> Component.literal(menu.isGeneratorEnabled() ? "ON" : "OFF");
            default -> Component.literal("Unavailable");
        };
    }
}