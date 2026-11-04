package com.cobblepassive.neoforge.client;

import com.cobblepassive.CobblePassive;
import com.cobblepassive.client.ActiveSkillHudOverlay;
import com.cobblepassive.client.CobblePassiveClient;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

public class CobblePassiveNeoForgeClient {

    public static void init() {
        CobblePassive.LOGGER.info("[CobblePassive-Client] Client Renderer & Summary UI Overlay Initialized (NeoForge)!");
    }

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(CobblePassiveNeoForgeClient::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(CobblePassiveNeoForgeClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(CobblePassiveNeoForgeClient::onRenderGui);
        init();
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CobblePassiveClient.ACTIVE_SKILL_KEY);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        CobblePassiveClient.onClientTick(Minecraft.getInstance());
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        ActiveSkillHudOverlay.render(event.getGuiGraphics(), null);
    }
}
