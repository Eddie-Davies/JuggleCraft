package com.eddie.jugglecraft.client.screen;

import com.eddie.jugglecraft.menu.SolarPanelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class SolarPanelScreen extends AbstractEnergyScreen<SolarPanelMenu> {
    private Button togglePanelButton;

    public SolarPanelScreen(SolarPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 196, 276);
    }

    @Override
    protected void init() {
        super.init();
        togglePanelButton = addRenderableWidget(Button.builder(getToggleButtonLabel(), button -> pressMenuButton(SolarPanelMenu.TOGGLE_PANEL_BUTTON))
                .bounds(leftPos + 104, topPos + 54, 80, 20)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        drawEnergyBackground(guiGraphics, 0xFF829680, 0xFFF1EFDF, 0xFFD6D2C5);

        // Data display area
        guiGraphics.fill(x + 14, y + 116, x + imageWidth - 14, y + 172, 0xFFD6D2C5);

        // Energy and generation bars
        guiGraphics.fill(x + 150, y + 24, x + 162, y + 84, 0xFF52636A);
        guiGraphics.fill(x + 166, y + 24, x + 178, y + 84, 0xFF6E5B3C);

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
        renderIpLabel(guiGraphics, 230, 0xFF203026);
    }

    protected void pressMenuButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    private Component getToggleButtonLabel() {
        return Component.literal(menu.isPanelEnabled() ? "Disable Panel" : "Enable Panel");
    }

    @Override
    protected int getMenuIpAddress() {
        return menu.getIpAddress();
    }

    @Override
    protected void adjustIpOctet(int octetIndex, int delta) {
        if (delta > 0) {
            pressMenuButton(SolarPanelMenu.IP_OCTET_INCREMENT_BASE + octetIndex);
        } else {
            pressMenuButton(SolarPanelMenu.IP_OCTET_DECREMENT_BASE + octetIndex);
        }
    }

    @Override
    protected void onIpArrowUp() {
        adjustIpOctet(0, 1);
    }

    @Override
    protected void onIpArrowDown() {
        adjustIpOctet(0, -1);
    }
}