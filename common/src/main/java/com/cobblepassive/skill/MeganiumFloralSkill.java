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
import com.cobblepassive.network.ActiveSkillCooldownPayload;
import com.cobblepassive.platform.Services;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Handles the server-side execution of Meganium's Active Skill ("Floral Breath").
 * Applies bonemeal growth to all valid crops in a 9x9 horizontal area around the player
 * (matching the full standard Minecraft farmland plot hydrated by 1 water block).
 */
public final class MeganiumFloralSkill {

    public static final long DEFAULT_COOLDOWN_MS = 30_000L;
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    public static long getCooldownMs() {
        return com.cobblepassive.PassiveConfig.getActiveSkillCooldownMs("meganium", DEFAULT_COOLDOWN_MS);
    }

    private MeganiumFloralSkill() {
    }

    /**
     * Checks if the player has Meganium sent out in the world and returns the Pokemon instance.
     */
    public static Pokemon findSentOutMeganium(ServerPlayer player) {
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
                if ("meganium".equals(species) && isSentOut(pokemon, player.serverLevel())) {
                    return pokemon;
                }
            }
        } catch (Throwable t) {
            CobblePassive.LOGGER.error("[CobblePassive] Error finding sent-out Meganium for player: " + player.getName().getString(), t);
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
     * Attempts to find the living PokemonEntity representing Meganium in the server level.
     */
    public static PokemonEntity findMeganiumEntity(ServerPlayer player, Pokemon pokemon) {
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
     * Activates Meganium's Floral Breath active skill.
     */
    public static void activate(ServerPlayer player) {
        if (player == null || player.isSpectator()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        Pokemon meganium = findSentOutMeganium(player);

        if (meganium == null) {
            player.displayClientMessage(
                    Component.translatable("message.cobblepassive.meganium.not_sent_out")
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
                    Component.translatable("message.cobblepassive.meganium.cooldown", remainingSec)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        // Apply Cooldown
        COOLDOWNS.put(player.getUUID(), now + cooldownMs);
        Services.PLATFORM.sendToPlayer(player, new ActiveSkillCooldownPayload(cooldownMs, cooldownMs));

        // Meganium cry and sound feedback
        PokemonEntity meganiumEntity = findMeganiumEntity(player, meganium);
        if (meganiumEntity != null) {
            try {
                java.lang.reflect.Method cryMethod = meganiumEntity.getClass().getMethod("cry");
                cryMethod.invoke(meganiumEntity);
            } catch (Throwable ignored) {
            }
        }

        // 9x9 Bonemeal Area around player (matching full standard farmland hydrated by 1 water source)
        BlockPos playerPos = player.blockPosition();
        int cropsFertilized = 0;

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                for (int dy : new int[]{0, -1, 1}) {
                    BlockPos targetPos = playerPos.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(targetPos);

                    if (state.getBlock() instanceof BonemealableBlock bonemealable) {
                        if (bonemealable.isValidBonemealTarget(level, targetPos, state)) {
                            bonemealable.performBonemeal(level, level.random, targetPos, state);
                            level.levelEvent(1505, targetPos, 0); // Vanilla Bonemeal particles & sound
                            cropsFertilized++;
                            break; // Only fertilize one crop per column
                        }
                    }
                }
            }
        }

        // Visual and auditory effects: Emerald sparkles and spore petals across the 9x9 area
        for (int i = 0; i < 50; i++) {
            double px = player.getX() + (level.random.nextDouble() - 0.5) * 9.0;
            double py = player.getY() + level.random.nextDouble() * 1.5;
            double pz = player.getZ() + (level.random.nextDouble() - 0.5) * 9.0;
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
        }
        for (int i = 0; i < 25; i++) {
            double px = player.getX() + (level.random.nextDouble() - 0.5) * 9.0;
            double py = player.getY() + 0.2 + level.random.nextDouble() * 0.8;
            double pz = player.getZ() + (level.random.nextDouble() - 0.5) * 9.0;
            level.sendParticles(ParticleTypes.FALLING_SPORE_BLOSSOM, px, py, pz, 1, 0.0, 0.0, 0.0, 0.0);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1.2F, 1.0F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 1.2F);

        // Success notification
        player.displayClientMessage(
                Component.translatable("message.cobblepassive.meganium.activated")
                        .withStyle(ChatFormatting.GREEN),
                true
        );
    }
}
