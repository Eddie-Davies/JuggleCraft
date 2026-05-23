package com.eddie.jugglecraft.client.screen;

import com.eddie.jugglecraft.logic.DeviceAddress;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Base class for energy block UIs (Solar Panel, Coal Generator, Battery).
 * Provides consistent layout, styling, and IP address arrow-key control.
 */
public abstract class AbstractEnergyScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    public AbstractEnergyScreen(T menu, Inventory inventory, Component title, int imageWidth, int imageHeight) {
        super(menu, inventory, title);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    /**
     * Draw consistent background for energy screens.
     * Should be called from child class renderBg() methods after custom drawing.
     */
    protected void drawEnergyBackground(GuiGraphics guiGraphics, int borderColor, int contentBgColor, int infoBoxColor) {
        int x = leftPos;
        int y = topPos;

        // Outer border
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, borderColor);
        // Main content background
        guiGraphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, contentBgColor);
        // Info box (bottom area for IP display)
        guiGraphics.fill(x + 8, y + imageHeight - 26, x + imageWidth - 8, y + imageHeight - 8, infoBoxColor);
    }

    /**
     * Render consistent info text for IP address display.
     */
    protected void renderIpLabel(GuiGraphics guiGraphics, int yOffset, int textColor) {
        guiGraphics.drawString(font, 
                Component.literal("IP: " + DeviceAddress.toString(getMenuIpAddress())), 
                leftPos + 12, 
                topPos + yOffset, 
                textColor, 
                false);
        guiGraphics.drawString(font, 
                Component.literal("IP Octets"), 
                leftPos + 12, 
                topPos + imageHeight - 22, 
                textColor, 
                false);
    }

    /**
     * Handle arrow key input for IP address adjustment.
     * Override this in subclasses to handle arrow up/down for specific octet cycling.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Arrow Up (GLFW key 265) and Arrow Down (GLFW key 264)
        if (keyCode == 265) { // Arrow Up
            onIpArrowUp();
            return true;
        } else if (keyCode == 264) { // Arrow Down
            onIpArrowDown();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Called when arrow up is pressed. Subclasses should override to change active octet or increment.
     */
    protected void onIpArrowUp() {
        // Override in subclass
    }

    /**
     * Called when arrow down is pressed. Subclasses should override to change active octet or decrement.
     */
    protected void onIpArrowDown() {
        // Override in subclass
    }

    /**
     * Get the current IP address from the menu. Subclasses must implement.
     */
    protected abstract int getMenuIpAddress();

    /**
     * Send IP adjustment command to the menu/server.
     * Subclasses should use this to call pressMenuButton with appropriate button IDs.
     */
    protected abstract void adjustIpOctet(int octetIndex, int delta);

    protected void pressMenuButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }
}
