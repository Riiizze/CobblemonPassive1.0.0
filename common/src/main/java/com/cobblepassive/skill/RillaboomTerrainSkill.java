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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

/**
 * Handles the server-side execution of Rillaboom's Active Skill ("Grass Surge").
 * Creates a concentric circular Grassy Terrain zone matching Cobblemon's battle terrain effect
 * for 10 seconds (200 ticks), healing grounded players and sent-out Pokémon.
 */
public final class RillaboomTerrainSkill {

    public static final long DEFAULT_COOLDOWN_MS = 30_000L;
    public static final int DURATION_TICKS = 200; // 10 seconds
    public static final int TERRAIN_RADIUS = 5;

    public static final DustParticleOptions DUST_GRASS =
            new DustParticleOptions(new Vector3f(98.0F / 255.0F, 209.0F / 255.0F, 79.0F / 255.0F), 1.5F);

    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, GrassyZone> ACTIVE_ZONES = new ConcurrentHashMap<>();

    private RillaboomTerrainSkill() {
    }

    public static long getCooldownMs() {
        return PassiveConfig.getActiveSkillCooldownMs("rillaboom", DEFAULT_COOLDOWN_MS);
    }

    /**
     * Checks if the player has Rillaboom sent out in the world and returns the Pokemon instance.
     */
    public static Pokemon findSentOutRillaboom(ServerPlayer player) {
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
                if ("rillaboom".equals(species) && isSentOut(pokemon, player.serverLevel())) {
                    return pokemon;
                }
            }
        } catch (Throwable t) {
            CobblePassive.LOGGER.error("[CobblePassive] Error finding sent-out Rillaboom for player: " + player.getName().getString(), t);
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
     * Attempts to find the living PokemonEntity representing Rillaboom in the server level.
     */
    public static PokemonEntity findRillaboomEntity(ServerPlayer player, Pokemon pokemon) {
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
     * Activates Rillaboom's Grassy Terrain active skill.
     */
    public static void activate(ServerPlayer player) {
        if (player == null || player.isSpectator()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        Pokemon rillaboom = findSentOutRillaboom(player);

        if (rillaboom == null) {
            player.displayClientMessage(
                    Component.translatable("message.cobblepassive.rillaboom.not_sent_out")
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
                    Component.translatable("message.cobblepassive.rillaboom.cooldown", remainingSec)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        // Apply Cooldown and sync with client HUD
        COOLDOWNS.put(player.getUUID(), now + cooldownMs);
        Services.PLATFORM.sendToPlayer(player, new ActiveSkillCooldownPayload(cooldownMs, cooldownMs));

        // Rillaboom cry and drum rhythm sound feedback
        PokemonEntity rillaboomEntity = findRillaboomEntity(player, rillaboom);
        if (rillaboomEntity != null) {
            try {
                java.lang.reflect.Method cryMethod = rillaboomEntity.getClass().getMethod("cry");
                cryMethod.invoke(rillaboomEntity);
            } catch (Throwable ignored) {
            }
        }

        // Spawn circular Grassy Terrain around player (radius 5)
        Vec3 center = player.position();
        AABB box = new AABB(
                center.x - 5.5D, center.y - 3.0D, center.z - 5.5D,
                center.x + 5.5D, center.y + 3.5D, center.z + 5.5D
        );

        ACTIVE_ZONES.put(player.getUUID(), new GrassyZone(level.dimension(), center, box, DURATION_TICKS));

        // Initial burst sound and concentric terrain rings matching Cobblemon battle visual
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1.6F, 0.6F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CHERRY_WOOD_FALL, SoundSource.PLAYERS, 1.2F, 1.0F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GRASS_PLACE, SoundSource.PLAYERS, 1.2F, 0.8F);

        spawnTerrainRings(level, center, TERRAIN_RADIUS);

        // Actionbar notification
        player.displayClientMessage(
                Component.translatable("message.cobblepassive.rillaboom.activated")
                        .withStyle(ChatFormatting.GREEN),
                true
        );
    }

    /**
     * Ticks any active Grassy Terrain zone associated with the given player each server tick.
     * Invoked from {@link com.cobblepassive.CobblePassive#onPlayerTick(ServerPlayer)}.
     */
    public static void tickPlayer(ServerPlayer player) {
        if (player == null || player.isRemoved()) {
            return;
        }

        GrassyZone zone = ACTIVE_ZONES.get(player.getUUID());
        if (zone == null) {
            return;
        }

        ServerLevel level = player.serverLevel();
        if (!level.dimension().equals(zone.dimension())) {
            return;
        }

        zone.remainingTicks--;

        // Subtle ambient floating grassy particles across the terrain zone
        if (zone.remainingTicks % 4 == 0) {
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double r = level.random.nextDouble() * TERRAIN_RADIUS;
            double px = zone.center.x + Math.cos(angle) * r;
            double pz = zone.center.z + Math.sin(angle) * r;
            double py = getGroundY(level, px, zone.center.y, pz) + 0.1D;
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        // Heal cycle and terrain ring pulse every 20 ticks (1 second)
        if (zone.remainingTicks % 20 == 0) {
            // Pulse concentric rings so they stay clearly visible on the ground
            spawnTerrainRings(level, zone.center, TERRAIN_RADIUS);
            level.playSound(null, zone.center.x, zone.center.y, zone.center.z, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8F, 0.7F);

            if (zone.boundingBox.contains(player.position())) {
                if (player.getHealth() < player.getMaxHealth()) {
                    player.heal(1.0F); // 1 HP (0.5 heart)
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.4F, 1.8F);
                    level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.2D, player.getZ(), 1, 0.2D, 0.2D, 0.2D, 0.0D);
                }
            }

            // Grounded Pokémon heal 1/16 of max HP per second inside Grassy Terrain
            try {
                PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
                if (party != null) {
                    for (Pokemon pkmn : party) {
                        if (pkmn != null && !pkmn.isFainted() && pkmn.canBeHealed()) {
                            PokemonEntity pe = pkmn.getEntity();
                            if (pe != null && !pe.isRemoved()) {
                                net.minecraft.world.entity.Entity mob = pe;
                                if (zone.boundingBox.contains(mob.position())) {
                                    int healAmount = Math.max(1, pkmn.getMaxHealth() / 16);
                                    pkmn.setCurrentHealth(Math.min(pkmn.getMaxHealth(), pkmn.getCurrentHealth() + healAmount));
                                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, mob.getX(), mob.getY() + 0.5D, mob.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.0D);
                                }
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        // Expire zone when duration ends
        if (zone.remainingTicks <= 0) {
            ACTIVE_ZONES.remove(player.getUUID());
            level.playSound(null, zone.center.x, zone.center.y, zone.center.z, SoundEvents.CHERRY_LEAVES_BREAK, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
    }

    /**
     * Spawns concentric rings of emerald green dust particles that hug the ground,
     * exactly reproducing Cobblemon's Grassy Terrain battle field visual effect.
     */
    public static void spawnTerrainRings(ServerLevel level, Vec3 center, int maxRadius) {
        for (int r = 1; r <= maxRadius; r++) {
            float step = (float) (Math.PI / (8.0D * r));
            for (float angle = 0.0F; angle < (float) (Math.PI * 2.0D); angle += step) {
                double px = center.x + Math.cos(angle) * r;
                double pz = center.z + Math.sin(angle) * r;
                double py = getGroundY(level, px, center.y, pz);
                level.sendParticles(DUST_GRASS, px, py, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    /**
     * Calculates the top ground surface Y coordinate at the given horizontal position,
     * properly hugging stairs, slopes, and terrain elevation drops.
     */
    public static double getGroundY(ServerLevel level, double x, double startY, double z) {
        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        int sy = Mth.floor(startY);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(bx, sy + 2, bz);

        for (int y = sy + 2; y >= sy - 6; y--) {
            pos.setY(y);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) {
                VoxelShape shape = state.getCollisionShape(level, pos);
                if (!shape.isEmpty()) {
                    return y + shape.max(Direction.Axis.Y) + 0.05D;
                }
            }
        }
        return startY + 0.05D;
    }

    /**
     * Represents an active 4x4 Grassy Terrain zone.
     */
    private static final class GrassyZone {
        private final ResourceKey<Level> dimension;
        private final Vec3 center;
        private final AABB boundingBox;
        private int remainingTicks;

        private GrassyZone(ResourceKey<Level> dimension, Vec3 center, AABB boundingBox, int remainingTicks) {
            this.dimension = dimension;
            this.center = center;
            this.boundingBox = boundingBox;
            this.remainingTicks = remainingTicks;
        }

        public ResourceKey<Level> dimension() {
            return dimension;
        }
    }
}
