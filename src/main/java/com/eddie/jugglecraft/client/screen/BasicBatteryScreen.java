package com.eddie.jugglecraft.client.screen;

import com.eddie.jugglecraft.menu.BasicBatteryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BasicBatteryScreen extends AbstractEnergyScreen<BasicBatteryMenu> {
    private Button chargeModeButton;

    public BasicBatteryScreen(BasicBatteryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 224);
    }

    @Override
    protected void init() {
        super.init();
        chargeModeButton = addRenderableWidget(Button.builder(getChargeModeButtonLabel(), button -> pressMenuButton(BasicBatteryMenu.CYCLE_CHARGE_MODE_BUTTON))
                .bounds(leftPos + 12, topPos + 54, 118, 20)
                .build());
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

        drawEnergyBackground(guiGraphics, 0xFFBFC9C9, 0xFFF4EFE6, 0xFFDDD6CA);

        // Energy bar background and fill
        guiGraphics.fill(x + 133, y + 22, x + 145, y + 72, 0xFF606C76);
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
        renderIpLabel(guiGraphics, 170, 0xFF2D3436);
    }

    protected void pressMenuButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    private Component getChargeModeButtonLabel() {
        return Component.literal("Mode: ").append(menu.getChargeMode().getDisplayName());
    }

    @Override
    protected int getMenuIpAddress() {
        return menu.getIpAddress();
    }

    @Override
    protected void adjustIpOctet(int octetIndex, int delta) {
        if (delta > 0) {
            pressMenuButton(BasicBatteryMenu.IP_OCTET_INCREMENT_BASE + octetIndex);
        } else {
            pressMenuButton(BasicBatteryMenu.IP_OCTET_DECREMENT_BASE + octetIndex);
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