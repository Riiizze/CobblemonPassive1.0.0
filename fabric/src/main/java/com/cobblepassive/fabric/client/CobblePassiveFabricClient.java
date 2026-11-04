package com.cobblepassive.fabric.client;

import com.cobblepassive.CobblePassive;
import com.cobblepassive.client.ActiveSkillHudOverlay;
import com.cobblepassive.client.CobblePassiveClient;
import com.cobblepassive.network.ActiveSkillCooldownPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class CobblePassiveFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(CobblePassiveClient.ACTIVE_SKILL_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(CobblePassiveClient::onClientTick);

        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            ActiveSkillHudOverlay.render(guiGraphics, deltaTracker);
        });

        ClientPlayNetworking.registerGlobalReceiver(ActiveSkillCooldownPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> ActiveSkillHudOverlay.setCooldown(payload.cooldownRemainingMs(), payload.totalCooldownMs()));
        });

        CobblePassive.LOGGER.info("[CobblePassive-Client] Client Renderer & Summary UI Overlay Initialized (Fabric)!");
    }
}
