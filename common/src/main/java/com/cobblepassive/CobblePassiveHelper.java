package com.cobblepassive;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.storage.ClientParty;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.activestate.PokemonState;
import com.cobblemon.mod.common.pokemon.activestate.SentOutState;
import com.cobblemon.mod.common.pokemon.activestate.ShoulderedState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;

public class CobblePassiveHelper {
    private static final Map<UUID, Integer> PLAYER_FIRE_PROT_LEVEL = new HashMap<>();
    private static final Map<UUID, Integer> PLAYER_LAVA_MAX_TICKS = new HashMap<>();
    private static final Map<UUID, Integer> PLAYER_LAVA_REMAINING_TICKS = new HashMap<>();
    private static final Map<UUID, Float> PLAYER_LIFE_STEAL = new HashMap<>();
    private static final Map<UUID, Double> PLAYER_VINE_CLIMB = new HashMap<>();
    private static final Map<UUID, Float> PLAYER_BREAK_SPEED = new HashMap<>();
    private static final Map<UUID, Float> PLAYER_THORNS_DAMAGE = new HashMap<>();
    private static final Map<UUID, Boolean> PLAYER_FERROTHORN_POISON = new HashMap<>();
    private static final Map<UUID, Integer> PLAYER_FIRE_ASPECT_SEC = new HashMap<>();
    private static final Map<UUID, Float> PLAYER_BARE_HAND_DMG = new HashMap<>();
    private static final Map<UUID, Integer> PLAYER_FISHING_LUCK = new HashMap<>();
    private static final Map<UUID, Integer> PLAYER_FISHING_LURE = new HashMap<>();

    private static final ResourceLocation OXYGEN_BONUS_ID = ResourceLocation.fromNamespaceAndPath("cobblepassive", "oxygen_bonus");
    private static final ResourceLocation BURNING_TIME_ID = ResourceLocation.fromNamespaceAndPath("cobblepassive", "burning_time");
    private static final ResourceLocation SNEAK_SPEED_ID = ResourceLocation.fromNamespaceAndPath("cobblepassive", "sneak_speed_bonus");
    private static final ResourceLocation BLOCK_REACH_ID = ResourceLocation.fromNamespaceAndPath("cobblepassive", "block_reach_bonus");
    private static final ResourceLocation ENTITY_REACH_ID = ResourceLocation.fromNamespaceAndPath("cobblepassive", "entity_reach_bonus");
    private static final ResourceLocation BREAK_SPEED_ID = ResourceLocation.fromNamespaceAndPath("cobblepassive", "break_speed_bonus");

    private static java.lang.reflect.Field FIELD_TIME_UNTIL_LURED;
    static {
        try {
            FIELD_TIME_UNTIL_LURED = net.minecraft.world.entity.projectile.FishingHook.class.getDeclaredField("timeUntilLured");
            FIELD_TIME_UNTIL_LURED.setAccessible(true);
        } catch (Throwable ignored) {}
    }

    public static int getFireProtectionLevel(Player player) {
        return PLAYER_FIRE_PROT_LEVEL.getOrDefault(player.getUUID(), 0);
    }

    public static boolean hasLavaStrider(Player player) {
        UUID uuid = player.getUUID();
        Integer max = PLAYER_LAVA_MAX_TICKS.get(uuid);
        if (max == null || max <= 0) return false;
        int rem = PLAYER_LAVA_REMAINING_TICKS.getOrDefault(uuid, max);
        return rem > 0;
    }

    public static double getVineClimbBonus(Player player) {
        Double bonus = PLAYER_VINE_CLIMB.get(player.getUUID());
        if (bonus != null && bonus > 0.0) return bonus;
        if (player.level().isClientSide()) {
            try {
                Iterable<Pokemon> party = getClientParty();
                if (party != null) {
                    double maxBonus = 0.0;
                    for (Pokemon pokemon : party) {
                        if (!isPokemonActive(player, pokemon)) continue;
                        String species = pokemon.getSpecies().getName().toLowerCase();
                        PassiveConfig.PassiveEntry entry = PassiveConfig.getPassive(species);
                        if (entry != null && entry.vineClimbSpeedBonus > maxBonus) {
                            maxBonus = entry.vineClimbSpeedBonus;
                        }
                        if (species.equals("tangrowth") && maxBonus < 0.50) maxBonus = 0.50;
                        else if (species.equals("tangela") && maxBonus < 0.25) maxBonus = 0.25;
                    }
                    return maxBonus;
                }
            } catch (Throwable ignored) {}
        }
        return 0.0;
    }

    public static void applyLifeSteal(Player player) {
        Float amount = PLAYER_LIFE_STEAL.get(player.getUUID());
        if (amount != null && amount > 0.0f) {
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(amount);
            }
        }
    }

    public static void onPlayerDamaged(Player player, DamageSource source, float amount) {
        if (player == null || source == null || player.level().isClientSide()) return;
        if (!source.is(DamageTypes.THORNS)) {
            Entity directAttacker = source.getEntity();
            if (directAttacker instanceof LivingEntity livingAttacker && livingAttacker != player && livingAttacker.isAlive()) {
                float thornsDmg = getPlayerThornsDamage(player);
                if (thornsDmg > 0.0f) {
                    livingAttacker.hurt(player.damageSources().thorns(player), thornsDmg);
                    if (hasFerrothornPoison(player)) {
                        livingAttacker.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
                    }
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.THORNS_HIT, SoundSource.PLAYERS, 1.0f, 1.0f);
                    if (player.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR, livingAttacker.getX(), livingAttacker.getY() + livingAttacker.getBbHeight() * 0.5, livingAttacker.getZ(), 5, 0.2, 0.2, 0.2, 0.1);
                    }
                }
            }
        }
    }

    public static boolean isPlayerOnOrInLava(Player player) {
        if (player.isInLava()) {
            return true;
        }
        Level world = player.level();
        BlockPos pos = player.blockPosition();
        if (world.getFluidState(pos).is(FluidTags.LAVA)) {
            return true;
        }
        FluidState below = world.getFluidState(pos.below());
        if (below.is(FluidTags.LAVA)) {
            return true;
        }
        return false;
    }

    public static void tickLavaStrider(Player player) {
        UUID uuid = player.getUUID();
        Integer max = PLAYER_LAVA_MAX_TICKS.get(uuid);
        if (max == null || max <= 0) return;

        boolean inOrOnLava = isPlayerOnOrInLava(player);
        if (!inOrOnLava) {
            PLAYER_LAVA_REMAINING_TICKS.put(uuid, max);
            return;
        }

        int rem = PLAYER_LAVA_REMAINING_TICKS.getOrDefault(uuid, max);
        if (rem > 0) {
            rem--;
            PLAYER_LAVA_REMAINING_TICKS.put(uuid, rem);

            if (!player.isCrouching()) {
                Vec3 vel = player.getDeltaMovement();
                double vx = vel.x;
                double vy = vel.y;
                double vz = vel.z;

                if (player.isInLava()) {
                    CollisionContext ctx = CollisionContext.of(player);
                    if (ctx.isAbove(Shapes.block(), player.blockPosition(), true)
                            && !player.level().getFluidState(player.blockPosition().above()).is(FluidTags.LAVA)) {
                        player.setOnGround(true);
                        if (vy < 0.0) {
                            vy = 0.0;
                        }
                    } else {
                        vy = vy * 0.5 + 0.05;
                    }
                }

                double hSpeed = Math.hypot(vx, vz);
                if (hSpeed > 0.002) {
                    double targetPerTick = 0.10;
                    double scale = targetPerTick / hSpeed;
                    vx *= scale;
                    vz *= scale;
                }
                player.setDeltaMovement(vx, vy, vz);
            }
        }
    }

    public static void onPlayerTick(ServerPlayer player) {
        if (player.isDeadOrDying()) {
            cleanupPlayer(player);
            return;
        }

        PlayerPartyStore party;
        try {
            party = Cobblemon.INSTANCE.getStorage().getParty(player);
        } catch (Exception e) {
            return;
        }

        boolean hasMew = false;
        int maxFireProt = 0;
        int maxLavaStriderSec = 0;
        int maxRespiration = 0;
        boolean hasSharpedo = false;
        boolean hasCelebi = false;
        int maxCropRadius = 8;
        double maxCropChance = 1.0;
        double minCropIntervalSec = 1.0;
        int minCropsThreshold = 2;
        float maxLifeSteal = 0.0f;
        float mewLifeStealBonus = 1.0f;
        double maxVineClimb = 0.0;
        double maxSneakSpeed = 0.0;
        double maxReachBonus = 0.0;
        double maxBreakSpeedBonus = 0.0;
        float maxThornsDamage = 0.0f;
        boolean hasFerrothornPoison = false;
        int maxFireAspectSec = 0;
        float maxBareHandDmg = 0.0f;
        int maxFishingLuck = 0;
        int maxFishingLure = 0;

        Entity vehicle = player.getVehicle();
        if (vehicle instanceof PokemonEntity pokeEntity) {
            String sp = pokeEntity.getPokemon().getSpecies().getName().toLowerCase();
            if (sp.contains("sharpedo")) {
                hasSharpedo = true;
            }
        }

        for (Pokemon pokemon : party) {
            if (!isPokemonActive(player, pokemon)) continue;
            String species = pokemon.getSpecies().getName().toLowerCase();
            PassiveConfig.PassiveEntry entry = PassiveConfig.getPassive(species);
            if (entry != null) {
                if (entry.isMewDnaResonance || species.equals("mew")) hasMew = true;
                if (entry.fireProtectionLevel > maxFireProt) maxFireProt = entry.fireProtectionLevel;
                if (entry.lavaStriderSeconds > maxLavaStriderSec) maxLavaStriderSec = entry.lavaStriderSeconds;
                if (entry.respirationLevel > maxRespiration) maxRespiration = entry.respirationLevel;
                if (entry.waterNightVision || species.contains("sharpedo")) hasSharpedo = true;
                if (entry.cropGrowthRadius > 0 || species.equals("celebi") || species.equals("chelebi")) {
                    hasCelebi = true;
                    if (entry.cropGrowthRadius > 0) maxCropRadius = entry.cropGrowthRadius;
                    if (entry.cropGrowthChance > 0) maxCropChance = entry.cropGrowthChance;
                    if (entry.cropGrowthIntervalSeconds > 0) minCropIntervalSec = entry.cropGrowthIntervalSeconds;
                    if (entry.cropGrowthMinCrops >= 0) minCropsThreshold = entry.cropGrowthMinCrops;
                }
                if (entry.lifeSteal > maxLifeSteal) maxLifeSteal = entry.lifeSteal;
                if (entry.lifeStealMewBonus > 0.0f) mewLifeStealBonus = entry.lifeStealMewBonus;
                if (species.equals("crobat")) {
                    if (maxLifeSteal < 2.0f) maxLifeSteal = 2.0f;
                } else if (species.equals("golbat") || species.equals("zubat")) {
                    if (maxLifeSteal < 1.0f) maxLifeSteal = 1.0f;
                }

                if (entry.vineClimbSpeedBonus > maxVineClimb) maxVineClimb = entry.vineClimbSpeedBonus;
                if (species.equals("tangrowth") && maxVineClimb < 0.50) maxVineClimb = 0.50;
                else if (species.equals("tangela") && maxVineClimb < 0.25) maxVineClimb = 0.25;

                if (entry.sneakSpeedBonus > maxSneakSpeed) maxSneakSpeed = entry.sneakSpeedBonus;
                if (species.equals("ekans") && maxSneakSpeed < 0.3) maxSneakSpeed = 0.3;
                else if (species.equals("arbok") && maxSneakSpeed < 0.6) maxSneakSpeed = 0.6;

                if (entry.reachBonus > maxReachBonus) maxReachBonus = entry.reachBonus;
                if (entry.breakSpeedBonus > maxBreakSpeedBonus) maxBreakSpeedBonus = entry.breakSpeedBonus;
                if (entry.thornsDamage > maxThornsDamage) maxThornsDamage = entry.thornsDamage;
                if (entry.inflictPoisonSeconds > 0 || species.equals("ferrothorn")) hasFerrothornPoison = true;
                if (entry.fireAspectSeconds > maxFireAspectSec) maxFireAspectSec = entry.fireAspectSeconds;
                if (entry.handBonusDamage > maxBareHandDmg) maxBareHandDmg = entry.handBonusDamage;
                if (entry.fishingLuckLevel > maxFishingLuck) maxFishingLuck = entry.fishingLuckLevel;
                if (entry.fishingLureLevel > maxFishingLure) maxFishingLure = entry.fishingLureLevel;
            } else {
                if (species.equals("mew")) hasMew = true;
                if (species.contains("sharpedo")) hasSharpedo = true;
                if (species.equals("celebi") || species.equals("chelebi")) hasCelebi = true;
                if (species.equals("crobat")) {
                    if (maxLifeSteal < 2.0f) maxLifeSteal = 2.0f;
                } else if (species.equals("golbat") || species.equals("zubat")) {
                    if (maxLifeSteal < 1.0f) maxLifeSteal = 1.0f;
                }

                if (species.equals("tangrowth") && maxVineClimb < 0.50) maxVineClimb = 0.50;
                else if (species.equals("tangela") && maxVineClimb < 0.25) maxVineClimb = 0.25;

                if (species.equals("ekans") && maxSneakSpeed < 0.3) maxSneakSpeed = 0.3;
                else if (species.equals("arbok") && maxSneakSpeed < 0.6) maxSneakSpeed = 0.6;
            }

            if (species.equals("timburr")) {
                if (maxReachBonus < 0.5) maxReachBonus = 0.5;
                if (maxBreakSpeedBonus < 0.25) maxBreakSpeedBonus = 0.25;
            } else if (species.equals("gurdurr")) {
                if (maxReachBonus < 1.0) maxReachBonus = 1.0;
                if (maxBreakSpeedBonus < 0.50) maxBreakSpeedBonus = 0.50;
            } else if (species.equals("conkeldurr")) {
                if (maxReachBonus < 1.5) maxReachBonus = 1.5;
                if (maxBreakSpeedBonus < 0.75) maxBreakSpeedBonus = 0.75;
            } else if (species.equals("ferroseed")) {
                if (maxThornsDamage < 1.5f) maxThornsDamage = 1.5f;
            } else if (species.equals("ferrothorn")) {
                if (maxThornsDamage < 3.0f) maxThornsDamage = 3.0f;
                hasFerrothornPoison = true;
            } else if (species.equals("charmander")) {
                if (maxFireAspectSec < 3) maxFireAspectSec = 3;
            } else if (species.equals("charmeleon")) {
                if (maxFireAspectSec < 5) maxFireAspectSec = 5;
            } else if (species.equals("charizard")) {
                if (maxFireAspectSec < 7) maxFireAspectSec = 7;
            } else if (species.equals("tyrogue")) {
                if (maxBareHandDmg < 2.0f) maxBareHandDmg = 2.0f;
            } else if (species.equals("hitmonchan") || species.equals("hitmonlee") || species.equals("hitmontop")) {
                if (maxBareHandDmg < 4.0f) maxBareHandDmg = 4.0f;
            } else if (species.equals("feebas") || species.equals("milotic")) {
                if (maxFishingLuck < 3) maxFishingLuck = 3;
                if (maxFishingLure < 3) maxFishingLure = 3;
            }
        }

        if (hasMew) {
            if (maxReachBonus > 0.0) maxReachBonus += 0.5;
            if (maxBreakSpeedBonus > 0.0) maxBreakSpeedBonus += 0.25;
            if (maxThornsDamage > 0.0f) maxThornsDamage += 1.5f;
            if (maxFireAspectSec > 0) maxFireAspectSec += 2;
            if (maxBareHandDmg > 0.0f) maxBareHandDmg += 2.0f;
            if (maxFishingLuck > 0) maxFishingLuck += 1;
            if (maxFishingLure > 0) maxFishingLure += 1;
        }

        if (hasSharpedo) {
            boolean inWater = player.isInWater()
                    || player.isEyeInFluid(FluidTags.WATER)
                    || player.level().getFluidState(player.blockPosition()).is(FluidTags.WATER)
                    || player.level().getFluidState(BlockPos.containing(player.getX(), player.getEyeY(), player.getZ())).is(FluidTags.WATER);
            if (inWater) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false, PassiveConfig.showStatusEffectIcons));
            }
        }

        int cropTickInterval = Math.max(1, (int) Math.round(minCropIntervalSec * 20.0));
        if (hasMew) cropTickInterval = Math.max(1, cropTickInterval / 2);

        if (hasCelebi && player.tickCount % cropTickInterval == 0) {
            if (player.level() instanceof ServerLevel world) {
                BlockPos playerPos = player.blockPosition();
                int radius = maxCropRadius > 0 ? maxCropRadius : 8;
                List<BlockPos> detectedCrops = new ArrayList<>();
                BlockPos minPos = playerPos.offset(-radius, -2, -radius);
                BlockPos maxPos = playerPos.offset(radius, 2, radius);

                for (BlockPos targetPos : BlockPos.betweenClosed(minPos, maxPos)) {
                    BlockState state = world.getBlockState(targetPos);
                    Block block = state.getBlock();
                    boolean isCropOrSapling = block instanceof BonemealableBlock
                            || block instanceof CropBlock
                            || block instanceof SaplingBlock
                            || block instanceof StemBlock
                            || block instanceof SweetBerryBushBlock
                            || block instanceof CocoaBlock
                            || block instanceof NetherWartBlock;
                    if (isCropOrSapling) {
                        detectedCrops.add(targetPos.immutable());
                    }
                }

                if (detectedCrops.size() > minCropsThreshold) {
                    for (BlockPos cropPos : detectedCrops) {
                        BlockState state = world.getBlockState(cropPos);
                        if (maxCropChance >= 1.0 || world.random.nextDouble() < maxCropChance) {
                            state.randomTick(world, cropPos, world.getRandom());
                        }
                        if (world.random.nextFloat() < 0.25f) {
                            world.sendParticles(ParticleTypes.HAPPY_VILLAGER, cropPos.getX() + 0.5, cropPos.getY() + 0.5, cropPos.getZ() + 0.5, 1, 0.2, 0.2, 0.2, 0.05);
                        }
                    }
                }
            }
        }

        if (maxLifeSteal > 0.0f) {
            float effLifeSteal = maxLifeSteal + (hasMew ? mewLifeStealBonus : 0.0f);
            PLAYER_LIFE_STEAL.put(player.getUUID(), effLifeSteal);
        } else {
            PLAYER_LIFE_STEAL.remove(player.getUUID());
        }

        if (maxVineClimb > 0.0) {
            PLAYER_VINE_CLIMB.put(player.getUUID(), maxVineClimb);
        } else {
            PLAYER_VINE_CLIMB.remove(player.getUUID());
        }

        if (maxFireProt > 0) {
            int effLevel = maxFireProt + (hasMew ? 1 : 0);
            PLAYER_FIRE_PROT_LEVEL.put(player.getUUID(), effLevel);
            updatePlayerBurningTime(player, effLevel);
        } else {
            PLAYER_FIRE_PROT_LEVEL.remove(player.getUUID());
            updatePlayerBurningTime(player, 0);
        }

        if (maxLavaStriderSec > 0) {
            int maxTicks = (maxLavaStriderSec + (hasMew ? 1 : 0)) * 20;
            PLAYER_LAVA_MAX_TICKS.put(player.getUUID(), maxTicks);
            PLAYER_LAVA_REMAINING_TICKS.putIfAbsent(player.getUUID(), maxTicks);
        } else {
            PLAYER_LAVA_MAX_TICKS.remove(player.getUUID());
            PLAYER_LAVA_REMAINING_TICKS.remove(player.getUUID());
        }

        if (maxRespiration > 0) {
            updatePlayerOxygenBonus(player, maxRespiration + (hasMew ? 1 : 0));
        } else {
            updatePlayerOxygenBonus(player, 0);
        }

        if (maxSneakSpeed > 0.0) {
            double effSneak = maxSneakSpeed + (hasMew ? 0.2 : 0.0);
            updatePlayerSneakSpeed(player, effSneak);
        } else {
            updatePlayerSneakSpeed(player, 0.0);
        }

        updatePlayerReach(player, maxReachBonus);
        updatePlayerBreakSpeed(player, maxBreakSpeedBonus);
        if (maxBreakSpeedBonus > 0.0) {
            PLAYER_BREAK_SPEED.put(player.getUUID(), (float) maxBreakSpeedBonus);
        } else {
            PLAYER_BREAK_SPEED.remove(player.getUUID());
        }

        if (maxThornsDamage > 0.0f) {
            PLAYER_THORNS_DAMAGE.put(player.getUUID(), maxThornsDamage);
        } else {
            PLAYER_THORNS_DAMAGE.remove(player.getUUID());
        }
        if (hasFerrothornPoison) {
            PLAYER_FERROTHORN_POISON.put(player.getUUID(), true);
        } else {
            PLAYER_FERROTHORN_POISON.remove(player.getUUID());
        }

        if (maxFireAspectSec > 0) {
            PLAYER_FIRE_ASPECT_SEC.put(player.getUUID(), maxFireAspectSec);
        } else {
            PLAYER_FIRE_ASPECT_SEC.remove(player.getUUID());
        }

        if (maxBareHandDmg > 0.0f) {
            PLAYER_BARE_HAND_DMG.put(player.getUUID(), maxBareHandDmg);
        } else {
            PLAYER_BARE_HAND_DMG.remove(player.getUUID());
        }

        if (maxFishingLuck > 0 || maxFishingLure > 0) {
            boolean holdingRod = player.getMainHandItem().getItem() instanceof net.minecraft.world.item.FishingRodItem
                    || player.getOffhandItem().getItem() instanceof net.minecraft.world.item.FishingRodItem
                    || player.fishing != null;
            if (holdingRod) {
                int amp = Math.max(0, maxFishingLuck - 1);
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 220, amp, true, false, PassiveConfig.showStatusEffectIcons));
            }
            if (player.fishing != null && maxFishingLure > 0 && FIELD_TIME_UNTIL_LURED != null) {
                try {
                    int current = FIELD_TIME_UNTIL_LURED.getInt(player.fishing);
                    if (current > 20) {
                        int reduction = Math.min(maxFishingLure, 3);
                        FIELD_TIME_UNTIL_LURED.setInt(player.fishing, Math.max(20, current - reduction));
                    }
                } catch (Throwable ignored) {}
            }
            PLAYER_FISHING_LUCK.put(player.getUUID(), maxFishingLuck);
            PLAYER_FISHING_LURE.put(player.getUUID(), maxFishingLure);
        } else {
            PLAYER_FISHING_LUCK.remove(player.getUUID());
            PLAYER_FISHING_LURE.remove(player.getUUID());
        }
    }

    public static void cleanupPlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        PLAYER_FIRE_PROT_LEVEL.remove(uuid);
        PLAYER_LAVA_MAX_TICKS.remove(uuid);
        PLAYER_LAVA_REMAINING_TICKS.remove(uuid);
        PLAYER_LIFE_STEAL.remove(uuid);
        PLAYER_VINE_CLIMB.remove(uuid);
        PLAYER_BREAK_SPEED.remove(uuid);
        PLAYER_THORNS_DAMAGE.remove(uuid);
        PLAYER_FERROTHORN_POISON.remove(uuid);
        PLAYER_FIRE_ASPECT_SEC.remove(uuid);
        PLAYER_BARE_HAND_DMG.remove(uuid);
        PLAYER_FISHING_LUCK.remove(uuid);
        PLAYER_FISHING_LURE.remove(uuid);
        updatePlayerBurningTime(player, 0);
        updatePlayerOxygenBonus(player, 0);
        updatePlayerSneakSpeed(player, 0.0);
        updatePlayerReach(player, 0.0);
        updatePlayerBreakSpeed(player, 0.0);
    }

    private static void updatePlayerSneakSpeed(ServerPlayer player, double bonus) {
        AttributeInstance inst = player.getAttribute(Attributes.SNEAKING_SPEED);
        if (inst != null) {
            AttributeModifier currentMod = inst.getModifier(SNEAK_SPEED_ID);
            if (currentMod != null) {
                if (Math.abs(currentMod.amount() - bonus) < 0.001) {
                    return;
                }
                inst.removeModifier(SNEAK_SPEED_ID);
            }
            if (bonus > 0.0) {
                inst.addPermanentModifier(new AttributeModifier(SNEAK_SPEED_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    private static void updatePlayerBurningTime(ServerPlayer player, int level) {
        AttributeInstance inst = player.getAttribute(Attributes.BURNING_TIME);
        if (inst != null) {
            inst.removeModifier(BURNING_TIME_ID);
            if (level > 0) {
                double reduction = Math.max(-0.85, -0.15 * (double) level);
                inst.addPermanentModifier(new AttributeModifier(BURNING_TIME_ID, reduction, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
    }

    private static void updatePlayerOxygenBonus(ServerPlayer player, int level) {
        AttributeInstance inst = player.getAttribute(Attributes.OXYGEN_BONUS);
        if (inst != null) {
            inst.removeModifier(OXYGEN_BONUS_ID);
            if (level > 0) {
                inst.addPermanentModifier(new AttributeModifier(OXYGEN_BONUS_ID, (double) level, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    private static void updatePlayerReach(ServerPlayer player, double bonus) {
        AttributeInstance blockReach = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (blockReach != null) {
            blockReach.removeModifier(BLOCK_REACH_ID);
            if (bonus > 0.0) {
                blockReach.addPermanentModifier(new AttributeModifier(BLOCK_REACH_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }
        AttributeInstance entityReach = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (entityReach != null) {
            entityReach.removeModifier(ENTITY_REACH_ID);
            if (bonus > 0.0) {
                entityReach.addPermanentModifier(new AttributeModifier(ENTITY_REACH_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    private static void updatePlayerBreakSpeed(ServerPlayer player, double bonus) {
        AttributeInstance breakSpeed = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        if (breakSpeed != null) {
            breakSpeed.removeModifier(BREAK_SPEED_ID);
            if (bonus > 0.0) {
                breakSpeed.addPermanentModifier(new AttributeModifier(BREAK_SPEED_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    public static float getPlayerBreakSpeedBonus(Player player) {
        if (player == null) return 0.0f;
        Float b = PLAYER_BREAK_SPEED.get(player.getUUID());
        if (b != null && b > 0.0f) return b;
        if (player.level().isClientSide()) {
            try {
                Iterable<Pokemon> party = getClientParty();
                if (party != null) {
                    float max = 0.0f;
                    for (Pokemon p : party) {
                        if (!isPokemonActive(player, p)) continue;
                        String sp = p.getSpecies().getName().toLowerCase();
                        if (sp.equals("conkeldurr")) max = Math.max(max, 0.75f);
                        else if (sp.equals("gurdurr")) max = Math.max(max, 0.50f);
                        else if (sp.equals("timburr")) max = Math.max(max, 0.25f);
                    }
                    return max;
                }
            } catch (Throwable ignored) {}
        }
        return 0.0f;
    }

    public static float getPlayerThornsDamage(Player player) {
        if (player == null) return 0.0f;
        return PLAYER_THORNS_DAMAGE.getOrDefault(player.getUUID(), 0.0f);
    }

    public static boolean hasFerrothornPoison(Player player) {
        if (player == null) return false;
        return PLAYER_FERROTHORN_POISON.getOrDefault(player.getUUID(), false);
    }

    public static int getPlayerFireAspectSeconds(Player player) {
        if (player == null) return 0;
        return PLAYER_FIRE_ASPECT_SEC.getOrDefault(player.getUUID(), 0);
    }

    public static float getPlayerBareHandBonusDamage(Player player) {
        if (player == null) return 0.0f;
        return PLAYER_BARE_HAND_DMG.getOrDefault(player.getUUID(), 0.0f);
    }

    public static boolean hasFishingBuff(Player player) {
        if (player == null) return false;
        return PLAYER_FISHING_LUCK.containsKey(player.getUUID()) || PLAYER_FISHING_LURE.containsKey(player.getUUID());
    }

    public static double getLavaVisionAmount() {
        try {
            Minecraft client = Minecraft.getInstance();
            if (client == null || client.player == null) return 0.0;
            LocalPlayer player = client.player;

            Iterable<Pokemon> party = getClientParty();
            if (party == null) return 0.0;

            boolean hasMew = false;
            for (Pokemon p : party) {
                if (isPokemonActive(player, p)) {
                    try {
                        if (p.getSpecies() != null && p.getSpecies().getName().equalsIgnoreCase("mew")) {
                            hasMew = true;
                            break;
                        }
                    } catch (Throwable ignored) {}
                }
            }

            double maxClear = 0.0;
            for (Pokemon pokemon : party) {
                if (!isPokemonActive(player, pokemon)) continue;
                try {
                    if (pokemon.getSpecies() == null || pokemon.getSpecies().getName() == null) continue;
                    String species = pokemon.getSpecies().getName().toLowerCase();
                    PassiveConfig.PassiveEntry entry = PassiveConfig.getPassive(species);
                    if (entry != null) {
                        if (entry.lavaFogClearPercent > 0.0) {
                            if (entry.lavaFogClearPercent > maxClear) {
                                maxClear = entry.lavaFogClearPercent;
                            }
                        } else if (entry.clearLavaVision) {
                            double def = (species.equals("slugma")) ? 0.5 : 1.0;
                            if (def > maxClear) maxClear = def;
                        }
                    }
                    if (species.equals("slugma")) {
                        if (maxClear < 0.5) maxClear = 0.5;
                    } else if (species.equals("magcargo") || species.equals("macargo")) {
                        if (maxClear < 1.0) maxClear = 1.0;
                    }
                } catch (Throwable ignored) {}
            }
            if (hasMew && maxClear > 0.0) {
                maxClear = 1.0;
            }
            return maxClear;
        } catch (Throwable ignored) {}
        return 0.0;
    }

    public static int getPlayerFortuneLevel(ServerPlayer player) {
        if (player == null) return 0;
        try {
            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
            if (party == null) return 0;

            boolean hasMew = false;
            for (Pokemon p : party) {
                if (isPokemonActive(player, p) && p.getSpecies() != null && p.getSpecies().getName().equalsIgnoreCase("mew")) {
                    hasMew = true;
                    break;
                }
            }

            int maxFortune = 0;
            for (Pokemon pokemon : party) {
                if (!isPokemonActive(player, pokemon)) continue;
                if (pokemon.getSpecies() == null || pokemon.getSpecies().getName() == null) continue;
                String species = pokemon.getSpecies().getName().toLowerCase();
                PassiveConfig.PassiveEntry entry = PassiveConfig.getPassive(species);
                if (entry != null && entry.fortuneLevel > 0) {
                    if (entry.fortuneLevel > maxFortune) {
                        maxFortune = entry.fortuneLevel;
                    }
                }
                if (species.equals("meowth")) {
                    if (maxFortune < 1) maxFortune = 1;
                } else if (species.equals("persian")) {
                    if (maxFortune < 3) maxFortune = 3;
                }
            }

            if (maxFortune > 0 && hasMew) {
                maxFortune += 1;
            }
            return maxFortune;
        } catch (Throwable t) {
            return 0;
        }
    }

    public static boolean isPokemonActive(Player player, Pokemon pokemon) {
        if (pokemon == null) return false;
        if (PassiveConfig.requireNotFainted && pokemon.isFainted()) return false;
        if (PassiveConfig.requireSentOut) {
            PokemonState state = pokemon.getState();
            boolean isSentOut = (state instanceof SentOutState) || (state instanceof com.cobblemon.mod.common.pokemon.activestate.ActivePokemonState);
            boolean isShouldered = PassiveConfig.allowShoulderState() && (state instanceof ShoulderedState || (player != null && isPokemonOnPlayerShoulder(player, pokemon)));
            return isSentOut || isShouldered;
        }
        return true;
    }

    public static boolean hasClearLavaVision() {
        return getLavaVisionAmount() > 0.0;
    }

    private static boolean isPokemonOnPlayerShoulder(Player player, Pokemon pokemon) {
        return matchesShoulderNbt(player.getShoulderEntityLeft(), pokemon) || matchesShoulderNbt(player.getShoulderEntityRight(), pokemon);
    }

    private static boolean matchesShoulderNbt(CompoundTag shoulderNbt, Pokemon pokemon) {
        if (shoulderNbt == null || shoulderNbt.isEmpty()) return false;
        if (shoulderNbt.contains("Pokemon")) {
            CompoundTag pokeNbt = shoulderNbt.getCompound("Pokemon");
            if (pokeNbt.hasUUID("UUID") && pokeNbt.getUUID("UUID").equals(pokemon.getUuid())) {
                return true;
            }
            if (pokeNbt.contains("Species")) {
                String sp = pokeNbt.getString("Species").toLowerCase();
                return sp.endsWith(":" + pokemon.getSpecies().getName().toLowerCase())
                        || sp.equals(pokemon.getSpecies().getName().toLowerCase());
            }
        }
        return false;
    }

    /**
     * Safely retrieves the client player's party across Cobblemon 1.6, 1.7, and 1.8+
     * using dynamic reflection fallback without throwing NoSuchMethodError.
     */
    @SuppressWarnings("unchecked")
    public static Iterable<Pokemon> getClientParty() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return null;

        try {
            Object storage = CobblemonClient.INSTANCE.getStorage();
            if (storage != null) {
                try {
                    java.lang.reflect.Method m = storage.getClass().getMethod("getParty");
                    Object partyObj = m.invoke(storage);
                    if (partyObj instanceof Iterable) {
                        return (Iterable<Pokemon>) partyObj;
                    }
                } catch (NoSuchMethodException ignored) {
                    try {
                        java.lang.reflect.Method m = storage.getClass().getMethod("getMyParty");
                        Object partyObj = m.invoke(storage);
                        if (partyObj instanceof Iterable) {
                            return (Iterable<Pokemon>) partyObj;
                        }
                    } catch (Throwable ignored2) {}
                }
            }
        } catch (Throwable ignored) {}

        if (client.player != null && client.getSingleplayerServer() != null) {
            try {
                ServerPlayer sp = client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());
                if (sp != null) {
                    return Cobblemon.INSTANCE.getStorage().getParty(sp);
                }
            } catch (Throwable ignored) {}
        }

        return null;
    }
}
