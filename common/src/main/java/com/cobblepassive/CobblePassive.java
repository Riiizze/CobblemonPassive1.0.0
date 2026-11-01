/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.cobblemon.mod.common.Cobblemon
 *  com.cobblemon.mod.common.api.pokemon.PokemonProperties
 *  com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
 *  com.cobblemon.mod.common.api.storage.pc.PCStore
 *  com.cobblemon.mod.common.entity.pokemon.PokemonEntity
 *  com.cobblemon.mod.common.pokemon.Pokemon
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.math.Transformation
 *  net.minecraft.ChatFormatting
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.commands.SharedSuggestionProvider
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.ServerScoreboard
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.util.Brightness
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Display
 *  net.minecraft.world.entity.Display$BillboardConstraints
 *  net.minecraft.world.entity.Display$BlockDisplay
 *  net.minecraft.world.entity.Display$TextDisplay
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.FishingHook
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.scores.PlayerTeam
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.common.Mod
 *  net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.neoforged.fml.loading.FMLEnvironment
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.event.RegisterCommandsEvent
 *  net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Pre
 *  net.neoforged.neoforge.event.entity.player.AttackEntityEvent
 *  net.neoforged.neoforge.event.entity.player.ItemFishedEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$BreakSpeed
 *  net.neoforged.neoforge.event.level.BlockDropsEvent
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.cobblepassive;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblepassive.CobblePassiveHelper;
import com.cobblepassive.PassiveConfig;
import com.cobblepassive.WebImmunityEffect;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.math.Transformation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Brightness;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblePassive {
    public static final String MOD_ID = "cobblepassive";
    public static final Logger LOGGER = LoggerFactory.getLogger("cobblepassive");
    public static Holder<MobEffect> WEB_IMMUNITY;
    private static final Map<UUID, Long> HUNGER_TIMERS = new HashMap<UUID, Long>();
    private static final Map<UUID, Long> LAST_RADAR_PING = new HashMap<UUID, Long>();
    private static final Map<UUID, Map<BlockPos, ActivePing>> PLAYER_ACTIVE_PINGS = new HashMap<UUID, Map<BlockPos, ActivePing>>();
    private static Method METHOD_SET_BLOCK_STATE;
    private static Method METHOD_GET_BLOCK_STATE;
    private static Method METHOD_SET_BRIGHTNESS;
    private static Method METHOD_SET_GLOW_COLOR;
    private static Method METHOD_SET_TRANSFORMATION;
    private static Method METHOD_SET_BILLBOARD;
    private static Method METHOD_SET_TEXT;
    private static Method METHOD_SET_BACKGROUND_COLOR;
    private static Method METHOD_SET_FLAGS;

    public static void init() {
        LOGGER.info("[CobblePassive] Initializing CobblePassive common logic...");
        PassiveConfig.load();
    }

    public static void onPlayerTick(ServerPlayer player2) {
        CobblePassiveHelper.onPlayerTick(player2);
        if (CobblePassiveHelper.hasLavaStrider(player2)) {
            CobblePassiveHelper.tickLavaStrider(player2);
        }
        applyPartyPassives(player2);
    }

    public static void applyPartyPassives(ServerPlayer player) {
        PlayerPartyStore party;
        try {
            party = Cobblemon.INSTANCE.getStorage().getParty(player);
        }
        catch (Exception e) {
            return;
        }
        boolean hasMew = false;
        ArrayList<PassiveConfig.PassiveEntry> activePassives = new ArrayList<PassiveConfig.PassiveEntry>();
        for (Pokemon pokemon : party) {
            Object species;
            PassiveConfig.PassiveEntry entry;
            if (!CobblePassiveHelper.isPokemonActive((Player)player, pokemon) || (entry = PassiveConfig.getPassive((String)(species = pokemon.getSpecies().getName().toLowerCase()))) == null) continue;
            if (entry.isMewDnaResonance || ((String)species).equals("mew")) {
                hasMew = true;
            }
            activePassives.add(entry);
        }
        boolean isNight = CobblePassive.isNightTime(player.level());
        String biomeName = CobblePassive.getPlayerBiomeString(player);
        for (PassiveConfig.PassiveEntry entry : activePassives) {
            int glowInterval;
            long lastFeed;
            long now;
            for (PassiveConfig.PotionEffect pe : entry.effects) {
                if (pe.nightOnly() && !isNight || pe.biomeRequired() != null && !biomeName.contains(pe.biomeRequired()) || pe.itemInHand() != null && !CobblePassive.isHoldingItem(player, pe.itemInHand()) || pe.maxY() != null && player.getBlockY() > pe.maxY()) continue;
                int amp = pe.amplifier();
                if (hasMew && !entry.isMewDnaResonance) {
                    ++amp;
                }
                player.addEffect(new MobEffectInstance(pe.effect(), 220, amp, true, false, PassiveConfig.showStatusEffectIcons));
            }
            if (entry.webImmunity && WEB_IMMUNITY != null) {
                player.addEffect(new MobEffectInstance(WEB_IMMUNITY, 60, 0, true, false, false));
            }
            if (entry.hungerFeedIntervalSeconds > 0 && (now = System.currentTimeMillis()) - (lastFeed = HUNGER_TIMERS.getOrDefault(player.getUUID(), 0L).longValue()) >= (long)entry.hungerFeedIntervalSeconds * 1000L) {
                if (player.getFoodData().needsFood()) {
                    player.getFoodData().eat(entry.hungerFeedAmount, 0.5f);
                }
                HUNGER_TIMERS.put(player.getUUID(), now);
            }
            if (entry.fastDetox) {
                applyFastDetox(player, (entry.fastDetoxMaxDurationSeconds > 0 ? entry.fastDetoxMaxDurationSeconds : 3) * 20);
            }
            if (entry.mobGlowingRadius <= 0 || player.tickCount % (glowInterval = entry.mobGlowingIntervalSeconds > 0 ? entry.mobGlowingIntervalSeconds * 20 : 40) != 0) continue;
            tickMobGlowing(player, entry.mobGlowingRadius + (hasMew ? 5 : 0), entry.mobGlowingNightOnly, isNight);
        }
        PassiveConfig.PassiveEntry radarEntry = null;
        for (PassiveConfig.PassiveEntry entry : activePassives) {
            if (entry.oreRadarRadius <= 0) continue;
            radarEntry = entry;
            break;
        }
        if (radarEntry != null) {
            tickOreRadar(player, radarEntry, hasMew);
        } else {
            removePlayerPings(player.getUUID());
        }
        tickExpiredPings(player);
    }

    public static void onAttackEntity(Player player, Entity target) {
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            CobblePassiveHelper.applyLifeSteal((Player)player2);
        }
    }

    public static float onLivingDamage(LivingEntity livingEntity, DamageSource source, float damage) {
        ServerLevel sl;
        Level level;
        Entity directAttacker;
        ServerPlayer player;
        float newDamage = damage;
        if (livingEntity instanceof ServerPlayer) {
            float thornsDmg;
            LivingEntity livingAttacker;
            player = (ServerPlayer)livingEntity;
            if (!source.is(DamageTypes.THORNS) && (directAttacker = source.getEntity()) instanceof LivingEntity && (livingAttacker = (LivingEntity)directAttacker) != player && livingAttacker.isAlive() && (thornsDmg = CobblePassiveHelper.getPlayerThornsDamage((Player)player)) > 0.0f) {
                livingAttacker.hurt(player.damageSources().thorns((Entity)player), thornsDmg);
                if (CobblePassiveHelper.hasFerrothornPoison((Player)player)) {
                    livingAttacker.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
                }
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.THORNS_HIT, SoundSource.PLAYERS, 1.0f, 1.0f);
                level = player.level();
                if (level instanceof ServerLevel) {
                    sl = (ServerLevel)level;
                    sl.sendParticles((ParticleOptions)ParticleTypes.DAMAGE_INDICATOR, livingAttacker.getX(), livingAttacker.getY() + (double)livingAttacker.getBbHeight() * 0.5, livingAttacker.getZ(), 5, 0.2, 0.2, 0.2, 0.1);
                }
            }
        }
        if (source != null && (directAttacker = source.getEntity()) instanceof ServerPlayer) {
            LivingEntity target;
            player = (ServerPlayer)directAttacker;
            CobblePassiveHelper.applyLifeSteal((Player)player);
            if (livingEntity != null && (target = livingEntity).isAlive()) {
                float bonusDmg;
                int fireSec = CobblePassiveHelper.getPlayerFireAspectSeconds((Player)player);
                if (fireSec > 0) {
                    target.igniteForSeconds((float)fireSec);
                    level = player.level();
                    if (level instanceof ServerLevel) {
                        sl = (ServerLevel)level;
                        sl.sendParticles((ParticleOptions)ParticleTypes.FLAME, target.getX(), target.getY() + (double)target.getBbHeight() * 0.5, target.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
                    }
                    player.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6f, 1.2f);
                }
                if (player.getMainHandItem().isEmpty() && (bonusDmg = CobblePassiveHelper.getPlayerBareHandBonusDamage((Player)player)) > 0.0f) {
                    newDamage += bonusDmg;
                    Level level2 = player.level();
                    if (level2 instanceof ServerLevel) {
                        ServerLevel sl2 = (ServerLevel)level2;
                        sl2.sendParticles((ParticleOptions)ParticleTypes.CRIT, target.getX(), target.getY() + (double)target.getBbHeight() * 0.5, target.getZ(), 8, 0.3, 0.3, 0.3, 0.15);
                    }
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 1.1f);
                }
            }
        }
        return newDamage;
    }

    public static float onBreakSpeed(Player player, BlockState state, float originalSpeed) {
        float bonus = CobblePassiveHelper.getPlayerBreakSpeedBonus(player);
        if (bonus > 0.0f) {
            return originalSpeed * (1.0f + bonus);
        }
        return originalSpeed;
    }

    public static void onItemFished(ServerPlayer player2, List<ItemStack> drops, Entity hook) {
        if (CobblePassiveHelper.hasFishingBuff((Player)player2)) {
            for (int i = 0; i < drops.size(); ++i) {
                ItemStack stack = (ItemStack)drops.get(i);
                if (!CobblePassive.isJunkFishingItem(stack)) continue;
                ItemStack treasure = CobblePassive.getRandomFishingTreasure(player2.serverLevel().random);
                drops.set(i, treasure);
            }
            if (player2.serverLevel().random.nextFloat() < 0.35f) {
                drops.add(new ItemStack((ItemLike)Items.SALMON, 1));
            }
            if (hook != null) {
                player2.serverLevel().sendParticles((ParticleOptions)ParticleTypes.SPLASH, hook.getX(), hook.getY(), hook.getZ(), 25, 0.5, 0.5, 0.5, 0.2);
                player2.serverLevel().sendParticles((ParticleOptions)ParticleTypes.HAPPY_VILLAGER, player2.getX(), player2.getY() + 1.2, player2.getZ(), 8, 0.3, 0.3, 0.3, 0.1);
            }
            player2.level().playSound(null, player2.getX(), player2.getY(), player2.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6f, 1.8f);
        }
    }

    public static void onBlockDrops(ServerLevel level, BlockPos pos, BlockState state, List<ItemEntity> drops, ServerPlayer player, ItemStack tool) {
        int passiveFortune = CobblePassiveHelper.getPlayerFortuneLevel(player);
        if (passiveFortune <= 0) {
            return;
        }
        if (hasSilkTouch(player, tool)) {
            return;
        }
        int toolFortune = getToolFortune(player, tool);
        int extraFortune = passiveFortune - toolFortune;
        if (extraFortune <= 0) {
            return;
        }
        boolean proc = false;
        Random rand = new Random();
        for (ItemEntity dropEntity2 : drops) {
            int bonusRoll;
            ItemStack dropStack;
            if (dropEntity2 == null || (dropStack = dropEntity2.getItem()).isEmpty()) continue;
            Item blockItem = state.getBlock().asItem();
            if (dropStack.getItem() == blockItem || (bonusRoll = rand.nextInt(extraFortune + 2) - 1) <= 0) continue;
            dropStack.grow(dropStack.getCount() * bonusRoll);
            proc = true;
        }
        if (proc) {
            level.sendParticles((ParticleOptions)ParticleTypes.HAPPY_VILLAGER, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.1);
        }
    }

    public static boolean hasSilkTouch(ServerPlayer player, ItemStack tool) {
        if (tool == null || tool.isEmpty()) {
            return false;
        }
        try {
            ItemEnchantments enchants = (ItemEnchantments)tool.getOrDefault(DataComponents.ENCHANTMENTS, (Object)ItemEnchantments.EMPTY);
            for (Holder holder : enchants.keySet()) {
                if (!holder.is(Enchantments.SILK_TOUCH)) continue;
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    public static int getToolFortune(ServerPlayer player, ItemStack tool) {
        if (tool == null || tool.isEmpty()) {
            return 0;
        }
        try {
            ItemEnchantments enchants = (ItemEnchantments)tool.getOrDefault(DataComponents.ENCHANTMENTS, (Object)ItemEnchantments.EMPTY);
            for (Holder holder : enchants.keySet()) {
                if (!holder.is(Enchantments.FORTUNE)) continue;
                return enchants.getLevel(holder);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return 0;
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"summonpokemon").requires(s -> s.hasPermission(2))).then(Commands.literal((String)"list").executes(CobblePassive::cmdListPassives))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument((String)"species", (ArgumentType)StringArgumentType.word()).suggests((ctx, builder) -> SharedSuggestionProvider.suggest(PassiveConfig.getAllPassives().keySet(), (SuggestionsBuilder)builder)).executes(ctx -> cmdSummonPokemon((CommandContext<CommandSourceStack>)ctx, false))).then(Commands.literal((String)"spawn").executes(ctx -> cmdSummonPokemon((CommandContext<CommandSourceStack>)ctx, true)))).then(Commands.literal((String)"party").executes(ctx -> cmdSummonPokemon((CommandContext<CommandSourceStack>)ctx, false)))));
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"summonpokemonall").requires(s -> s.hasPermission(2))).executes(ctx -> cmdSummonPokemonAll((CommandContext<CommandSourceStack>)ctx, false))).then(Commands.literal((String)"party").executes(ctx -> cmdSummonPokemonAll((CommandContext<CommandSourceStack>)ctx, false)))).then(Commands.literal((String)"spawn").executes(ctx -> cmdSummonPokemonAll((CommandContext<CommandSourceStack>)ctx, true))));
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)MOD_ID).requires(s -> s.hasPermission(2))).then(Commands.literal((String)"reload").executes(CobblePassive::cmdReload))).then(Commands.literal((String)"list").executes(CobblePassive::cmdListPassives))).then(Commands.literal((String)"summon").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument((String)"species", (ArgumentType)StringArgumentType.word()).suggests((ctx, builder) -> SharedSuggestionProvider.suggest(PassiveConfig.getAllPassives().keySet(), (SuggestionsBuilder)builder)).executes(ctx -> cmdSummonPokemon((CommandContext<CommandSourceStack>)ctx, false))).then(Commands.literal((String)"spawn").executes(ctx -> cmdSummonPokemon((CommandContext<CommandSourceStack>)ctx, true)))).then(Commands.literal((String)"party").executes(ctx -> cmdSummonPokemon((CommandContext<CommandSourceStack>)ctx, false)))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"summonall").executes(ctx -> cmdSummonPokemonAll((CommandContext<CommandSourceStack>)ctx, false))).then(Commands.literal((String)"party").executes(ctx -> cmdSummonPokemonAll((CommandContext<CommandSourceStack>)ctx, false)))).then(Commands.literal((String)"spawn").executes(ctx -> cmdSummonPokemonAll((CommandContext<CommandSourceStack>)ctx, true)))));
    }

    private static int cmdReload(CommandContext<CommandSourceStack> ctx) {
        PassiveConfig.load();
        ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal((String)"\u00a7a[CobblePassive] Configuration reloaded successfully."), true);
        return 1;
    }

    private static int cmdSummonPokemon(CommandContext<CommandSourceStack> ctx, boolean spawnInWorld) {
        CommandSourceStack source = (CommandSourceStack)ctx.getSource();
        try {
            ServerPlayer player = source.getPlayerOrException();
            String species = StringArgumentType.getString(ctx, (String)"species").toLowerCase();
            PassiveConfig.PassiveEntry entry = PassiveConfig.getPassive(species);
            if (entry == null) {
                source.sendFailure((Component)Component.literal((String)("\u00a7c[CobblePassive] Pok\u00e9mon '" + species + "' tidak memiliki pasif terdaftar. Gunakan /summonpokemon list untuk melihat daftar.")));
                return 0;
            }
            PokemonProperties properties = PokemonProperties.Companion.parse(species + " level=50");
            if (spawnInWorld) {
                PokemonEntity entity = properties.createEntity((Level)player.serverLevel());
                entity.setPos(player.getX() + player.getLookAngle().x * 2.0, player.getY(), player.getZ() + player.getLookAngle().z * 2.0);
                player.serverLevel().addFreshEntity((Entity)entity);
                source.sendSuccess(() -> Component.literal((String)("\u00a7a[CobblePassive] Berhasil meng-summon \u00a7e" + species.toUpperCase() + " \u00a7a(Lv. 50) di dunia!")), true);
            } else {
                Pokemon pokemon = properties.create();
                boolean added = Cobblemon.INSTANCE.getStorage().getParty(player).add(pokemon);
                if (added) {
                    source.sendSuccess(() -> Component.literal((String)("\u00a7a[CobblePassive] Berhasil menambahkan \u00a7e" + species.toUpperCase() + " \u00a7a(Lv. 50) langsung ke party Anda!")), true);
                } else {
                    Cobblemon.INSTANCE.getStorage().getPC(player).add(pokemon);
                    source.sendSuccess(() -> Component.literal((String)("\u00a7e[CobblePassive] Party penuh! \u00a7e" + species.toUpperCase() + " \u00a7etelah dimasukkan ke PC.")), true);
                }
            }
            return 1;
        }
        catch (Exception e) {
            source.sendFailure((Component)Component.literal((String)("\u00a7c[CobblePassive] Gagal meng-summon Pok\u00e9mon: " + e.getMessage())));
            return 0;
        }
    }

    private static int cmdSummonPokemonAll(CommandContext<CommandSourceStack> ctx, boolean spawnInWorld) {
        CommandSourceStack source = (CommandSourceStack)ctx.getSource();
        try {
            ServerPlayer player = source.getPlayerOrException();
            Set<String> speciesSet = PassiveConfig.getAllPassives().keySet();
            ArrayList<String> sorted = new ArrayList<String>(speciesSet);
            Collections.sort(sorted);
            if (sorted.isEmpty()) {
                source.sendFailure((Component)Component.literal((String)"\u00a7c[CobblePassive] Tidak ada Pok\u00e9mon dengan pasif terdaftar."));
                return 0;
            }
            int successCount = 0;
            int partyCount = 0;
            int pcCount = 0;
            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
            PCStore pc = Cobblemon.INSTANCE.getStorage().getPC(player);
            int total = sorted.size();
            for (int i = 0; i < total; ++i) {
                String species = (String)sorted.get(i);
                try {
                    PokemonProperties properties = PokemonProperties.Companion.parse(species + " level=50");
                    if (spawnInWorld) {
                        PokemonEntity entity = properties.createEntity((Level)player.serverLevel());
                        double angle = Math.PI * 2 / (double)total * (double)i;
                        double dist = 4.0 + (double)(i % 3) * 1.5;
                        double x = player.getX() + Math.cos(angle) * dist;
                        double z = player.getZ() + Math.sin(angle) * dist;
                        entity.setPos(x, player.getY(), z);
                        player.serverLevel().addFreshEntity((Entity)entity);
                        ++successCount;
                        continue;
                    }
                    Pokemon pokemon = properties.create();
                    boolean added = party.add(pokemon);
                    if (added) {
                        ++partyCount;
                    } else {
                        pc.add(pokemon);
                        ++pcCount;
                    }
                    ++successCount;
                    continue;
                }
                catch (Exception ex) {
                    LOGGER.warn("[CobblePassive] Gagal meng-summon species '{}': {}", (Object)species, (Object)ex.getMessage());
                }
            }
            if (spawnInWorld) {
                int finalCount = successCount;
                source.sendSuccess(() -> Component.literal((String)("\u00a7a[CobblePassive] Berhasil meng-summon \u00a7e" + finalCount + " Pok\u00e9mon \u00a7apasif (Lv. 50) di sekitar Anda!")), true);
            } else {
                int finalSuccess = successCount;
                int finalParty = partyCount;
                int finalPc = pcCount;
                source.sendSuccess(() -> Component.literal((String)("\u00a7a[CobblePassive] Berhasil meng-summon \u00a7e" + finalSuccess + " Pok\u00e9mon \u00a7apasif (Lv. 50)! (\u00a7e" + finalParty + " \u00a7ake Party, \u00a7e" + finalPc + " \u00a7ake PC)")), true);
            }
            return successCount;
        }
        catch (Exception e) {
            source.sendFailure((Component)Component.literal((String)("\u00a7c[CobblePassive] Gagal menjalankan /summonpokemonall: " + e.getMessage())));
            return 0;
        }
    }

    private static int cmdListPassives(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = (CommandSourceStack)ctx.getSource();
        Set<String> speciesList = PassiveConfig.getAllPassives().keySet();
        ArrayList<String> sorted = new ArrayList<String>(speciesList);
        Collections.sort(sorted);
        source.sendSuccess(() -> Component.literal((String)("\u00a76\u00a7l=== CobblePassive Pok\u00e9mon List (" + sorted.size() + " Total) ===")), false);
        source.sendSuccess(() -> Component.literal((String)("\u00a7f" + String.join((CharSequence)", ", sorted))), false);
        source.sendSuccess(() -> Component.literal((String)"\u00a77Ketik \u00a7e/summonpokemon <nama> \u00a77atau \u00a7e/summonpokemonall [party|spawn] \u00a77untuk meng-summon semua!"), false);
        return 1;
    }

    private static boolean isNightTime(Level world) {
        long time = world.getDayTime() % 24000L;
        return !world.isDay() || time >= 12500L && time <= 23500L;
    }

    private static String getPlayerBiomeString(ServerPlayer player) {
        return player.level().getBiome(player.blockPosition()).unwrapKey().map(k -> k.location().getPath().toLowerCase()).orElse("");
    }

    private static boolean isHoldingItem(ServerPlayer player, String itemStr) {
        ResourceLocation id;
        ItemStack main = player.getMainHandItem();
        return !main.isEmpty() && (id = BuiltInRegistries.ITEM.getKey((Object)main.getItem())) != null && (id.toString().equals(itemStr) || id.getPath().contains(itemStr));
    }

    private static boolean isPokemonOnPlayerShoulder(Player player, Pokemon pokemon) {
        return CobblePassive.matchesShoulderNbt(player.getShoulderEntityLeft(), pokemon) || CobblePassive.matchesShoulderNbt(player.getShoulderEntityRight(), pokemon);
    }

    private static boolean matchesShoulderNbt(CompoundTag shoulderNbt, Pokemon pokemon) {
        if (shoulderNbt == null || shoulderNbt.isEmpty()) {
            return false;
        }
        if (shoulderNbt.contains("Pokemon")) {
            CompoundTag pokeNbt = shoulderNbt.getCompound("Pokemon");
            if (pokeNbt.hasUUID("UUID") && pokeNbt.getUUID("UUID").equals(pokemon.getUuid())) {
                return true;
            }
            if (pokeNbt.contains("Species")) {
                String sp = pokeNbt.getString("Species").toLowerCase();
                return sp.endsWith(":" + pokemon.getSpecies().getName().toLowerCase()) || sp.equals(pokemon.getSpecies().getName().toLowerCase());
            }
        }
        return false;
    }

    private static void applyFastDetox(ServerPlayer player, int maxDurationTicks) {
        MobEffectInstance eff;
        if (player.hasEffect(MobEffects.POISON) && (eff = player.getEffect(MobEffects.POISON)) != null && eff.getDuration() > maxDurationTicks) {
            player.removeEffect(MobEffects.POISON);
            player.addEffect(new MobEffectInstance(MobEffects.POISON, maxDurationTicks, eff.getAmplifier(), eff.isAmbient(), eff.isVisible(), eff.showIcon()));
        }
        if (player.hasEffect(MobEffects.WITHER) && (eff = player.getEffect(MobEffects.WITHER)) != null && eff.getDuration() > maxDurationTicks) {
            player.removeEffect(MobEffects.WITHER);
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, maxDurationTicks, eff.getAmplifier(), eff.isAmbient(), eff.isVisible(), eff.showIcon()));
        }
    }

    private static void tickMobGlowing(ServerPlayer player, int radius, boolean nightOnly, boolean isNight) {
        if (nightOnly && !isNight) {
            return;
        }
        int effRadius = radius > 0 ? radius : 10;
        AABB box = player.getBoundingBox().inflate((double)effRadius);
        List entities = player.serverLevel().getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive() && e instanceof Mob);
        for (LivingEntity mob : (List<LivingEntity>)entities) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false, false));
        }
    }

    private static void tickOreRadar(ServerPlayer player, PassiveConfig.PassiveEntry entry, boolean hasMew) {
        long now = player.level().getGameTime();
        int radius = entry.oreRadarRadius + (hasMew ? 6 : 0);
        long intervalTicks = (long)entry.radarIntervalSeconds * 20L;
        long durationTicks = (long)entry.pingDurationSeconds * 20L;
        long lastPing = LAST_RADAR_PING.getOrDefault(player.getUUID(), 0L);
        if (now - lastPing < intervalTicks) {
            return;
        }
        LAST_RADAR_PING.put(player.getUUID(), now);
        Level level = player.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel world = (ServerLevel)level;
        BlockPos center = player.blockPosition();
        ArrayList<OreTarget> targets = new ArrayList<OreTarget>();
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dy = -radius; dy <= radius; ++dy) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    BlockPos checkPos = center.offset(dx, dy, dz);
                    BlockState state = world.getBlockState(checkPos);
                    if (!entry.matchesBlock(state)) continue;
                    double distSq = center.distSqr((Vec3i)checkPos);
                    int distMeters = (int)Math.round(Math.sqrt(distSq));
                    targets.add(new OreTarget(checkPos.immutable(), state, distSq, distMeters));
                }
            }
        }
        if (!targets.isEmpty()) {
            targets.sort(Comparator.comparingDouble(OreTarget::distSq));
            int limit = entry.maxOresDetected > 0 ? entry.maxOresDetected : 8;
            int count = Math.min(limit, targets.size());
            Map playerPings = PLAYER_ACTIVE_PINGS.computeIfAbsent(player.getUUID(), k -> new HashMap());
            long expireAt = now + durationTicks;
            for (int i = 0; i < count; ++i) {
                ActivePing created;
                OreTarget target = (OreTarget)targets.get(i);
                ActivePing existing = (ActivePing)playerPings.get(target.pos);
                if (existing != null && existing.isValid()) {
                    existing.update(target.distMeters, expireAt);
                    continue;
                }
                if (existing != null) {
                    existing.discard();
                }
                if ((created = spawnOrePingDisplay(world, target.pos, target.state, target.distMeters, expireAt)) == null) continue;
                playerPings.put(target.pos, created);
            }
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.6f);
        }
    }

    private static void setDisplayBlockState(Display.BlockDisplay display, BlockState state) {
        try {
            if (METHOD_SET_BLOCK_STATE != null) {
                METHOD_SET_BLOCK_STATE.invoke((Object)display, state);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static BlockState getDisplayBlockState(Display.BlockDisplay display) {
        try {
            if (METHOD_GET_BLOCK_STATE != null) {
                return (BlockState)METHOD_GET_BLOCK_STATE.invoke((Object)display, new Object[0]);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return Blocks.AIR.defaultBlockState();
    }

    private static void setDisplayBrightness(Display display, Brightness brightness) {
        try {
            if (METHOD_SET_BRIGHTNESS != null) {
                METHOD_SET_BRIGHTNESS.invoke((Object)display, brightness);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void setDisplayGlowColor(Display display, int color) {
        try {
            if (METHOD_SET_GLOW_COLOR != null) {
                METHOD_SET_GLOW_COLOR.invoke((Object)display, color);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void setDisplayTransformation(Display display, Transformation transformation) {
        try {
            if (METHOD_SET_TRANSFORMATION != null) {
                METHOD_SET_TRANSFORMATION.invoke((Object)display, transformation);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void setDisplayBillboard(Display display, Display.BillboardConstraints constraints) {
        try {
            if (METHOD_SET_BILLBOARD != null) {
                METHOD_SET_BILLBOARD.invoke((Object)display, constraints);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void setTextDisplayText(Display.TextDisplay display, Component text) {
        try {
            if (METHOD_SET_TEXT != null) {
                METHOD_SET_TEXT.invoke((Object)display, text);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void setTextDisplayBackground(Display.TextDisplay display, int color) {
        try {
            if (METHOD_SET_BACKGROUND_COLOR != null) {
                METHOD_SET_BACKGROUND_COLOR.invoke((Object)display, color);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void setTextDisplaySeeThrough(Display.TextDisplay display, boolean seeThrough) {
        try {
            if (METHOD_SET_FLAGS != null) {
                byte flags = seeThrough ? (byte)2 : 0;
                METHOD_SET_FLAGS.invoke((Object)display, flags);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static ActivePing spawnOrePingDisplay(ServerLevel world, BlockPos pos, BlockState state, int distMeters, long expireTime) {
        try {
            OreColorStyle style = CobblePassive.getOreStyle(state);
            String oreName = state.getBlock().getName().getString();
            PlayerTeam team = getOrCreateOreTeam(world.getServer(), style);
            Display.BlockDisplay blockDisplay = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, (Level)world);
            blockDisplay.setPos((double)pos.getX() + 0.002, (double)pos.getY() + 0.002, (double)pos.getZ() + 0.002);
            CobblePassive.setDisplayBlockState(blockDisplay, state);
            CobblePassive.setDisplayBrightness((Display)blockDisplay, new Brightness(15, 15));
            blockDisplay.setGlowingTag(true);
            CobblePassive.setDisplayGlowColor((Display)blockDisplay, style.glowColorHex);
            CobblePassive.setDisplayTransformation((Display)blockDisplay, new Transformation(new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(0.996f, 0.996f, 0.996f), new Quaternionf()));
            blockDisplay.addTag("CobblePassivePing");
            world.addFreshEntity((Entity)blockDisplay);
            if (team != null) {
                world.getServer().getScoreboard().addPlayerToTeam(blockDisplay.getScoreboardName(), team);
            }
            Display.TextDisplay textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, (Level)world);
            textDisplay.setPos((double)pos.getX() + 0.5, (double)pos.getY() + 0.65, (double)pos.getZ() + 0.5);
            CobblePassive.setTextDisplayText(textDisplay, (Component)Component.literal((String)(style.formatCode + "\u2756 " + oreName + " \u00a77[" + distMeters + "m]")));
            CobblePassive.setDisplayBillboard((Display)textDisplay, Display.BillboardConstraints.CENTER);
            CobblePassive.setTextDisplaySeeThrough(textDisplay, true);
            CobblePassive.setTextDisplayBackground(textDisplay, 0x55000000);
            CobblePassive.setDisplayBrightness((Display)textDisplay, new Brightness(15, 15));
            CobblePassive.setDisplayTransformation((Display)textDisplay, new Transformation(new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(0.75f, 0.75f, 0.75f), new Quaternionf()));
            textDisplay.addTag("CobblePassivePing");
            world.addFreshEntity((Entity)textDisplay);
            return new ActivePing(pos, blockDisplay, textDisplay, expireTime);
        }
        catch (Exception e) {
            LOGGER.error("[CobblePassive] Error spawning Ore Ping Display", (Throwable)e);
            return null;
        }
    }

    private static PlayerTeam getOrCreateOreTeam(MinecraftServer server, OreColorStyle style) {
        try {
            ServerScoreboard scoreboard = server.getScoreboard();
            String teamName = "cp_ore_" + Integer.toHexString(style.glowColorHex);
            PlayerTeam team = scoreboard.getPlayerTeam(teamName);
            if (team == null) {
                team = scoreboard.addPlayerTeam(teamName);
                ChatFormatting fmt = getFormattingFromColor(style.glowColorHex);
                team.setColor(fmt);
            }
            return team;
        }
        catch (Exception e) {
            return null;
        }
    }

    private static ChatFormatting getFormattingFromColor(int color) {
        return switch (color) {
            case 0x55FFFF -> ChatFormatting.AQUA;
            case 0x55FF55 -> ChatFormatting.GREEN;
            case 0xFFAA00 -> ChatFormatting.GOLD;
            case 0xFF5555 -> ChatFormatting.RED;
            case 0x5555FF -> ChatFormatting.BLUE;
            case 0xAA0000 -> ChatFormatting.DARK_RED;
            case 0xFFFF55 -> ChatFormatting.YELLOW;
            default -> ChatFormatting.WHITE;
        };
    }

    public static OreColorStyle getOreStyle(BlockState state) {
        if (state.is(BlockTags.DIAMOND_ORES)) {
            return new OreColorStyle("\u00a7b", 0x55FFFF);
        }
        if (state.is(BlockTags.EMERALD_ORES)) {
            return new OreColorStyle("\u00a7a", 0x55FF55);
        }
        if (state.is(BlockTags.GOLD_ORES)) {
            return new OreColorStyle("\u00a76", 0xFFAA00);
        }
        if (state.is(BlockTags.REDSTONE_ORES)) {
            return new OreColorStyle("\u00a7c", 0xFF5555);
        }
        if (state.is(BlockTags.LAPIS_ORES)) {
            return new OreColorStyle("\u00a79", 0x5555FF);
        }
        if (state.is(Blocks.ANCIENT_DEBRIS)) {
            return new OreColorStyle("\u00a74", 0xAA0000);
        }
        if (state.is(BlockTags.IRON_ORES)) {
            return new OreColorStyle("\u00a7f", 0xFFFFFF);
        }
        if (state.is(BlockTags.COPPER_ORES)) {
            return new OreColorStyle("\u00a7e", 0xFFFF55);
        }
        String id = BuiltInRegistries.BLOCK.getKey((Object)state.getBlock()).getPath().toLowerCase();
        if (id.contains("diamond")) {
            return new OreColorStyle("\u00a7b", 0x55FFFF);
        }
        if (id.contains("emerald")) {
            return new OreColorStyle("\u00a7a", 0x55FF55);
        }
        if (id.contains("gold")) {
            return new OreColorStyle("\u00a76", 0xFFAA00);
        }
        if (id.contains("redstone")) {
            return new OreColorStyle("\u00a7c", 0xFF5555);
        }
        if (id.contains("lapis")) {
            return new OreColorStyle("\u00a79", 0x5555FF);
        }
        if (id.contains("debris")) {
            return new OreColorStyle("\u00a74", 0xAA0000);
        }
        if (id.contains("quartz")) {
            return new OreColorStyle("\u00a7f", 0xFFFFFF);
        }
        return new OreColorStyle("\u00a7e", 0xFFFF55);
    }

    private static void tickExpiredPings(ServerPlayer player) {
        Map<BlockPos, ActivePing> pings = PLAYER_ACTIVE_PINGS.get(player.getUUID());
        if (pings == null || pings.isEmpty()) {
            return;
        }
        long now = player.level().getGameTime();
        Level world = player.level();
        Iterator<Map.Entry<BlockPos, ActivePing>> it = pings.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, ActivePing> entry = it.next();
            ActivePing ping = entry.getValue();
            BlockState state = world.getBlockState(ping.pos);
            if (now < ping.expireTime && !state.isAir() && ping.isValid()) continue;
            ping.discard();
            it.remove();
        }
    }

    private static void removePlayerPings(UUID playerId) {
        Map<BlockPos, ActivePing> pings = PLAYER_ACTIVE_PINGS.remove(playerId);
        if (pings != null) {
            for (ActivePing ping : pings.values()) {
                ping.discard();
            }
        }
    }

    static {
        try {
            METHOD_SET_BLOCK_STATE = Display.BlockDisplay.class.getDeclaredMethod("setBlockState", BlockState.class);
            METHOD_SET_BLOCK_STATE.setAccessible(true);
            METHOD_GET_BLOCK_STATE = Display.BlockDisplay.class.getDeclaredMethod("getBlockState", new Class[0]);
            METHOD_GET_BLOCK_STATE.setAccessible(true);
            METHOD_SET_BRIGHTNESS = Display.class.getDeclaredMethod("setBrightnessOverride", Brightness.class);
            METHOD_SET_BRIGHTNESS.setAccessible(true);
            METHOD_SET_GLOW_COLOR = Display.class.getDeclaredMethod("setGlowColorOverride", Integer.TYPE);
            METHOD_SET_GLOW_COLOR.setAccessible(true);
            METHOD_SET_TRANSFORMATION = Display.class.getDeclaredMethod("setTransformation", Transformation.class);
            METHOD_SET_TRANSFORMATION.setAccessible(true);
            METHOD_SET_BILLBOARD = Display.class.getDeclaredMethod("setBillboardConstraints", Display.BillboardConstraints.class);
            METHOD_SET_BILLBOARD.setAccessible(true);
            METHOD_SET_TEXT = Display.TextDisplay.class.getDeclaredMethod("setText", Component.class);
            METHOD_SET_TEXT.setAccessible(true);
            METHOD_SET_BACKGROUND_COLOR = Display.TextDisplay.class.getDeclaredMethod("setBackgroundColor", Integer.TYPE);
            METHOD_SET_BACKGROUND_COLOR.setAccessible(true);
            METHOD_SET_FLAGS = Display.TextDisplay.class.getDeclaredMethod("setFlags", Byte.TYPE);
            METHOD_SET_FLAGS.setAccessible(true);
        }
        catch (Exception e) {
            LOGGER.error("[CobblePassive] Failed to reflect Display entity methods", (Throwable)e);
        }
    }

    record OreTarget(BlockPos pos, BlockState state, double distSq, int distMeters) {
    }

    static class ActivePing {
        final BlockPos pos;
        final Display.BlockDisplay blockDisplay;
        final Display.TextDisplay textDisplay;
        long expireTime;

        ActivePing(BlockPos pos, Display.BlockDisplay blockDisplay, Display.TextDisplay textDisplay, long expireTime) {
            this.pos = pos;
            this.blockDisplay = blockDisplay;
            this.textDisplay = textDisplay;
            this.expireTime = expireTime;
        }

        boolean isValid() {
            return !this.blockDisplay.isRemoved() && !this.textDisplay.isRemoved();
        }

        void discard() {
            if (!this.blockDisplay.isRemoved()) {
                this.blockDisplay.discard();
            }
            if (!this.textDisplay.isRemoved()) {
                this.textDisplay.discard();
            }
        }

        void update(int distMeters, long expireTime) {
            this.expireTime = expireTime;
            BlockState state = CobblePassive.getDisplayBlockState(this.blockDisplay);
            OreColorStyle style = CobblePassive.getOreStyle(state);
            String oreName = state.getBlock().getName().getString();
            CobblePassive.setTextDisplayText(this.textDisplay, (Component)Component.literal((String)(style.formatCode() + "\u2756 " + oreName + " \u00a77[" + distMeters + "m]")));
        }
    }

    public record OreColorStyle(String formatCode, int glowColorHex) {
    }
}

