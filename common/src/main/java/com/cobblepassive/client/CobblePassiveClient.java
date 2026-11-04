package com.cobblepassive.client;

import com.cobblepassive.network.ActiveSkillPayload;
import com.cobblepassive.platform.Services;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Common client-side keybind definitions and client tick input dispatcher for CobblePassive active skills.
 */
public final class CobblePassiveClient {

    public static final String KEY_CATEGORY = "key.categories.cobblepassive";
    public static final String KEY_ACTIVE_SKILL_NAME = "key.cobblepassive.active_skill";
    public static final String KEY_EXCADRILL_SKILL_NAME = KEY_ACTIVE_SKILL_NAME;

    /**
     * Unified Keybind to trigger the active skill of whichever Pokémon is sent out (Default: V, configurable in Controls menu).
     */
    public static final KeyMapping ACTIVE_SKILL_KEY = new KeyMapping(
            KEY_ACTIVE_SKILL_NAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KEY_CATEGORY
    );

    public static final KeyMapping EXCADRILL_SKILL_KEY = ACTIVE_SKILL_KEY;

    private CobblePassiveClient() {
    }

    /**
     * Polls registered keybindings each client tick and dispatches C2S skill packets when pressed.
     */
    public static void onClientTick(Minecraft client) {
        if (client.player == null || client.level == null || client.getConnection() == null) {
            return;
        }
        while (ACTIVE_SKILL_KEY.consumeClick()) {
            if (client.screen == null) {
                try {
                    ActiveSkillHudOverlay.ActiveSkillType activeSkill = ActiveSkillHudOverlay.getSentOutActiveSkill();
                    Services.PLATFORM.sendToServer(new ActiveSkillPayload());
                    if (activeSkill != null) {
                        ActiveSkillHudOverlay.onLocalSkillTriggered();
                    }
                } catch (Throwable t) {
                    com.cobblepassive.CobblePassive.LOGGER.error("[CobblePassive-Client] Failed to send ActiveSkillPayload", t);
                }
            }
        }
    }
}
