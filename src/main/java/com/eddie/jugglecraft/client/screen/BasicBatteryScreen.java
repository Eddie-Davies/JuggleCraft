package com.eddie.jugglecraft.client.screen;

import com.eddie.jugglecraft.logic.DeviceAddress;
import com.eddie.jugglecraft.menu.BasicBatteryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BasicBatteryScreen extends AbstractContainerScreen<BasicBatteryMenu> {
    private Button chargeModeButton;

    public BasicBatteryScreen(BasicBatteryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 224;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        chargeModeButton = addRenderableWidget(Button.builder(getChargeModeButtonLabel(), button -> pressMenuButton(BasicBatteryMenu.CYCLE_CHARGE_MODE_BUTTON))
                .bounds(leftPos + 12, topPos + 54, 118, 20)
                .build());

        addIpButtons(12, 182, BasicBatteryMenu.IP_OCTET_DECREMENT_BASE, BasicBatteryMenu.IP_OCTET_INCREMENT_BASE);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (chargeModeButton != null) {
            chargeModeButton.setMessage(getChargeModeButtonLabel());
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFBFC9C9);
        guiGraphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFF4EFE6);
        guiGraphics.fill(x + 7, y + 19, x + imageWidth - 7, y + 80, 0xFF2D3436);
        guiGraphics.fill(x + 133, y + 22, x + 145, y + 72, 0xFF606C76);
        guiGraphics.fill(x + 7, y + 166, x + imageWidth - 7, y + imageHeight - 8, 0xFFDDD6CA);

        int energyProgress = menu.getEnergyProgress();
        if (energyProgress > 0) {
            guiGraphics.fill(x + 134, y + 72 - energyProgress, x + 144, y + 72, 0xFFE3A018);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        if (isHovering(133, 22, 12, 50, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font,
                    Component.literal(menu.getEnergyStored() + "/" + menu.getEnergyCapacity() + " FE"),
                    mouseX,
                    mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0xFF2D3436, false);
        guiGraphics.drawString(font, Component.literal("Stored: " + menu.getEnergyStored() + " FE"), 12, 26, 0xFFF4EFE6, false);
        guiGraphics.drawString(font, Component.literal("Capacity: " + menu.getEnergyCapacity() + " FE"), 12, 42, 0xFFF4EFE6, false);
        guiGraphics.drawString(font, Component.literal("Mode: ").append(menu.getChargeMode().getDisplayName()), 12, 58, 0xFFF4EFE6, false);
        guiGraphics.drawString(font, Component.literal("IP: " + DeviceAddress.toString(menu.getIpAddress())), 12, 74, 0xFFF4EFE6, false);
        guiGraphics.drawString(font, Component.literal("IP Octets"), 12, 170, 0xFF2D3436, false);
    }

    private void pressMenuButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    private Component getChargeModeButtonLabel() {
        return Component.literal("Mode: ").append(menu.getChargeMode().getDisplayName());
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