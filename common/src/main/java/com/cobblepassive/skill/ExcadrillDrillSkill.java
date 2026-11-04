package com.cobblepassive.skill;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.activestate.ActivePokemonState;
import com.cobblemon.mod.common.pokemon.activestate.PokemonState;
import com.cobblemon.mod.common.pokemon.activestate.SentOutState;
import com.cobblepassive.CobblePassive;
import com.cobblepassive.network.ActiveSkillCooldownPayload;
import com.cobblepassive.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the server-side state machine and execution lifecycle for Excadrill's Active Drill Skill ("Drill").
 */
public final class ExcadrillDrillSkill {

    /** Default per-player skill cooldown in milliseconds (30 seconds). */
    public static final long DEFAULT_COOLDOWN_MS = 30_000L;

    public static long getCooldownMs() {
        return com.cobblepassive.PassiveConfig.getActiveSkillCooldownMs("excadrill", DEFAULT_COOLDOWN_MS);
    }

    /** Maximum crosshair raycast range in blocks. */
    public static final double MAX_RAYCAST_DISTANCE = 25.0D;

    /** Delay in server ticks for the full drill dive sequence before the instant 15-block burst (35 ticks = 1.75s). */
    public static final int DRILL_DELAY_TICKS = 35;

    /** Number of blocks straight down to destroy instantaneously after the drill windup. */
    public static final int BURST_DEPTH = 15;

    private static final Map<UUID, Long> PLAYER_COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, DrillTask> ACTIVE_DRILL_TASKS = new ConcurrentHashMap<>();

    private ExcadrillDrillSkill() {
    }

    /**
     * Triggered when the server receives an {@link com.cobblepassive.network.ExcadrillSkillPayload} from a player.
     */
    public static void activate(ServerPlayer player) {
        try {
            if (player == null || player.isSpectator() || !(player.level() instanceof ServerLevel level)) {
                return;
            }

            UUID playerId = player.getUUID();
            if (ACTIVE_DRILL_TASKS.containsKey(playerId)) {
                return;
            }

            // 1. Validate that the player has an active, sent-out Excadrill in the world
            // Always reference the entity as Vanilla Mob so all Minecraft methods remap cleanly across Cobblemon versions
            Mob excadrillEntity = findSentOutExcadrill(player);
            if (excadrillEntity == null) {
                player.displayClientMessage(
                        Component.translatable("message.cobblepassive.excadrill.not_sent_out")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return;
            }

            // 2. Check per-player cooldown (default 30 seconds, configurable in cobblepassive.json)
            long now = System.currentTimeMillis();
            long cooldownMs = getCooldownMs();
            long cooldownUntil = PLAYER_COOLDOWNS.getOrDefault(playerId, 0L);
            if (now < cooldownUntil) {
                long remainingSeconds = Math.max(1L, (cooldownUntil - now + 999L) / 1000L);
                Services.PLATFORM.sendToPlayer(player, new ActiveSkillCooldownPayload(cooldownUntil - now, cooldownMs));
                player.displayClientMessage(
                        Component.translatable("message.cobblepassive.excadrill.cooldown", remainingSeconds)
                                .withStyle(ChatFormatting.YELLOW),
                        true
                );
                return;
            }

            // 3. Perform raycast from player's crosshair up to 25 blocks
            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVec = player.getLookAngle();
            Vec3 endPos = eyePos.add(lookVec.scale(MAX_RAYCAST_DISTANCE));
            BlockHitResult hitResult = level.clip(new ClipContext(
                    eyePos,
                    endPos,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            ));

            if (hitResult.getType() != HitResult.Type.BLOCK) {
                player.displayClientMessage(
                        Component.translatable("message.cobblepassive.excadrill.no_target")
                                .withStyle(ChatFormatting.GOLD),
                        true
                );
                Services.PLATFORM.sendToPlayer(player, new ActiveSkillCooldownPayload(0L, 0L));
                return;
            }

            BlockPos targetPos = hitResult.getBlockPos();
            BlockState targetState = level.getBlockState(targetPos);
            if (targetState.isAir()) {
                player.displayClientMessage(
                        Component.translatable("message.cobblepassive.excadrill.no_target")
                                .withStyle(ChatFormatting.GOLD),
                        true
                );
                Services.PLATFORM.sendToPlayer(player, new ActiveSkillCooldownPayload(0L, 0L));
                return;
            }

            // Lock in cooldown and sync to client
            PLAYER_COOLDOWNS.put(playerId, now + cooldownMs);
            Services.PLATFORM.sendToPlayer(player, new ActiveSkillCooldownPayload(cooldownMs, cooldownMs));

            // 4. Teleport Excadrill directly above targetPos (targetPos.above()) and reset velocity to zero
            BlockPos aboveTarget = targetPos.above();
            double destX = aboveTarget.getX() + 0.5D;
            double destY = aboveTarget.getY();
            double destZ = aboveTarget.getZ() + 0.5D;

            excadrillEntity.getNavigation().stop();
            excadrillEntity.teleportTo(destX, destY, destZ);
            excadrillEntity.setDeltaMovement(Vec3.ZERO);
            excadrillEntity.hasImpulse = true;
            excadrillEntity.fallDistance = 0.0F;
            excadrillEntity.setNoGravity(true);
            excadrillEntity.setInvulnerable(true);

            // Orient Excadrill towards player's aim angle / facing down into the ground
            excadrillEntity.setYRot(player.getYRot());
            excadrillEntity.setYHeadRot(player.getYRot());
            excadrillEntity.setYBodyRot(player.getYRot());
            excadrillEntity.setXRot(35.0F);

            // Arrival dust particles and sweep sound
            BlockParticleOption arrivalDust = new BlockParticleOption(ParticleTypes.BLOCK, targetState);
            level.sendParticles(arrivalDust, destX, destY + 0.1D, destZ, 28, 0.35D, 0.15D, 0.35D, 0.12D);
            level.sendParticles(ParticleTypes.CLOUD, destX, destY + 0.2D, destZ, 12, 0.25D, 0.15D, 0.25D, 0.04D);
            level.playSound(null, aboveTarget, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 0.85F);

            // 5. Trigger Excadrill's real attack animation ("physical" poser animation)
            triggerDrillAnimation(level, excadrillEntity);

            // Play initial drill sounds and swirl particles
            level.playSound(null, aboveTarget, SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 1.3F, 1.15F);
            spawnDrillSwirlParticles(level, destX, destY, destZ, targetState, 0);

            player.displayClientMessage(
                    Component.translatable("message.cobblepassive.excadrill.activated")
                            .withStyle(ChatFormatting.GOLD),
                    true
            );

            // 6. Schedule 24-tick (~1.2s) server delay state before the 15-block instant burst
            ACTIVE_DRILL_TASKS.put(
                    playerId,
                    new DrillTask(playerId, excadrillEntity.getId(), level.dimension(), targetPos.immutable(), DRILL_DELAY_TICKS)
            );
        } catch (Throwable t) {
            CobblePassive.LOGGER.error("[CobblePassive] Unexpected error while activating Excadrill drill skill", t);
        }
    }

    /**
     * Ticks any active Excadrill drill task for the given player.
     * Invoked every server tick from {@link com.cobblepassive.CobblePassive#onPlayerTick(ServerPlayer)}.
     */
    public static void tickPlayer(ServerPlayer player) {
        try {
            UUID playerId = player.getUUID();
            DrillTask task = ACTIVE_DRILL_TASKS.get(playerId);
            if (task == null) {
                return;
            }

            if (!(player.level() instanceof ServerLevel level) || !level.dimension().equals(task.dimension)) {
                ACTIVE_DRILL_TASKS.remove(playerId);
                Entity entity = player.serverLevel().getEntity(task.entityId);
                if (entity instanceof Mob excadrill) {
                    excadrill.setNoGravity(false);
                    excadrill.setInvulnerable(false);
                    excadrill.fallDistance = 0.0F;
                    excadrill.teleportTo(player.getX(), player.getY(), player.getZ());
                }
                return;
            }

            Entity entity = level.getEntity(task.entityId);
            Mob excadrill = entity instanceof Mob mob && mob.isAlive() ? mob : null;

            BlockPos targetPos = task.targetPos;
            BlockPos abovePos = targetPos.above();
            double centerX = abovePos.getX() + 0.5D;
            double centerY = abovePos.getY();
            double centerZ = abovePos.getZ() + 0.5D;

            if (task.ticksRemaining > 0) {
                int elapsed = DRILL_DELAY_TICKS - task.ticksRemaining;
                BlockState surfaceState = level.getBlockState(targetPos);
                if (surfaceState.isAir()) {
                    surfaceState = Blocks.DIRT.defaultBlockState();
                }

                // Keep Excadrill anchored on top of targetPos with zero velocity while playing attack animation
                if (excadrill != null) {
                    excadrill.getNavigation().stop();
                    excadrill.setPos(centerX, centerY, centerZ);
                    excadrill.setDeltaMovement(Vec3.ZERO);
                    excadrill.hasImpulse = true;
                    excadrill.fallDistance = 0.0F;
                    excadrill.setNoGravity(true);

                    // Keep Excadrill facing towards the target block along player's look vector
                    excadrill.setYRot(player.getYRot());
                    excadrill.setYHeadRot(player.getYRot());
                    excadrill.setYBodyRot(player.getYRot());
                    excadrill.setXRot(35.0F);
                }

                // Play drilling sounds and swirl particles throughout the windup phase
                spawnDrillSwirlParticles(level, centerX, centerY, centerZ, surfaceState, elapsed);

                if (elapsed % 4 == 0) {
                    float pitch = 0.9F + (elapsed / (float) DRILL_DELAY_TICKS) * 0.6F;
                    level.playSound(null, abovePos, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, pitch);
                    level.playSound(null, abovePos, SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 1.1F, pitch);
                }

                task.ticksRemaining--;
                return;
            }

            // Phase 5: Instant 15-Block Burst (from targetPos down to targetPos.below(14))
            ACTIVE_DRILL_TASKS.remove(playerId);
            executeInstantDrillBurst(level, player, excadrill, targetPos);
        } catch (Throwable t) {
            if (player != null) {
                ACTIVE_DRILL_TASKS.remove(player.getUUID());
            }
            CobblePassive.LOGGER.error("[CobblePassive] Unexpected error while ticking Excadrill drill skill", t);
        }
    }

    /**
     * Destroys 15 blocks straight down in a single tick with safety checks, normal block drops,
     * block particle explosions, and heavy impact sounds, then returns Excadrill safely to the player.
     */
    private static void executeInstantDrillBurst(
            ServerLevel level,
            ServerPlayer player,
            Mob excadrill,
            BlockPos targetPos
    ) {
        int brokenCount = 0;

        for (int i = 0; i < BURST_DEPTH; i++) {
            BlockPos currentPos = targetPos.below(i);
            if (!level.isInWorldBounds(currentPos)) {
                break;
            }

            BlockState state = level.getBlockState(currentPos);
            if (!isSafeToDrill(level, currentPos, state)) {
                continue;
            }

            // Spawn block shatter particle burst for each destroyed block
            BlockParticleOption blockParticle = new BlockParticleOption(ParticleTypes.BLOCK, state);
            level.sendParticles(
                    blockParticle,
                    currentPos.getX() + 0.5D,
                    currentPos.getY() + 0.5D,
                    currentPos.getZ() + 0.5D,
                    22,
                    0.35D,
                    0.35D,
                    0.35D,
                    0.15D
            );

            // Destroy block and drop items normally
            if (level.destroyBlock(currentPos, true, player)) {
                brokenCount++;
            }
        }

        // Spawn shockwave particles at the surface and bottom of the shaft
        level.sendParticles(
                ParticleTypes.CLOUD,
                targetPos.getX() + 0.5D,
                targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D,
                20,
                0.4D,
                0.4D,
                0.4D,
                0.08D
        );
        level.sendParticles(
                ParticleTypes.CRIT,
                targetPos.getX() + 0.5D,
                targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D,
                25,
                0.45D,
                0.5D,
                0.45D,
                0.2D
        );

        // Play loud rock-shattering sound effects (anvil sound removed per user request)
        level.playSound(null, targetPos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 2.0F, 0.7F);
        BlockState targetState = level.getBlockState(targetPos);
        level.playSound(null, targetPos, targetState.getSoundType().getBreakSound(), SoundSource.BLOCKS, 2.0F, 0.8F);
        if (brokenCount > 0) {
            BlockPos midPos = targetPos.below(Math.min(brokenCount, BURST_DEPTH - 1) / 2);
            level.playSound(null, midPos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.8F, 0.75F);
        }

        // Visual Teleport Return: Safely return Excadrill back to the player so it NEVER falls into the pit or takes fall damage
        if (excadrill != null) {
            excadrill.setNoGravity(false);
            excadrill.setInvulnerable(false);
            excadrill.fallDistance = 0.0F;
            excadrill.setDeltaMovement(Vec3.ZERO);

            // Teleport back safely to player's side
            Vec3 returnPos = findSafeReturnPosition(player);
            excadrill.teleportTo(returnPos.x, returnPos.y, returnPos.z);
            excadrill.setYRot(player.getYRot());
            excadrill.setYHeadRot(player.getYRot());
            excadrill.setYBodyRot(player.getYRot());
            excadrill.setXRot(0.0F);

            // Poof particles and subtle teleport sound at the return location
            level.sendParticles(ParticleTypes.POOF, returnPos.x, returnPos.y + 0.3D, returnPos.z, 14, 0.25D, 0.25D, 0.25D, 0.04D);
            level.playSound(null, BlockPos.containing(returnPos), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, 1.2F);
        }
    }

    /**
     * Calculates a safe position right beside or behind the player for Excadrill to return to.
     */
    private static Vec3 findSafeReturnPosition(ServerPlayer player) {
        Vec3 playerPos = player.position();
        float rad = (float) Math.toRadians(player.getYRot() + 135.0F);
        double offsetX = -Math.sin(rad) * 1.35D;
        double offsetZ = Math.cos(rad) * 1.35D;
        return new Vec3(playerPos.x + offsetX, playerPos.y, playerPos.z + offsetZ);
    }

    /**
     * Safety check ensuring Bedrock, Barrier, Air, liquids, and unbreakable blocks (hardness &lt; 0) are never broken.
     */
    private static boolean isSafeToDrill(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return false;
        }
        if (state.is(Blocks.BEDROCK) || state.is(Blocks.BARRIER)) {
            return false;
        }
        float destroySpeed = state.getDestroySpeed(level, pos);
        if (destroySpeed < 0.0F) {
            return false;
        }
        // Skip pure fluid blocks (water/lava source or flowing blocks without solid collision)
        if (!state.getFluidState().isEmpty() && state.getCollisionShape(level, pos).isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Spawns a swirling vortex of drill particles and block dust around Excadrill during the drill animation phase.
     */
    private static void spawnDrillSwirlParticles(
            ServerLevel level,
            double centerX,
            double centerY,
            double centerZ,
            BlockState targetState,
            int tick
    ) {
        double angle1 = (tick * 0.85D);
        double angle2 = angle1 + Math.PI;
        double radius = 0.65D;

        double x1 = centerX + Math.cos(angle1) * radius;
        double z1 = centerZ + Math.sin(angle1) * radius;
        double x2 = centerX + Math.cos(angle2) * radius;
        double z2 = centerZ + Math.sin(angle2) * radius;

        level.sendParticles(ParticleTypes.CRIT, x1, centerY + 0.35D, z1, 3, 0.05D, 0.05D, 0.05D, 0.02D);
        level.sendParticles(ParticleTypes.CRIT, x2, centerY + 0.35D, z2, 3, 0.05D, 0.05D, 0.05D, 0.02D);

        if (tick % 3 == 0) {
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, centerX, centerY + 0.3D, centerZ, 1, 0.1D, 0.05D, 0.1D, 0.0D);
        }

        BlockParticleOption dustParticle = new BlockParticleOption(ParticleTypes.BLOCK, targetState);
        level.sendParticles(dustParticle, centerX, centerY + 0.05D, centerZ, 8, 0.25D, 0.1D, 0.25D, 0.08D);
    }

    /**
     * Triggers Excadrill's Cobblemon attack animation ("physical" poser animation and arm swing).
     */
    private static void triggerDrillAnimation(ServerLevel level, Mob excadrill) {
        // 1. Direct Cobblemon playAnimation("physical", ...) call via reflection
        try {
            Method playAnimation = excadrill.getClass().getMethod("playAnimation", String.class, List.class);
            playAnimation.invoke(excadrill, "physical", Collections.emptyList());
        } catch (Throwable ignored) {
        }

        // 2. Broadcast Cobblemon's PlayPosableAnimationPacket for "physical" attack animation
        try {
            Class<?> packetClass = Class.forName("com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket");
            Constructor<?> ctor = packetClass.getConstructor(int.class, Set.class, List.class);
            Object packet = ctor.newInstance(
                    excadrill.getId(),
                    Set.of("physical"),
                    Collections.emptyList()
            );

            Method sendToPlayersAround = null;
            for (Method m : packetClass.getMethods()) {
                if (m.getName().equals("sendToPlayersAround") && m.getParameterCount() >= 4) {
                    sendToPlayersAround = m;
                    break;
                }
            }
            if (sendToPlayersAround != null) {
                Class<?>[] paramTypes = sendToPlayersAround.getParameterTypes();
                if (paramTypes.length == 6) {
                    sendToPlayersAround.invoke(
                            packet,
                            excadrill.getX(),
                            excadrill.getY(),
                            excadrill.getZ(),
                            64.0D,
                            level.dimension(),
                            Collections.emptyList()
                    );
                }
            }
        } catch (Throwable ignored) {
        }

        // 3. Trigger Vanilla arm swing animation
        try {
            excadrill.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        } catch (Throwable ignored) {
        }

        // 4. Trigger Cobblemon cry
        try {
            Method cryMethod = excadrill.getClass().getMethod("cry");
            cryMethod.invoke(excadrill);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Searches the player's party for a non-fainted Excadrill that is currently sent out in the world.
     * Returns the entity as a Vanilla {@link Mob} so all subsequent entity operations use standard Mojmap/Intermediary methods.
     */
    public static Mob findSentOutExcadrill(ServerPlayer player) {
        try {
            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
            if (party == null) {
                return null;
            }

            for (Pokemon pokemon : party) {
                if (pokemon == null || pokemon.isFainted()) {
                    continue;
                }
                if (pokemon.getSpecies() == null || pokemon.getSpecies().getName() == null) {
                    continue;
                }
                String species = pokemon.getSpecies().getName();
                if (!"excadrill".equalsIgnoreCase(species)) {
                    continue;
                }

                PokemonState state = pokemon.getState();
                boolean isSentOut = (state instanceof SentOutState) || (state instanceof ActivePokemonState);
                if (!isSentOut) {
                    continue;
                }

                Mob resolved = resolvePokemonMob(player, pokemon, state);
                if (resolved != null && resolved.isAlive() && resolved.level() == player.level()) {
                    return resolved;
                }
            }
        } catch (Throwable t) {
            CobblePassive.LOGGER.error("[CobblePassive] Error searching party for sent-out Excadrill", t);
        }
        return null;
    }

    /**
     * Resolves the in-world {@link Mob} entity for a sent-out {@link Pokemon} across Cobblemon 1.6.x - 1.8.x.
     */
    private static Mob resolvePokemonMob(ServerPlayer player, Pokemon pokemon, PokemonState state) {
        // 1. Direct SentOutState / ActivePokemonState lookup
        try {
            if (state instanceof SentOutState sentOut) {
                PokemonEntity entity = sentOut.getEntity();
                if (entity != null) {
                    return entity;
                }
            }
            if (state instanceof ActivePokemonState activeState) {
                PokemonEntity entity = activeState.getEntity();
                if (entity != null) {
                    return entity;
                }
            }
        } catch (Throwable ignored) {
        }

        // 2. Reflective getEntity() fallback on state or pokemon instance
        try {
            if (state != null) {
                Method getEntity = state.getClass().getMethod("getEntity");
                Object obj = getEntity.invoke(state);
                if (obj instanceof Mob mob) {
                    return mob;
                }
            }
        } catch (Throwable ignored) {
        }

        try {
            Method getEntity = pokemon.getClass().getMethod("getEntity");
            Object obj = getEntity.invoke(pokemon);
            if (obj instanceof Mob mob) {
                return mob;
            }
        } catch (Throwable ignored) {
        }

        // 3. World entity scan fallback around the player
        try {
            if (player.level() instanceof ServerLevel level) {
                UUID targetPokemonUuid = pokemon.getUuid();
                AABB searchBox = player.getBoundingBox().inflate(64.0D);
                List<Mob> nearbyMobs = level.getEntitiesOfClass(
                        Mob.class,
                        searchBox,
                        e -> e instanceof PokemonEntity && e.isAlive()
                );
                for (Mob mob : nearbyMobs) {
                    if (mob instanceof PokemonEntity pe && pe.getPokemon() != null) {
                        if (targetPokemonUuid != null && targetPokemonUuid.equals(pe.getPokemon().getUuid())) {
                            return mob;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return null;
    }

    /**
     * Represents an in-flight Excadrill drill windup task before the 15-block instant burst.
     */
    private static final class DrillTask {
        private final UUID playerId;
        private final int entityId;
        private final ResourceKey<Level> dimension;
        private final BlockPos targetPos;
        private int ticksRemaining;

        private DrillTask(
                UUID playerId,
                int entityId,
                ResourceKey<Level> dimension,
                BlockPos targetPos,
                int ticksRemaining
        ) {
            this.playerId = playerId;
            this.entityId = entityId;
            this.dimension = dimension;
            this.targetPos = targetPos;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
