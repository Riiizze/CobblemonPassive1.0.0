package com.cobblepassive.fabric.client;

import com.cobblemon.mod.common.client.gui.summary.Summary;
import com.cobblepassive.client.CobblePassiveUiConfigScreen;
import com.cobblepassive.client.PassiveTooltipHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class CobblePassiveFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("cpui").executes(ctx -> {
                Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new CobblePassiveUiConfigScreen(null)));
                return 1;
            }));
            dispatcher.register(ClientCommandManager.literal("cobblepassiveui").executes(ctx -> {
                Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new CobblePassiveUiConfigScreen(null)));
                return 1;
            }));
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof Summary) {
                ScreenMouseEvents.allowMouseClick(screen).register((scr, mouseX, mouseY, button) ->
                        !PassiveTooltipHelper.handleMouseClicked(mouseX, mouseY, button)
                );
            }
        });
    }
}
