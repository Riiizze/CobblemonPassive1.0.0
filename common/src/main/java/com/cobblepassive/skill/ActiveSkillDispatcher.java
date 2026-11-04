package com.cobblepassive.skill;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Unified server-side router for Pokémon active skills triggered by the universal Active Skill keybind.
 */
public final class ActiveSkillDispatcher {

    private ActiveSkillDispatcher() {
    }

    /**
     * Dispatches the active skill to the currently sent-out Pokémon in the player's party.
     */
    public static void activate(ServerPlayer player) {
        if (player == null || player.isSpectator()) {
            return;
        }

        // Ignore skill activation while player is participating in an active Cobblemon battle
        try {
            if (com.cobblemon.mod.common.Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null) {
                return;
            }
        } catch (Throwable ignored) {
        }

        // 1. Excadrill - Drill
        if (ExcadrillDrillSkill.findSentOutExcadrill(player) != null) {
            ExcadrillDrillSkill.activate(player);
            return;
        }

        // 2. Meganium - Floral Breath (9x9 Bonemeal)
        if (MeganiumFloralSkill.findSentOutMeganium(player) != null) {
            MeganiumFloralSkill.activate(player);
            return;
        }

        // 3. Rayquaza - Air Lock (Clear Weather)
        if (RayquazaWeatherSkill.findSentOutRayquaza(player) != null) {
            RayquazaWeatherSkill.activate(player);
            return;
        }

        // 4. Rillaboom - Grassy Terrain (4x4 Heal 1HP/s for 10s)
        if (RillaboomTerrainSkill.findSentOutRillaboom(player) != null) {
            RillaboomTerrainSkill.activate(player);
            return;
        }

        // Fallback: player pressed the key without any active skill Pokémon sent out
        player.displayClientMessage(
                Component.translatable("message.cobblepassive.no_active_skill_pokemon")
                        .withStyle(ChatFormatting.RED),
                true
        );
        com.cobblepassive.platform.Services.PLATFORM.sendToPlayer(player, new com.cobblepassive.network.ActiveSkillCooldownPayload(0L, 0L));
    }
}
