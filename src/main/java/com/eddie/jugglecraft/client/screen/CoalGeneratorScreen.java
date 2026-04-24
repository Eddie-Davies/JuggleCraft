package com.eddie.jugglecraft.client.screen;

import com.eddie.jugglecraft.logic.DeviceAddress;
import com.eddie.jugglecraft.menu.CoalGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CoalGeneratorScreen extends AbstractContainerScreen<CoalGeneratorMenu> {
    private Button generatorToggleButton;

    public CoalGeneratorScreen(CoalGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 224;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        generatorToggleButton = addRenderableWidget(Button.builder(getGeneratorButtonLabel(), button -> pressMenuButton(CoalGeneratorMenu.TOGGLE_GENERATOR_BUTTON))
                .bounds(leftPos + 88, topPos + 52, 78, 20)
                .build());

        addIpButtons(12, 182, CoalGeneratorMenu.IP_OCTET_DECREMENT_BASE, CoalGeneratorMenu.IP_OCTET_INCREMENT_BASE);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (generatorToggleButton != null) {
            generatorToggleButton.setMessage(getGeneratorButtonLabel());
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF9EA7B3);
        guiGraphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFF0ECE3);
        guiGraphics.fill(x + 7, y + 19, x + imageWidth - 7, y + 80, 0xFF2C3136);
        guiGraphics.fill(x + 7, y + 166, x + imageWidth - 7, y + imageHeight - 8, 0xFFDAD3C7);
        guiGraphics.fill(x + 119, y + 20, x + 127, y + 70, 0xFF5C6770);
        guiGraphics.fill(x + 55, y + 35, x + 69, y + 49, 0xFF40352A);

        if (menu.isBurning()) {
            int burnProgress = menu.getBurnProgress();
            guiGraphics.fill(x + 56, y + 48 - burnProgress, x + 68, y + 49, 0xFFFF9C2F);
        }

        int energyProgress = menu.getEnergyProgress();
        if (energyProgress > 0) {
            guiGraphics.fill(x + 119, y + 20 + (50 - energyProgress), x + 127, y + 70, 0xFF3BB273);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        if (isHovering(118, 20, 10, 50, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font,
                    Component.literal(menu.getEnergyStored() + "/" + menu.getEnergyCapacity() + " FE/t"),
                    mouseX,
                    mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0xFF2D3436, false);
        guiGraphics.drawString(font, Component.literal("Output: " + menu.getEnergyStored() + "/" + menu.getEnergyCapacity() + " FE/t"), 78, 74, 0x404040, false);
        guiGraphics.drawString(font, Component.literal(menu.isGeneratorEnabled() ? "State: ON" : "State: OFF"), 88, 36, 0x404040, false);
        guiGraphics.drawString(font, Component.literal("IP: " + DeviceAddress.toString(menu.getIpAddress())), 12, 74, 0xFF2D3436, false);
        guiGraphics.drawString(font, Component.literal("IP Octets"), 12, 170, 0xFF2D3436, false);
    }

    private void pressMenuButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    private Component getGeneratorButtonLabel() {
        return Component.literal(menu.isGeneratorEnabled() ? "Turn Off" : "Turn On");
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