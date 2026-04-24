package com.eddie.jugglecraft.client.screen;

import com.eddie.jugglecraft.block.entity.JboxBlockEntity;
import com.eddie.jugglecraft.logic.DeviceAddress;
import com.eddie.jugglecraft.menu.JboxMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class JboxScreen extends AbstractContainerScreen<JboxMenu> {
    private Button addDeviceButton;
    private Button controlDeviceButton;
    private Button removeDeviceButton;

    public JboxScreen(JboxMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 256;
        imageHeight = 224;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;

        addRenderableWidget(Button.builder(Component.literal("<"), button -> pressMenuButton(JboxMenu.SELECT_PREVIOUS_DEVICE_BUTTON))
            .bounds(leftPos + 12, topPos + 124, 20, 20)
            .build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> pressMenuButton(JboxMenu.SELECT_NEXT_DEVICE_BUTTON))
            .bounds(leftPos + 36, topPos + 124, 20, 20)
            .build());
        addDeviceButton = addRenderableWidget(Button.builder(Component.literal("Add Device"), button -> pressMenuButton(JboxMenu.ADD_DEVICE_BUTTON))
            .bounds(leftPos + 62, topPos + 124, 62, 20)
            .build());
        removeDeviceButton = addRenderableWidget(Button.builder(Component.literal("Remove"), button -> pressMenuButton(JboxMenu.REMOVE_DEVICE_BUTTON))
            .bounds(leftPos + 128, topPos + 124, 58, 20)
            .build());
        controlDeviceButton = addRenderableWidget(Button.builder(getControlButtonLabel(), button -> pressMenuButton(JboxMenu.CONTROL_DEVICE_BUTTON))
            .bounds(leftPos + 190, topPos + 124, 54, 20)
                .build());

        addIpButtons(12, 184, JboxMenu.TARGET_IP_DECREMENT_BASE, JboxMenu.TARGET_IP_INCREMENT_BASE);
        addIpButtons(164, 184, JboxMenu.LOCAL_IP_DECREMENT_BASE, JboxMenu.LOCAL_IP_INCREMENT_BASE);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (addDeviceButton != null) {
            addDeviceButton.active = menu.getRegisteredDeviceCount() < JboxBlockEntity.MAX_CONNECTED_DEVICES;
        }
        if (removeDeviceButton != null) {
            removeDeviceButton.active = menu.getSelectedDeviceRegistration() != 0;
        }
        if (controlDeviceButton != null) {
            controlDeviceButton.setMessage(getControlButtonLabel());
            controlDeviceButton.active = menu.isTrackedDeviceOnline() && menu.getAttachedDeviceType() != JboxBlockEntity.DEVICE_NONE;
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF80979A);
        guiGraphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFF1E8D9);
        guiGraphics.fill(x + 8, y + 18, x + imageWidth - 8, y + 118, 0xFF1F2A2E);
        guiGraphics.fill(x + 232, y + 24, x + 244, y + 108, 0xFF4F5F63);
        guiGraphics.fill(x + 8, y + 154, x + imageWidth - 8, y + imageHeight - 8, 0xFFD7D0C3);

        int energyProgress = menu.getEnergyProgress();
        if (energyProgress > 0) {
            guiGraphics.fill(x + 233, y + 108 - energyProgress, x + 243, y + 108, 0xFF4CAF50);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        if (isHovering(232, 24, 12, 84, mouseX, mouseY) && menu.getTrackedCapacity() > 0) {
            guiGraphics.renderTooltip(font,
                    Component.literal(menu.getTrackedEnergy() + "/" + menu.getTrackedCapacity() + getEnergyUnitSuffix()),
                    mouseX,
                    mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0xFF1F2A2E, false);
        guiGraphics.drawString(font, Component.literal("Slot: " + (menu.getSelectedDeviceSlot() + 1) + "/16  Registered: " + menu.getRegisteredDeviceCount()), 12, 24, 0xFFF3EBDD, false);
        guiGraphics.drawString(font, Component.literal("Selected IP: " + getSelectedRegistrationText()), 12, 36, 0xFFF3EBDD, false);
        guiGraphics.drawString(font, Component.literal("Status: " + getSelectedStatusText()), 12, 48, 0xFFF3EBDD, false);
        guiGraphics.drawString(font, Component.literal("Device: " + getAttachedDeviceName()), 12, 60, 0xFFF3EBDD, false);
        guiGraphics.drawString(font, Component.literal("Control: ").append(getControlStateLabel()), 12, 72, 0xFFF3EBDD, false);
        guiGraphics.drawString(font, Component.literal(getEnergyLabel()), 12, 84, 0xFFF3EBDD, false);
        guiGraphics.drawString(font, Component.literal(getCapacityLabel()), 12, 96, 0xFFF3EBDD, false);
        guiGraphics.drawString(font, Component.literal("Target IP: " + DeviceAddress.toString(menu.getPendingDeviceIp())), 12, 160, 0xFF1F2A2E, false);
        guiGraphics.drawString(font, Component.literal("J-Box IP: " + DeviceAddress.toString(menu.getIpAddress())), 164, 160, 0xFF1F2A2E, false);
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
            case JboxBlockEntity.DEVICE_SOLAR -> Component.literal(menu.isGeneratorEnabled() ? "Disable Panel" : "Enable Panel");
            default -> Component.literal(menu.getSelectedDeviceRegistration() == 0 ? "No Device Selected" : "Device Offline");
        };
    }

    private String getAttachedDeviceName() {
        return switch (menu.getAttachedDeviceType()) {
            case JboxBlockEntity.DEVICE_BATTERY -> "Basic Battery";
            case JboxBlockEntity.DEVICE_GENERATOR -> "Coal Generator";
            case JboxBlockEntity.DEVICE_SOLAR -> "Solar Panel";
            default -> menu.getSelectedDeviceRegistration() == 0 ? "None" : "Unreachable";
        };
    }

    private Component getControlStateLabel() {
        return switch (menu.getAttachedDeviceType()) {
            case JboxBlockEntity.DEVICE_BATTERY -> menu.getBatteryChargeMode().getDisplayName();
            case JboxBlockEntity.DEVICE_GENERATOR -> Component.literal(menu.isGeneratorEnabled() ? "ON" : "OFF");
            case JboxBlockEntity.DEVICE_SOLAR -> Component.literal(menu.isGeneratorEnabled() ? "ON" : "OFF");
            default -> Component.literal(menu.getSelectedDeviceRegistration() == 0 ? "Unassigned" : "Unavailable");
        };
    }

    private String getEnergyLabel() {
        return switch (menu.getAttachedDeviceType()) {
            case JboxBlockEntity.DEVICE_GENERATOR -> "Output: " + menu.getTrackedEnergy() + " FE/t";
            case JboxBlockEntity.DEVICE_SOLAR -> "Output: " + menu.getTrackedEnergy() + " FE/t";
            case JboxBlockEntity.DEVICE_BATTERY -> "Stored: " + menu.getTrackedEnergy() + " FE";
            default -> "Energy: --";
        };
    }

    private String getCapacityLabel() {
        return switch (menu.getAttachedDeviceType()) {
            case JboxBlockEntity.DEVICE_GENERATOR -> "Max Output: " + menu.getTrackedCapacity() + " FE/t";
            case JboxBlockEntity.DEVICE_SOLAR -> "Max Output: " + menu.getTrackedCapacity() + " FE/t";
            case JboxBlockEntity.DEVICE_BATTERY -> "Capacity: " + menu.getTrackedCapacity() + " FE";
            default -> "Capacity: --";
        };
    }

    private String getSelectedRegistrationText() {
        return menu.getSelectedDeviceRegistration() == 0
                ? "Empty"
                : DeviceAddress.toString(menu.getSelectedDeviceRegistration());
    }

    private String getSelectedStatusText() {
        if (menu.getSelectedDeviceRegistration() == 0) {
            return "Empty slot";
        }

        return menu.isTrackedDeviceOnline() ? "Online" : "Offline or not reachable";
    }

    private String getEnergyUnitSuffix() {
        return menu.getAttachedDeviceType() == JboxBlockEntity.DEVICE_GENERATOR || menu.getAttachedDeviceType() == JboxBlockEntity.DEVICE_SOLAR
                ? " FE/t"
                : " FE";
    }

    private void addIpButtons(int baseX, int baseY, int decrementBase, int incrementBase) {
        for (int octetIndex = 0; octetIndex < DeviceAddress.OCTET_COUNT; octetIndex++) {
            int buttonX = leftPos + baseX + octetIndex * 18;
            int decrementId = decrementBase + octetIndex;
            int incrementId = incrementBase + octetIndex;

            addRenderableWidget(Button.builder(Component.literal("+"), button -> pressMenuButton(incrementId))
                    .bounds(buttonX, topPos + baseY, 16, 16)
                    .build());
            addRenderableWidget(Button.builder(Component.literal("-"), button -> pressMenuButton(decrementId))
                    .bounds(buttonX, topPos + baseY + 18, 16, 16)
                    .build());
        }
    }
}