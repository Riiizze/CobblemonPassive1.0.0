package com.cobblepassive.client;

import com.cobblepassive.PassiveConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CobblePassiveUiConfigScreen extends Screen {
    private final Screen parent;
    private final PassiveConfig.UiSettings temp;
    private String statusMessage = "";
    private int statusTimer = 0;

    public CobblePassiveUiConfigScreen(Screen parent) {
        super(Component.literal("CobblePassive UI Editor"));
        this.parent = parent;
        this.temp = PassiveConfig.ui.copy();
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
        int startY = 32;
        int rowH = 19;
        int controlX = this.width / 2 + 10;

        this.createStepperRow(controlX, startY, "-5", "-1", "+1", "+5", 
            () -> this.temp.iconOffsetX -= 5, 
            () -> --this.temp.iconOffsetX, 
            () -> ++this.temp.iconOffsetX, 
            () -> this.temp.iconOffsetX += 5);

        this.createStepperRow(controlX, startY + rowH, "-5", "-1", "+1", "+5", 
            () -> this.temp.iconOffsetY -= 5, 
            () -> --this.temp.iconOffsetY, 
            () -> ++this.temp.iconOffsetY, 
            () -> this.temp.iconOffsetY += 5);

        this.createStepperRow(controlX, startY + rowH * 2, "-2", "-1", "+1", "+2", 
            () -> this.temp.iconSize = Math.max(8, this.temp.iconSize - 2), 
            () -> this.temp.iconSize = Math.max(8, this.temp.iconSize - 1), 
            () -> ++this.temp.iconSize, 
            () -> this.temp.iconSize += 2);

        this.createStepperRow(controlX, startY + rowH * 3, "-5", "-1", "+1", "+5", 
            () -> this.temp.hudOffsetX -= 5, 
            () -> --this.temp.hudOffsetX, 
            () -> ++this.temp.hudOffsetX, 
            () -> this.temp.hudOffsetX += 5);

        this.createStepperRow(controlX, startY + rowH * 4, "-5", "-1", "+1", "+5", 
            () -> this.temp.hudOffsetY -= 5, 
            () -> --this.temp.hudOffsetY, 
            () -> ++this.temp.hudOffsetY, 
            () -> this.temp.hudOffsetY += 5);

        this.createStepperRow(controlX, startY + rowH * 5, "-10", "-2", "+2", "+10", 
            () -> this.temp.hudWidth = Math.max(100, this.temp.hudWidth - 10), 
            () -> this.temp.hudWidth = Math.max(100, this.temp.hudWidth - 2), 
            () -> this.temp.hudWidth += 2, 
            () -> this.temp.hudWidth += 10);

        this.createStepperRow(controlX, startY + rowH * 6, "-10", "-2", "+2", "+10", 
            () -> this.temp.hudMinHeight = Math.max(40, this.temp.hudMinHeight - 10), 
            () -> this.temp.hudMinHeight = Math.max(40, this.temp.hudMinHeight - 2), 
            () -> this.temp.hudMinHeight += 2, 
            () -> this.temp.hudMinHeight += 10);

        this.createStepperRow(controlX, startY + rowH * 7, "-0.1", "-0.05", "+0.05", "+0.1", 
            () -> this.temp.fontScale = Math.max(0.5f, (float)Math.round((this.temp.fontScale - 0.1f) * 100.0f) / 100.0f), 
            () -> this.temp.fontScale = Math.max(0.5f, (float)Math.round((this.temp.fontScale - 0.05f) * 100.0f) / 100.0f), 
            () -> this.temp.fontScale = Math.min(2.0f, (float)Math.round((this.temp.fontScale + 0.05f) * 100.0f) / 100.0f), 
            () -> this.temp.fontScale = Math.min(2.0f, (float)Math.round((this.temp.fontScale + 0.1f) * 100.0f) / 100.0f));

        this.createStepperRow(controlX, startY + rowH * 8, "-500", "-100", "+100", "+500", 
            () -> this.temp.hudLayer = Math.max(0, this.temp.hudLayer - 500), 
            () -> this.temp.hudLayer = Math.max(0, this.temp.hudLayer - 100), 
            () -> this.temp.hudLayer += 100, 
            () -> this.temp.hudLayer += 500);

        int btnY = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.literal("§a✔ Save Config"), btn -> {
            PassiveConfig.saveUiSettings(this.temp);
            this.statusMessage = "§a✔ Successfully saved to cobblepassive.json!";
            this.statusTimer = 80;
        }).bounds(this.width / 2 - 160, btnY, 130, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("§c↺ Reset"), btn -> {
            this.temp.reset();
            this.init();
            this.statusMessage = "§eSettings reset to default.";
            this.statusTimer = 60;
        }).bounds(this.width / 2 - 20, btnY, 70, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("§fBack"), btn -> this.onClose())
            .bounds(this.width / 2 + 60, btnY, 80, 20).build());
    }

    private void createStepperRow(int x, int y, String labelSub2, String labelSub1, String labelAdd1, String labelAdd2, 
                                  Runnable sub2, Runnable sub1, Runnable add1, Runnable add2) {
        int btnW = 26;
        int btnH = 17;
        this.addRenderableWidget(Button.builder(Component.literal(labelSub2), b -> { sub2.run(); this.init(); }).bounds(x, y, btnW, btnH).build());
        this.addRenderableWidget(Button.builder(Component.literal(labelSub1), b -> { sub1.run(); this.init(); }).bounds(x + 28, y, btnW, btnH).build());
        this.addRenderableWidget(Button.builder(Component.literal(labelAdd1), b -> { add1.run(); this.init(); }).bounds(x + 104, y, btnW, btnH).build());
        this.addRenderableWidget(Button.builder(Component.literal(labelAdd2), b -> { add2.run(); this.init(); }).bounds(x + 132, y, btnW, btnH).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, Component.literal("§6⚙ [CobblePassive] UI Position & Scale Editor"), this.width / 2, 10, 0xFFFFFF);

        int previewAreaX = 16;
        int previewAreaY = 30;
        int previewAreaW = this.width / 2 - 26;
        int previewAreaH = this.height - 65;

        context.fill(previewAreaX, previewAreaY, previewAreaX + previewAreaW, previewAreaY + previewAreaH, -871295712);
        context.renderOutline(previewAreaX, previewAreaY, previewAreaW, previewAreaH, -13286043);
        context.drawString(this.font, Component.literal("§b✦ Live Preview"), previewAreaX + 8, previewAreaY + 5, 0x55FFFF);

        int simPortraitX = previewAreaX + 14;
        int simPortraitY = previewAreaY + 28;
        int simPortraitSize = 66;

        context.fill(simPortraitX, simPortraitY, simPortraitX + simPortraitSize, simPortraitY + simPortraitSize, -14012872);
        context.renderOutline(simPortraitX, simPortraitY, simPortraitSize, simPortraitSize, -11183256);
        context.drawCenteredString(this.font, Component.literal("§7[3D Portrait]"), simPortraitX + 33, simPortraitY + 28, 0x888888);

        int iconX = simPortraitX + this.temp.iconOffsetX;
        int iconY = simPortraitY + this.temp.iconOffsetY;
        context.blit(PassiveTooltipHelper.ICON_TEXTURE, iconX, iconY, this.temp.iconSize, this.temp.iconSize, 0.0f, 0.0f, 50, 48, 50, 48);

        int hudX = iconX + this.temp.hudOffsetX;
        int hudY = iconY + this.temp.hudOffsetY;
        PassiveTooltipHelper.renderCustomHudBox(context, this.font, "Web Immunity", "Grants complete immunity to Cobweb slowness. You can walk, sprint, and jump freely through webs without hindrance.", hudX, hudY, this.temp.hudWidth, this.temp.hudMinHeight, this.temp.hudBgColor, this.temp.hudLayer, this.temp.fontScale, true, false);

        int startY = 32;
        int rowH = 19;
        int controlX = this.width / 2 + 10;
        this.renderRowLabel(context, controlX, startY, "Icon Offset X:", this.temp.iconOffsetX + " px");
        this.renderRowLabel(context, controlX, startY + rowH, "Icon Offset Y:", this.temp.iconOffsetY + " px");
        this.renderRowLabel(context, controlX, startY + rowH * 2, "Icon Size:", this.temp.iconSize + " px");
        this.renderRowLabel(context, controlX, startY + rowH * 3, "HUD Offset X:", this.temp.hudOffsetX + " px");
        this.renderRowLabel(context, controlX, startY + rowH * 4, "HUD Offset Y:", this.temp.hudOffsetY + " px");
        this.renderRowLabel(context, controlX, startY + rowH * 5, "HUD Box Width:", this.temp.hudWidth + " px");
        this.renderRowLabel(context, controlX, startY + rowH * 6, "HUD Min Height:", this.temp.hudMinHeight + " px");
        this.renderRowLabel(context, controlX, startY + rowH * 7, "Font Size:", String.format("%.2fx", Float.valueOf(this.temp.fontScale)));
        this.renderRowLabel(context, controlX, startY + rowH * 8, "Z-Layer (Depth):", this.temp.hudLayer + " Z");

        if (this.statusTimer > 0) {
            --this.statusTimer;
            context.drawCenteredString(this.font, Component.literal(this.statusMessage), this.width / 2, this.height - 44, 0xFFFFFF);
        }
    }

    private void renderRowLabel(GuiGraphics context, int x, int y, String title, String val) {
        context.drawString(this.font, Component.literal("§7" + title), x + 162, y + 4, 0xCCCCCC);
        context.drawCenteredString(this.font, Component.literal("§e" + val), x + 79, y + 4, 0xFFFF55);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
