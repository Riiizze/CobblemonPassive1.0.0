package com.cobblepassive.skill;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.activestate.ActivePokemonState;
import com.cobblemon.mod.common.pokemon.activestate.InactivePokemonState;
import com.cobblemon.mod.common.pokemon.activestate.PokemonState;
import com.cobblemon.mod.common.pokemon.activestate.SentOutState;
import com.cobblepassive.CobblePassive;
import com.cobblepassive.PassiveConfig;
import com.cobblepassive.network.ActiveSkillCooldownPayload;
import com.cobblepassive.platform.Services;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

/**
 * Handles the server-side execution of Rayquaza's Active Skill ("Air Lock").
 * Eliminates rain, storms, and thunderstorms across the skies, locking in clear weather.
 */
public final class RayquazaWeatherSkill {

    public static final long DEFAULT_COOLDOWN_MS = 60_000L;
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    private RayquazaWeatherSkill() {
    }

    public static long getCooldownMs() {
        return PassiveConfig.getActiveSkillCooldownMs("rayquaza", DEFAULT_COOLDOWN_MS);
    }

    /**
     * Checks if the player has Rayquaza sent out in the world and returns the Pokemon instance.
     */
    public static Pokemon findSentOutRayquaza(ServerPlayer player) {
        if (player == null) {
            return null;
        }

        try {
            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
            if (party == null) {
                return null;
            }

            for (Pokemon pokemon : party) {
                if (pokemon == null || pokemon.isFainted()) {
                    continue;
                }

                String species = pokemon.getSpecies() != null ? pokemon.getSpecies().getName().toLowerCase() : "";
                if ("rayquaza".equals(species) && isSentOut(pokemon, player.serverLevel())) {
                    return pokemon;
                }
            }
        } catch (Throwable t) {
            CobblePassive.LOGGER.error("[CobblePassive] Error finding sent-out Rayquaza for player: " + player.getName().getString(), t);
        }
        return null;
    }

    /**
     * Robust sent-out verification across Cobblemon state, entity reference, and level entities.
     */
    public static boolean isSentOut(Pokemon pokemon, ServerLevel level) {
        if (pokemon == null || pokemon.isFainted()) {
            return false;
        }

        try {
            PokemonState state = pokemon.getState();
            if (state != null) {
                if (state instanceof InactivePokemonState) {
                    return false;
                }
                if (state instanceof SentOutState || state instanceof ActivePokemonState) {
                    return true;
                }
                String name = state.getName();
                if (name != null && (name.equalsIgnoreCase("sentout") || name.equalsIgnoreCase("active"))) {
                    return true;
                }
                if (state.getIcon(pokemon) != null) {
                    return true;
                }
            }
        } catch (Throwable ignored) {
        }

        try {
            if (pokemon.getEntity() != null && !pokemon.getEntity().isRemoved()) {
                return true;
            }
        } catch (Throwable ignored) {
        }

        try {
            if (level != null) {
                for (Entity entity : level.getAllEntities()) {
                    if (entity instanceof PokemonEntity pe) {
                        if (pokemon.getUuid().equals(pe.getPokemon().getUuid())) {
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return false;
    }

    /**
     * Attempts to find the living PokemonEntity representing Rayquaza in the server level.
     */
    public static PokemonEntity findRayquazaEntity(ServerPlayer player, Pokemon pokemon) {
        if (pokemon == null || player == null) {
            return null;
        }

        try {
            if (pokemon.getEntity() != null && !pokemon.getEntity().isRemoved()) {
                return pokemon.getEntity();
            }
        } catch (Throwable ignored) {
        }

        try {
            ServerLevel level = player.serverLevel();
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof PokemonEntity pe) {
                    if (pokemon.getUuid().equals(pe.getPokemon().getUuid())) {
                        return pe;
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return null;
    }

    /**
     * Activates Rayquaza's Air Lock active skill to clear rain and thunderstorms.
     */
    public static void activate(ServerPlayer player) {
        if (player == null || player.isSpectator()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        Pokemon rayquaza = findSentOutRayquaza(player);

        if (rayquaza == null) {
            player.displayClientMessage(
                    Component.translatable("message.cobblepassive.rayquaza.not_sent_out")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        // Cooldown check
        long now = System.currentTimeMillis();
        long cooldownMs = getCooldownMs();
        Long cooldownEnd = COOLDOWNS.get(player.getUUID());
        if (cooldownEnd != null && now < cooldownEnd) {
            long remainingMs = cooldownEnd - now;
            int remainingSec = (int) Math.ceil(remainingMs / 1000.0D);
            player.displayClientMessage(
                    Component.translatable("message.cobblepassive.rayquaza.cooldown", remainingSec)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        // Apply Cooldown and sync with client HUD
        COOLDOWNS.put(player.getUUID(), now + cooldownMs);
        Services.PLATFORM.sendToPlayer(player, new ActiveSkillCooldownPayload(cooldownMs, cooldownMs));

        // Rayquaza cry and sound feedback
        PokemonEntity rayquazaEntity = findRayquazaEntity(player, rayquaza);
        if (rayquazaEntity != null) {
            try {
                java.lang.reflect.Method cryMethod = rayquazaEntity.getClass().getMethod("cry");
                cryMethod.invoke(rayquazaEntity);
            } catch (Throwable ignored) {
            }
        }

        // Clear weather for 12,000 ticks (10 minutes)
        level.setWeatherParameters(12000, 0, false, false);

        // Skyward vortex of Dragon Breath and Cloud particles
        for (int i = 0; i < 40; i++) {
            double px = player.getX() + (level.random.nextDouble() - 0.5) * 8.0;
            double py = player.getY() + 1.0 + level.random.nextDouble() * 4.0;
            double pz = player.getZ() + (level.random.nextDouble() - 0.5) * 8.0;
            level.sendParticles(ParticleTypes.CLOUD, px, py, pz, 1, 0.0, 0.2, 0.0, 0.05);
            level.sendParticles(ParticleTypes.DRAGON_BREATH, px, py, pz, 1, 0.0, 0.1, 0.0, 0.02);
        }

        // Audio effects: atmospheric roar, beacon activation, and thunder fading
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.7F, 1.4F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 1.2F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 0.5F, 1.8F);

        // Actionbar notification
        player.displayClientMessage(
                Component.translatable("message.cobblepassive.rayquaza.activated")
                        .withStyle(ChatFormatting.AQUA),
                true
        );
    }
}
