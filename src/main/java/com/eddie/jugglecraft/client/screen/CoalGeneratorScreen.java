package com.eddie.jugglecraft.client.screen;

import com.eddie.jugglecraft.menu.CoalGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CoalGeneratorScreen extends AbstractEnergyScreen<CoalGeneratorMenu> {
    private Button generatorToggleButton;

    public CoalGeneratorScreen(CoalGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 224);
    }

    @Override
    protected void init() {
        super.init();
        generatorToggleButton = addRenderableWidget(Button.builder(getGeneratorButtonLabel(), button -> pressMenuButton(CoalGeneratorMenu.TOGGLE_GENERATOR_BUTTON))
                .bounds(leftPos + 88, topPos + 52, 78, 20)
                .build());
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

        drawEnergyBackground(guiGraphics, 0xFF9EA7B3, 0xFFF0ECE3, 0xFFDAD3C7);

        // Burn progress bar
        guiGraphics.fill(x + 55, y + 35, x + 69, y + 49, 0xFF40352A);
        if (menu.isBurning()) {
            int burnProgress = menu.getBurnProgress();
            guiGraphics.fill(x + 56, y + 48 - burnProgress, x + 68, y + 49, 0xFFFF9C2F);
        }

        // Energy bar background and fill
        guiGraphics.fill(x + 119, y + 20, x + 127, y + 70, 0xFF5C6770);
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
        renderIpLabel(guiGraphics, 170, 0xFF2D3436);
    }

    protected void pressMenuButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    private Component getGeneratorButtonLabel() {
        return Component.literal(menu.isGeneratorEnabled() ? "Turn Off" : "Turn On");
    }

    @Override
    protected int getMenuIpAddress() {
        return menu.getIpAddress();
    }

    @Override
    protected void adjustIpOctet(int octetIndex, int delta) {
        if (delta > 0) {
            pressMenuButton(CoalGeneratorMenu.IP_OCTET_INCREMENT_BASE + octetIndex);
        } else {
            pressMenuButton(CoalGeneratorMenu.IP_OCTET_DECREMENT_BASE + octetIndex);
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