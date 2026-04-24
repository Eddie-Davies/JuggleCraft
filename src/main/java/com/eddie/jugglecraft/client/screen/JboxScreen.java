package com.eddie.jugglecraft.client.screen;

import com.eddie.jugglecraft.block.entity.JboxBlockEntity;
import com.eddie.jugglecraft.logic.DeviceAddress;
import com.eddie.jugglecraft.menu.JboxMenu;
import com.eddie.jugglecraft.network.JboxSetIpPacket;
import com.eddie.jugglecraft.network.ModMessages;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class JboxScreen extends AbstractContainerScreen<JboxMenu> {
    private static final int CARD_COUNT = JboxBlockEntity.MAX_CONNECTED_DEVICES;
    private static final int VISIBLE_CARD_COUNT = 2;
    private static final int CARD_HEIGHT = 82;
    private static final int CARD_GAP = 10;

    private final EditBox[] slotIpBoxes = new EditBox[CARD_COUNT];
    private final Button[] slotControlButtons = new Button[CARD_COUNT];
    private EditBox localIpBox;
    private int scrollIndex;

    public JboxScreen(JboxMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 362;
        imageHeight = 340;
        inventoryLabelX = 12;
        inventoryLabelY = 246;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 14;
        titleLabelY = 12;

        localIpBox = new EditBox(font, leftPos + 104, topPos + 20, 138, 18, Component.literal("J-Box IP"));
        localIpBox.setMaxLength(15);
        localIpBox.setValue(DeviceAddress.toEditableString(menu.getBlockEntity().getIpAddress()));
        localIpBox.setResponder(text -> sendIpUpdate(-1, text));
        addRenderableWidget(localIpBox);

        for (int slot = 0; slot < CARD_COUNT; slot++) {
            int finalSlot = slot;
            EditBox editBox = new EditBox(font, leftPos + 28, topPos + 70, 120, 18, Component.literal("Device IP"));
            editBox.setMaxLength(15);
            editBox.setValue(DeviceAddress.toEditableString(menu.getBlockEntity().getSlotIp(slot)));
            editBox.setResponder(text -> sendIpUpdate(finalSlot, text));
            slotIpBoxes[slot] = addRenderableWidget(editBox);

            Button controlButton = Button.builder(Component.literal("Control"), button -> pressMenuButton(JboxMenu.CONTROL_SLOT_BUTTON_BASE + finalSlot))
                    .bounds(leftPos + imageWidth - 102, topPos + 66, 82, 20)
                    .build();
            slotControlButtons[slot] = addRenderableWidget(controlButton);
        }

        updateScrollLayout();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        JboxBlockEntity blockEntity = menu.getBlockEntity();

        if (localIpBox != null && !localIpBox.isFocused()) {
            syncEditBox(localIpBox, DeviceAddress.toEditableString(blockEntity.getIpAddress()));
        }

        for (int slot = 0; slot < CARD_COUNT; slot++) {
            if (!slotIpBoxes[slot].isFocused()) {
                syncEditBox(slotIpBoxes[slot], DeviceAddress.toEditableString(blockEntity.getSlotIp(slot)));
            }

            slotControlButtons[slot].setMessage(getSlotControlLabel(slot));
            slotControlButtons[slot].active = slotControlButtons[slot].visible
                    && blockEntity.isSlotOnline(slot)
                    && blockEntity.getSlotDeviceType(slot) != JboxBlockEntity.DEVICE_NONE;
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF6F8488);
        guiGraphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFF2EDE0);
        guiGraphics.fill(x + 10, y + 10, x + imageWidth - 10, y + 50, 0xFF1E2A2D);
        guiGraphics.fill(x + 10, y + 58, x + imageWidth - 10, y + 232, 0xFFD8D0BF);
        guiGraphics.fill(x + 10, y + 242, x + imageWidth - 10, y + imageHeight - 10, 0xFFCEC7B8);

        guiGraphics.drawString(font, title, x + titleLabelX, y + titleLabelY, 0xFFF2EDE0, false);
        guiGraphics.drawString(font, Component.literal("Controller IP"), x + 18, y + 24, 0xFFF2EDE0, false);
        guiGraphics.drawString(font, Component.literal("Configured: " + menu.getBlockEntity().getRegisteredDeviceCount() + "/16"), x + 254, y + 18, 0xFFD8D0BF, false);
        guiGraphics.drawString(font, Component.literal("Online: " + countOnlineSlots()), x + 254, y + 32, 0xFFD8D0BF, false);

        for (int visibleIndex = 0; visibleIndex < VISIBLE_CARD_COUNT; visibleIndex++) {
            int slot = scrollIndex + visibleIndex;
            if (slot >= CARD_COUNT) {
                continue;
            }

            renderDeviceCard(guiGraphics, x + 18, y + 66 + visibleIndex * (CARD_HEIGHT + CARD_GAP), slot);
        }

        renderScrollBar(guiGraphics, x + imageWidth - 20, y + 66);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
    }

    private void pressMenuButton(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxScroll = Math.max(0, CARD_COUNT - VISIBLE_CARD_COUNT);
        if (maxScroll <= 0) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }

        int nextScroll = scrollIndex - (int) Math.signum(delta);
        nextScroll = Math.max(0, Math.min(maxScroll, nextScroll));
        if (nextScroll != scrollIndex) {
            scrollIndex = nextScroll;
            updateScrollLayout();
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void updateScrollLayout() {
        for (int slot = 0; slot < CARD_COUNT; slot++) {
            int visibleIndex = slot - scrollIndex;
            boolean visible = visibleIndex >= 0 && visibleIndex < VISIBLE_CARD_COUNT;
            int cardY = topPos + 66 + visibleIndex * (CARD_HEIGHT + CARD_GAP);

            slotIpBoxes[slot].visible = visible;
            slotIpBoxes[slot].active = visible;
            slotIpBoxes[slot].setX(leftPos + 96);
            slotIpBoxes[slot].setY(cardY + 10);

            slotControlButtons[slot].visible = visible;
            slotControlButtons[slot].active = visible;
            slotControlButtons[slot].setX(leftPos + imageWidth - 102);
            slotControlButtons[slot].setY(cardY + 10);
        }
    }

    private void renderDeviceCard(GuiGraphics guiGraphics, int cardX, int cardY, int slot) {
        int cardWidth = imageWidth - 42;
        int cardBottom = cardY + CARD_HEIGHT;
        JboxBlockEntity blockEntity = menu.getBlockEntity();
        boolean online = blockEntity.isSlotOnline(slot);
        int deviceType = blockEntity.getSlotDeviceType(slot);

        guiGraphics.fill(cardX, cardY, cardX + cardWidth, cardBottom, 0xFF233238);
        guiGraphics.fill(cardX + 1, cardY + 1, cardX + cardWidth - 1, cardBottom - 1, 0xFFF8F2E6);
        guiGraphics.fill(cardX + 10, cardY + 26, cardX + cardWidth - 12, cardBottom - 10, 0xFFE3DBCB);

        guiGraphics.drawString(font, Component.literal("Device Slot " + (slot + 1)), cardX + 12, cardY + 8, 0xFF233238, false);
        guiGraphics.drawString(font, Component.literal(online ? "ONLINE" : getSlotStatusText(slot)), cardX + 12, cardY + 22, online ? 0xFF2E7D32 : 0xFF8A4B2F, false);
        guiGraphics.drawString(font, Component.literal(getSlotTypeName(slot)), cardX + 188, cardY + 8, 0xFF233238, false);
        guiGraphics.drawString(font, Component.literal("IP"), cardX + 78, cardY + 14, 0xFF233238, false);

        guiGraphics.drawString(font, Component.literal(formatPowerLabel(slot)), cardX + 12, cardY + 34, getPowerColor(slot), false);
        guiGraphics.drawString(font, Component.literal(formatEnergySummary(slot)), cardX + 12, cardY + 48, 0xFF233238, false);
        guiGraphics.drawString(font, Component.literal(formatDetailLine(slot)), cardX + 12, cardY + 62, 0xFF233238, false);
    }

    private void renderScrollBar(GuiGraphics guiGraphics, int barX, int barY) {
        int trackHeight = VISIBLE_CARD_COUNT * CARD_HEIGHT + (VISIBLE_CARD_COUNT - 1) * CARD_GAP;
        int maxScroll = Math.max(0, CARD_COUNT - VISIBLE_CARD_COUNT);
        guiGraphics.fill(barX, barY, barX + 8, barY + trackHeight, 0xFFB7AF9F);

        int thumbHeight = Math.max(18, trackHeight * VISIBLE_CARD_COUNT / CARD_COUNT);
        int thumbTravel = trackHeight - thumbHeight;
        int thumbOffset = maxScroll == 0 ? 0 : thumbTravel * scrollIndex / maxScroll;
        guiGraphics.fill(barX + 1, barY + thumbOffset + 1, barX + 7, barY + thumbOffset + thumbHeight - 1, 0xFF556468);
    }

    private void sendIpUpdate(int slotIndex, String text) {
        Integer parsed = DeviceAddress.tryParse(text);
        if (parsed == null) {
            return;
        }

        int currentValue = slotIndex < 0 ? menu.getBlockEntity().getIpAddress() : menu.getBlockEntity().getSlotIp(slotIndex);
        if (currentValue == parsed) {
            return;
        }

        ModMessages.CHANNEL.sendToServer(new JboxSetIpPacket(menu.getBlockEntity().getBlockPos(), slotIndex, parsed));
    }

    private void syncEditBox(EditBox editBox, String value) {
        if (!editBox.getValue().equals(value)) {
            editBox.setValue(value);
        }
    }

    private int countOnlineSlots() {
        int online = 0;
        for (int slot = 0; slot < CARD_COUNT; slot++) {
            if (menu.getBlockEntity().isSlotOnline(slot)) {
                online++;
            }
        }
        return online;
    }

    private Component getSlotControlLabel(int slot) {
        return switch (menu.getBlockEntity().getSlotDeviceType(slot)) {
            case JboxBlockEntity.DEVICE_BATTERY -> Component.literal("Cycle Mode");
            case JboxBlockEntity.DEVICE_GENERATOR -> Component.literal(menu.getBlockEntity().getSlotControlState(slot) != 0 ? "Turn Off" : "Turn On");
            case JboxBlockEntity.DEVICE_SOLAR -> Component.literal(menu.getBlockEntity().getSlotControlState(slot) != 0 ? "Disable" : "Enable");
            default -> Component.literal("No Device");
        };
    }

    private String getSlotTypeName(int slot) {
        return switch (menu.getBlockEntity().getSlotDeviceType(slot)) {
            case JboxBlockEntity.DEVICE_BATTERY -> "Basic Battery";
            case JboxBlockEntity.DEVICE_GENERATOR -> "Coal Generator";
            case JboxBlockEntity.DEVICE_SOLAR -> "Solar Panel";
            default -> menu.getBlockEntity().getSlotIp(slot) == 0 ? "Unconfigured" : "No Reachable Device";
        };
    }

    private String getSlotStatusText(int slot) {
        return menu.getBlockEntity().getSlotIp(slot) == 0 ? "ENTER IP" : "OFFLINE";
    }

    private String formatPowerLabel(int slot) {
        int power = menu.getBlockEntity().getSlotPowerFlow(slot);
        String prefix = power > 0 ? "+" : power < 0 ? "-" : " ";
        return "Power: " + prefix + Math.abs(power) + " FE/t";
    }

    private int getPowerColor(int slot) {
        int power = menu.getBlockEntity().getSlotPowerFlow(slot);
        if (power > 0) {
            return 0xFF2E7D32;
        }
        if (power < 0) {
            return 0xFF8A4B2F;
        }
        return 0xFF233238;
    }

    private String formatEnergySummary(int slot) {
        int deviceType = menu.getBlockEntity().getSlotDeviceType(slot);
        return switch (deviceType) {
            case JboxBlockEntity.DEVICE_BATTERY -> "State of Charge: " + menu.getBlockEntity().getSlotStateOfCharge(slot) + "%   Stored: "
                    + menu.getBlockEntity().getSlotStoredEnergy(slot) + "/" + menu.getBlockEntity().getSlotCapacity(slot) + " FE";
            case JboxBlockEntity.DEVICE_GENERATOR -> "Live Output: " + menu.getBlockEntity().getSlotPowerFlow(slot) + "/" + menu.getBlockEntity().getSlotCapacity(slot) + " FE/t";
            case JboxBlockEntity.DEVICE_SOLAR -> "Buffer: " + menu.getBlockEntity().getSlotStateOfCharge(slot) + "%   Stored: "
                    + menu.getBlockEntity().getSlotStoredEnergy(slot) + "/" + menu.getBlockEntity().getSlotCapacity(slot) + " FE";
            default -> "Awaiting matching device on this RS485 graph";
        };
    }

    private String formatDetailLine(int slot) {
        int deviceType = menu.getBlockEntity().getSlotDeviceType(slot);
        return switch (deviceType) {
            case JboxBlockEntity.DEVICE_BATTERY -> "Mode: " + getBatteryModeName(menu.getBlockEntity().getSlotControlState(slot));
            case JboxBlockEntity.DEVICE_GENERATOR -> "Fuel: " + menu.getBlockEntity().getSlotPrimaryMetric(slot) + "/"
                    + menu.getBlockEntity().getSlotSecondaryMetric(slot) + " ticks  State: "
                    + (menu.getBlockEntity().getSlotControlState(slot) != 0 ? "ON" : "OFF");
            case JboxBlockEntity.DEVICE_SOLAR -> "Efficiency: " + menu.getBlockEntity().getSlotPrimaryMetric(slot) + "%   Sun: "
                    + menu.getBlockEntity().getSlotSecondaryMetric(slot) + "%   Height: "
                    + menu.getBlockEntity().getSlotTertiaryMetric(slot) + "%   Climate: "
                    + menu.getBlockEntity().getSlotQuaternaryMetric(slot) + "%";
            default -> "Type an IPv4 address to bind this device card";
        };
    }

    private String getBatteryModeName(int modeId) {
        return switch (modeId) {
            case 0 -> "Charging";
            case 1 -> "Idle";
            case 2 -> "Discharging";
            default -> "Unknown";
        };
    }
}