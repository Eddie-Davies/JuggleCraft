package com.eddie.jugglecraft.client.screen;

import com.eddie.jugglecraft.logic.DeviceAddress;
import com.eddie.jugglecraft.menu.SolarPanelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SolarPanelScreen extends AbstractContainerScreen<SolarPanelMenu> {
    private Button togglePanelButton;

    public SolarPanelScreen(SolarPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 196;
        imageHeight = 276;
        inventoryLabelY = 182;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        togglePanelButton = addRenderableWidget(Button.builder(getToggleButtonLabel(), button -> pressMenuButton(SolarPanelMenu.TOGGLE_PANEL_BUTTON))
                .bounds(leftPos + 104, topPos + 54, 80, 20)
                .build());

        addIpButtons(12, 240, SolarPanelMenu.IP_OCTET_DECREMENT_BASE, SolarPanelMenu.IP_OCTET_INCREMENT_BASE);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (togglePanelButton != null) {
            togglePanelButton.setMessage(getToggleButtonLabel());
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF829680);
        guiGraphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFF1EFDF);
        guiGraphics.fill(x + 8, y + 18, x + imageWidth - 8, y + 96, 0xFF213026);
        guiGraphics.fill(x + 14, y + 116, x + imageWidth - 14, y + 172, 0xFFD6D2C5);
        guiGraphics.fill(x + 150, y + 24, x + 162, y + 84, 0xFF52636A);
        guiGraphics.fill(x + 166, y + 24, x + 178, y + 84, 0xFF6E5B3C);
        guiGraphics.fill(x + 8, y + 224, x + imageWidth - 8, y + imageHeight - 8, 0xFFD6D2C5);

        int energyProgress = menu.getEnergyProgress();
        if (energyProgress > 0) {
            guiGraphics.fill(x + 151, y + 84 - energyProgress, x + 161, y + 84, 0xFF3BB273);
        }

        int generationProgress = menu.getGenerationProgress();
        if (generationProgress > 0) {
            guiGraphics.fill(x + 167, y + 84 - generationProgress, x + 177, y + 84, 0xFFF5C144);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        if (isHovering(150, 24, 12, 60, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font,
                    Component.literal(menu.getEnergyStored() + "/" + menu.getEnergyCapacity() + " FE"),
                    mouseX,
                    mouseY);
        }

        if (isHovering(166, 24, 12, 60, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font,
                    Component.literal(menu.getCurrentGeneration() + "/" + menu.getMaxGeneration() + " FE/t"),
                    mouseX,
                    mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0xFF203026, false);
        guiGraphics.drawString(font, Component.literal("Stored: " + menu.getEnergyStored() + " FE"), 12, 26, 0xFFF1EFDF, false);
        guiGraphics.drawString(font, Component.literal("Output: " + menu.getCurrentGeneration() + "/" + menu.getMaxGeneration() + " FE/t"), 12, 40, 0xFFF1EFDF, false);
        guiGraphics.drawString(font, Component.literal(menu.isPanelEnabled() ? "State: ON" : "State: OFF"), 104, 40, 0xFFF1EFDF, false);
        guiGraphics.drawString(font, Component.literal("Sun: " + menu.getSunFactorPercent() + "%"), 18, 124, 0xFF203026, false);
        guiGraphics.drawString(font, Component.literal("Height: " + menu.getHeightFactorPercent() + "%"), 18, 138, 0xFF203026, false);
        guiGraphics.drawString(font, Component.literal("Weather: " + menu.getWeatherFactorPercent() + "%"), 100, 124, 0xFF203026, false);
        guiGraphics.drawString(font, Component.literal("Sky: " + menu.getSkyFactorPercent() + "%"), 100, 138, 0xFF203026, false);
        guiGraphics.drawString(font, Component.literal("IP: " + DeviceAddress.toString(menu.getIpAddress())), 12, 230, 0xFF203026, false);
    }

    private void pressMenuButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    private Component getToggleButtonLabel() {
        return Component.literal(menu.isPanelEnabled() ? "Disable Panel" : "Enable Panel");
    }

    private void addIpButtons(int baseX, int baseY, int decrementBase, int incrementBase) {
        for (int octetIndex = 0; octetIndex < DeviceAddress.OCTET_COUNT; octetIndex++) {
            int buttonX = leftPos + baseX + octetIndex * 38;
            int decrementId = decrementBase + octetIndex;
            int incrementId = incrementBase + octetIndex;

            addRenderableWidget(Button.builder(Component.literal("+"), button -> pressMenuButton(incrementId))
                    .bounds(buttonX, topPos + baseY, 18, 16)
                    .build());
            addRenderableWidget(Button.builder(Component.literal("-"), button -> pressMenuButton(decrementId))
                    .bounds(buttonX, topPos + baseY + 18, 18, 16)
                    .build());
        }
    }
}