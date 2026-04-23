package com.eddie.jugglecraft.client.screen;

import com.eddie.jugglecraft.menu.CoalGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CoalGeneratorScreen extends AbstractContainerScreen<CoalGeneratorMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    private Button generatorToggleButton;

    public CoalGeneratorScreen(CoalGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
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

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        if (menu.isBurning()) {
            int burnProgress = menu.getBurnProgress();
            guiGraphics.blit(TEXTURE, x + 56, y + 36 + 12 - burnProgress, 176, 12 - burnProgress, 14, burnProgress + 1);
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
                    Component.literal(menu.getEnergyStored() + "/" + menu.getEnergyCapacity() + " FE"),
                    mouseX,
                    mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(font, Component.literal(menu.getEnergyStored() + " FE"), 100, 74, 0x404040, false);
        guiGraphics.drawString(font, Component.literal(menu.isGeneratorEnabled() ? "State: ON" : "State: OFF"), 88, 36, 0x404040, false);
    }

    private void pressMenuButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    private Component getGeneratorButtonLabel() {
        return Component.literal(menu.isGeneratorEnabled() ? "Turn Off" : "Turn On");
    }
}