package com.cobblepassive;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblepassive.skill.ExcadrillDrillSkill;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.math.Transformation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Brightness;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblePassive {
    public static final String MOD_ID = "cobblepassive";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Holder<MobEffect> WEB_IMMUNITY;

    private static final Map<UUID, Long> HUNGER_TIMERS = new HashMap<>();
    private static final Map<UUID, Long> LAST_RADAR_PING = new HashMap<>();
    private static final Map<UUID, Map<BlockPos, ActivePing>> PLAYER_ACTIVE_PINGS = new HashMap<>();

    public static void init() {
        LOGGER.info("[CobblePassive] Initializing CobblePassive common logic...");
        PassiveConfig.load();
    }

    public static void onPlayerTick(ServerPlayer player) {
        ExcadrillDrillSkill.tickPlayer(player);
        com.cobblepassive.skill.RillaboomTerrainSkill.tickPlayer(player);
        CobblePassiveHelper.onPlayerTick(player);
        if (CobblePassiveHelper.hasLavaStrider(player)) {
            CobblePassiveHelper.tickLavaStrider(player);
        }
        applyPartyPassives(player);
    }

    public static void applyPartyPassives(ServerPlayer player) {
        PlayerPartyStore party;
        try {
            party = Cobblemon.INSTANCE.getStorage().getParty(player);
        } catch (Exception e) {
            return;
        }

        boolean hasMew = false;
        List<PassiveConfig.PassiveEntry> activePassives = new ArrayList<>();

        for (Pokemon pokemon : party) {
            if (!CobblePassiveHelper.isPokemonActive(player, pokemon)) continue;
            String species = pokemon.getSpecies().getName().toLowerCase();
            PassiveConfig.PassiveEntry entry = PassiveConfig.getPassive(species);
            if (entry != null) {
                if (entry.isMewDnaResonance || species.equals("mew")) hasMew = true;
                activePassives.add(entry);
            }
        }

        boolean isNight = isNightTime(player.level());
        String biomeName = getPlayerBiomeString(player);

        for (PassiveConfig.PassiveEntry entry : activePassives) {
            for (PassiveConfig.PotionEffect pe : entry.effects) {
                if (pe.nightOnly() && !isNight) continue;
                if (pe.biomeRequired() != null && !biomeName.contains(pe.biomeRequired())) continue;
                if (pe.itemInHand() != null && !isHoldingItem(player, pe.itemInHand())) continue;
                if (pe.maxY() != null && player.getBlockY() > pe.maxY()) continue;

                int amp = pe.amplifier();
                if (hasMew && !entry.isMewDnaResonance) {
                    amp++;
                }
                player.addEffect(new MobEffectInstance(pe.effect(), 220, amp, true, false, PassiveConfig.showStatusEffectIcons));
            }

            if (entry.webImmunity && WEB_IMMUNITY != null) {
                player.addEffect(new MobEffectInstance(WEB_IMMUNITY, 60, 0, true, false, false));
            }

            if (entry.hungerFeedIntervalSeconds > 0) {
                long now = System.currentTimeMillis();
                long lastFeed = HUNGER_TIMERS.getOrDefault(player.getUUID(), 0L);
                if (now - lastFeed >= entry.hungerFeedIntervalSeconds * 1000L) {
                    if (player.getFoodData().needsFood()) {
                        player.getFoodData().eat(entry.hungerFeedAmount, 0.5f);
                    }
                    HUNGER_TIMERS.put(player.getUUID(), now);
                }
            }

            if (entry.fastDetox) {
                applyFastDetox(player, (entry.fastDetoxMaxDurationSeconds > 0 ? entry.fastDetoxMaxDurationSeconds : 3) * 20);
            }

            if (entry.mobGlowingRadius > 0) {
                int glowInterval = entry.mobGlowingIntervalSeconds > 0 ? entry.mobGlowingIntervalSeconds * 20 : 40;
                if (player.tickCount % glowInterval == 0) {
                    tickMobGlowing(player, entry.mobGlowingRadius + (hasMew ? 5 : 0), entry.mobGlowingNightOnly, isNight);
                }
            }
        }

        PassiveConfig.PassiveEntry radarEntry = null;
        for (PassiveConfig.PassiveEntry entry : activePassives) {
            if (entry.oreRadarRadius > 0) {
                radarEntry = entry;
                break;
            }
        }
        if (radarEntry != null) {
            tickOreRadar(player, radarEntry, hasMew);
        } else {
            removePlayerPings(player.getUUID());
        }
        tickExpiredPings(player);
    }

    public static void cleanupPlayerRadar(UUID playerId, MinecraftServer server) {
        removePlayerPings(playerId);
    }

    public static void onBlockBroken(ServerLevel world, ServerPlayer player, BlockPos pos, BlockState state) {
        int passiveFortune = CobblePassiveHelper.getPlayerFortuneLevel(player);
        if (passiveFortune > 0) {
            List<ItemStack> drops = Block.getDrops(state, world, pos, world.getBlockEntity(pos), player, player.getMainHandItem());
            if (!drops.isEmpty()) {
                for (ItemStack drop : drops) {
                    if (drop.getItem() != state.getBlock().asItem() && world.random.nextInt(passiveFortune + 2) > 0) {
                        Block.popResource(world, pos, drop.copyWithCount(1));
                    }
                }
            }
        }
    }

    public static void onAttackEntity(Player player, Level world, LivingEntity target) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        CobblePassiveHelper.applyLifeSteal(serverPlayer);
        if (target.isAlive()) {
            int fireSec = CobblePassiveHelper.getPlayerFireAspectSeconds(serverPlayer);
            if (fireSec > 0) {
                target.igniteForSeconds(fireSec);
                if (world instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
                }
                world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6f, 1.2f);
            }
            if (serverPlayer.getMainHandItem().isEmpty()) {
                float bonusDmg = CobblePassiveHelper.getPlayerBareHandBonusDamage(serverPlayer);
                if (bonusDmg > 0.0f) {
                    target.hurt(serverPlayer.damageSources().playerAttack(serverPlayer), bonusDmg);
                    if (world instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 8, 0.3, 0.3, 0.3, 0.15);
                    }
                    world.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 1.1f);
                }
            }
        }
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("summonpokemon")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.literal("list")
                                .executes(CobblePassive::cmdListPassives)
                        )
                        .then(Commands.argument("species", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(PassiveConfig.getAllPassives().keySet(), builder))
                                .executes(ctx -> cmdSummonPokemon(ctx, false))
                                .then(Commands.literal("spawn")
                                        .executes(ctx -> cmdSummonPokemon(ctx, true))
                                )
                                .then(Commands.literal("party")
                                        .executes(ctx -> cmdSummonPokemon(ctx, false))
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("summonpokemonall")
                        .requires(s -> s.hasPermission(2))
                        .executes(ctx -> cmdSummonPokemonAll(ctx, false))
                        .then(Commands.literal("party")
                                .executes(ctx -> cmdSummonPokemonAll(ctx, false))
                        )
                        .then(Commands.literal("spawn")
                                .executes(ctx -> cmdSummonPokemonAll(ctx, true))
                        )
        );

        dispatcher.register(
                Commands.literal("cobblepassive")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.literal("reload").executes(CobblePassive::cmdReload))
                        .then(Commands.literal("list").executes(CobblePassive::cmdListPassives))
                        .then(Commands.literal("summon")
                                .then(Commands.argument("species", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(PassiveConfig.getAllPassives().keySet(), builder))
                                        .executes(ctx -> cmdSummonPokemon(ctx, false))
                                        .then(Commands.literal("spawn")
                                                .executes(ctx -> cmdSummonPokemon(ctx, true))
                                        )
                                        .then(Commands.literal("party")
                                                .executes(ctx -> cmdSummonPokemon(ctx, false))
                                        )
                                )
                        )
                        .then(Commands.literal("summonall")
                                .executes(ctx -> cmdSummonPokemonAll(ctx, false))
                                .then(Commands.literal("party")
                                        .executes(ctx -> cmdSummonPokemonAll(ctx, false))
                                )
                                .then(Commands.literal("spawn")
                                        .executes(ctx -> cmdSummonPokemonAll(ctx, true))
                                )
                        )
        );
    }

    private static int cmdReload(CommandContext<CommandSourceStack> ctx) {
        PassiveConfig.load();
        ctx.getSource().sendSuccess(() -> Component.literal("§a[CobblePassive] Configuration reloaded successfully."), true);
        return 1;
    }

    private static int cmdSummonPokemon(CommandContext<CommandSourceStack> ctx, boolean spawnInWorld) {
        CommandSourceStack source = ctx.getSource();
        try {
            ServerPlayer player = source.getPlayerOrException();
            String species = StringArgumentType.getString(ctx, "species").toLowerCase();

            PassiveConfig.PassiveEntry entry = PassiveConfig.getPassive(species);
            if (entry == null) {
                source.sendFailure(Component.literal("§c[CobblePassive] Pokémon '" + species + "' does not have a registered passive. Use /summonpokemon list to view all."));
                return 0;
            }

            PokemonProperties properties = PokemonProperties.Companion.parse(species + " level=50");
            if (spawnInWorld) {
                PokemonEntity entity = properties.createEntity(player.serverLevel());
                entity.setPos(player.getX() + player.getLookAngle().x * 2.0, player.getY(), player.getZ() + player.getLookAngle().z * 2.0);
                player.serverLevel().addFreshEntity(entity);
                source.sendSuccess(() -> Component.literal("§a[CobblePassive] Summoned §e" + species.toUpperCase() + " §a(Lv. 50) in the world!"), true);
            } else {
                Pokemon pokemon = properties.create();
                boolean added = Cobblemon.INSTANCE.getStorage().getParty(player).add(pokemon);
                if (added) {
                    source.sendSuccess(() -> Component.literal("§a[CobblePassive] Added §e" + species.toUpperCase() + " §a(Lv. 50) to your party!"), true);
                } else {
                    Cobblemon.INSTANCE.getStorage().getPC(player).add(pokemon);
                    source.sendSuccess(() -> Component.literal("§e[CobblePassive] Party is full! §e" + species.toUpperCase() + " §ewas sent to your PC."), true);
                }
            }
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c[CobblePassive] Failed to summon Pokémon: " + e.getMessage()));
            return 0;
        }
    }

    private static int cmdSummonPokemonAll(CommandContext<CommandSourceStack> ctx, boolean spawnInWorld) {
        CommandSourceStack source = ctx.getSource();
        try {
            ServerPlayer player = source.getPlayerOrException();
            Set<String> speciesSet = PassiveConfig.getAllPassives().keySet();
            List<String> sorted = new ArrayList<>(speciesSet);
            Collections.sort(sorted);

            if (sorted.isEmpty()) {
                source.sendFailure(Component.literal("§c[CobblePassive] No passive Pokémon registered."));
                return 0;
            }

            int successCount = 0;
            int partyCount = 0;
            int pcCount = 0;

            PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
            var pc = Cobblemon.INSTANCE.getStorage().getPC(player);

            int total = sorted.size();
            for (int i = 0; i < total; i++) {
                String species = sorted.get(i);
                try {
                    PokemonProperties properties = PokemonProperties.Companion.parse(species + " level=50");
                    if (spawnInWorld) {
                        PokemonEntity entity = properties.createEntity(player.serverLevel());
                        double angle = (2.0 * Math.PI / total) * i;
                        double dist = 4.0 + (i % 3) * 1.5;
                        double x = player.getX() + Math.cos(angle) * dist;
                        double z = player.getZ() + Math.sin(angle) * dist;
                        entity.setPos(x, player.getY(), z);
                        player.serverLevel().addFreshEntity(entity);
                        successCount++;
                    } else {
                        Pokemon pokemon = properties.create();
                        boolean added = party.add(pokemon);
                        if (added) {
                            partyCount++;
                        } else {
                            pc.add(pokemon);
                            pcCount++;
                        }
                        successCount++;
                    }
                } catch (Exception ex) {
                    LOGGER.warn("[CobblePassive] Failed to summon species '{}': {}", species, ex.getMessage());
                }
            }

            if (spawnInWorld) {
                int finalCount = successCount;
                source.sendSuccess(() -> Component.literal("§a[CobblePassive] Summoned §e" + finalCount + " Pokémon §a(Lv. 50) around you!"), true);
            } else {
                int finalSuccess = successCount;
                int finalParty = partyCount;
                int finalPc = pcCount;
                source.sendSuccess(() -> Component.literal("§a[CobblePassive] Summoned §e" + finalSuccess + " Pokémon §a(Lv. 50)! (§e" + finalParty + " §ato Party, §e" + finalPc + " §ato PC)"), true);
            }
            return successCount;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§c[CobblePassive] Failed to execute /summonpokemonall: " + e.getMessage()));
            return 0;
        }
    }

    private static int cmdListPassives(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        Set<String> speciesList = PassiveConfig.getAllPassives().keySet();
        List<String> sorted = new ArrayList<>(speciesList);
        Collections.sort(sorted);

        source.sendSuccess(() -> Component.literal("§6§l=== CobblePassive Pokémon List (" + sorted.size() + " Total) ==="), false);
        source.sendSuccess(() -> Component.literal("§f" + String.join(", ", sorted)), false);
        source.sendSuccess(() -> Component.literal("§7Use §e/summonpokemon <name> §7or §e/summonpokemonall [party|spawn] §7to summon!"), false);
        return 1;
    }

    private static boolean isNightTime(Level world) {
        long time = world.getDayTime() % 24000;
        return !world.isDay() || (time >= 12500 && time <= 23500);
    }

    private static String getPlayerBiomeString(ServerPlayer player) {
        return player.level().getBiome(player.blockPosition()).unwrapKey()
                .map(k -> k.location().getPath().toLowerCase())
                .orElse("");
    }

    private static boolean isHoldingItem(ServerPlayer player, String itemStr) {
        ItemStack main = player.getMainHandItem();
        if (!main.isEmpty()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(main.getItem());
            if (id != null && (id.toString().equals(itemStr) || id.getPath().contains(itemStr))) return true;
        }
        return false;
    }

    private static void applyFastDetox(ServerPlayer player, int maxDurationTicks) {
        if (player.hasEffect(MobEffects.POISON)) {
            MobEffectInstance eff = player.getEffect(MobEffects.POISON);
            if (eff != null && eff.getDuration() > maxDurationTicks) {
                player.removeEffect(MobEffects.POISON);
                player.addEffect(new MobEffectInstance(MobEffects.POISON, maxDurationTicks, eff.getAmplifier(), eff.isAmbient(), eff.isVisible(), eff.showIcon()));
            }
        }
        if (player.hasEffect(MobEffects.WITHER)) {
            MobEffectInstance eff = player.getEffect(MobEffects.WITHER);
            if (eff != null && eff.getDuration() > maxDurationTicks) {
                player.removeEffect(MobEffects.WITHER);
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, maxDurationTicks, eff.getAmplifier(), eff.isAmbient(), eff.isVisible(), eff.showIcon()));
            }
        }
    }

    private static void tickMobGlowing(ServerPlayer player, int radius, boolean nightOnly, boolean isNight) {
        if (nightOnly && !isNight) return;
        int effRadius = radius > 0 ? radius : 10;
        AABB box = player.getBoundingBox().inflate((double) effRadius);
        List<LivingEntity> entities = player.serverLevel().getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> e != player && e.isAlive() && (e instanceof Mob)
        );
        for (LivingEntity mob : entities) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false, false));
        }
    }

    private static void tickOreRadar(ServerPlayer player, PassiveConfig.PassiveEntry entry, boolean hasMew) {
        long now = player.level().getGameTime();
        int radius = entry.oreRadarRadius + (hasMew ? 6 : 0);
        long intervalTicks = (long) entry.radarIntervalSeconds * 20L;
        long durationTicks = (long) entry.pingDurationSeconds * 20L;
        long lastPing = LAST_RADAR_PING.getOrDefault(player.getUUID(), 0L);
        if (now - lastPing < intervalTicks) return;

        LAST_RADAR_PING.put(player.getUUID(), now);
        if (!(player.level() instanceof ServerLevel world)) return;
        BlockPos center = player.blockPosition();
        List<OreTarget> targets = new ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos checkPos = center.offset(dx, dy, dz);
                    BlockState state = world.getBlockState(checkPos);
                    if (entry.matchesBlock(state)) {
                        double distSq = center.distSqr(checkPos);
                        int distMeters = (int) Math.round(Math.sqrt(distSq));
                        targets.add(new OreTarget(checkPos.immutable(), state, distSq, distMeters));
                    }
                }
            }
        }

        if (!targets.isEmpty()) {
            targets.sort(Comparator.comparingDouble(OreTarget::distSq));
            int limit = entry.maxOresDetected > 0 ? entry.maxOresDetected : 8;
            int count = Math.min(limit, targets.size());
            Map<BlockPos, ActivePing> playerPings = PLAYER_ACTIVE_PINGS.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
            long expireAt = now + durationTicks;

            for (int i = 0; i < count; i++) {
                OreTarget target = targets.get(i);
                ActivePing existing = playerPings.get(target.pos);
                if (existing != null && existing.isValid()) {
                    existing.update(target.distMeters, expireAt);
                } else {
                    if (existing != null) existing.discard();
                    ActivePing created = spawnOrePingDisplay(world, target.pos, target.state, target.distMeters, expireAt);
                    if (created != null) {
                        playerPings.put(target.pos, created);
                    }
                }
            }

            world.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS,
                    0.5f,
                    1.6f
            );
        }
    }

    private static Method METHOD_SET_BLOCK_STATE;
    private static Method METHOD_GET_BLOCK_STATE;
    private static Method METHOD_SET_BRIGHTNESS;
    private static Method METHOD_SET_GLOW_COLOR;
    private static Method METHOD_SET_TRANSFORMATION;
    private static Method METHOD_SET_BILLBOARD;
    private static Method METHOD_SET_TEXT;
    private static Method METHOD_SET_BACKGROUND_COLOR;
    private static Method METHOD_SET_FLAGS;

    static {
        try {
            METHOD_SET_BLOCK_STATE = Display.BlockDisplay.class.getDeclaredMethod("setBlockState", BlockState.class);
            METHOD_SET_BLOCK_STATE.setAccessible(true);
            METHOD_GET_BLOCK_STATE = Display.BlockDisplay.class.getDeclaredMethod("getBlockState");
            METHOD_GET_BLOCK_STATE.setAccessible(true);

            METHOD_SET_BRIGHTNESS = Display.class.getDeclaredMethod("setBrightnessOverride", Brightness.class);
            METHOD_SET_BRIGHTNESS.setAccessible(true);
            METHOD_SET_GLOW_COLOR = Display.class.getDeclaredMethod("setGlowColorOverride", int.class);
            METHOD_SET_GLOW_COLOR.setAccessible(true);
            METHOD_SET_TRANSFORMATION = Display.class.getDeclaredMethod("setTransformation", Transformation.class);
            METHOD_SET_TRANSFORMATION.setAccessible(true);
            METHOD_SET_BILLBOARD = Display.class.getDeclaredMethod("setBillboardConstraints", Display.BillboardConstraints.class);
            METHOD_SET_BILLBOARD.setAccessible(true);

            METHOD_SET_TEXT = Display.TextDisplay.class.getDeclaredMethod("setText", Component.class);
            METHOD_SET_TEXT.setAccessible(true);
            METHOD_SET_BACKGROUND_COLOR = Display.TextDisplay.class.getDeclaredMethod("setBackgroundColor", int.class);
            METHOD_SET_BACKGROUND_COLOR.setAccessible(true);
            METHOD_SET_FLAGS = Display.TextDisplay.class.getDeclaredMethod("setFlags", byte.class);
            METHOD_SET_FLAGS.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("[CobblePassive] Failed to reflect Display entity methods", e);
        }
    }

    private static void setDisplayBlockState(Display.BlockDisplay display, BlockState state) {
        try {
            if (METHOD_SET_BLOCK_STATE != null) METHOD_SET_BLOCK_STATE.invoke(display, state);
        } catch (Exception ignored) {}
    }

    private static BlockState getDisplayBlockState(Display.BlockDisplay display) {
        try {
            if (METHOD_GET_BLOCK_STATE != null) return (BlockState) METHOD_GET_BLOCK_STATE.invoke(display);
        } catch (Exception ignored) {}
        return Blocks.AIR.defaultBlockState();
    }

    private static void setDisplayBrightness(Display display, Brightness brightness) {
        try {
            if (METHOD_SET_BRIGHTNESS != null) METHOD_SET_BRIGHTNESS.invoke(display, brightness);
        } catch (Exception ignored) {}
    }

    private static void setDisplayGlowColor(Display display, int color) {
        try {
            if (METHOD_SET_GLOW_COLOR != null) METHOD_SET_GLOW_COLOR.invoke(display, color);
        } catch (Exception ignored) {}
    }

    private static void setDisplayTransformation(Display display, Transformation transformation) {
        try {
            if (METHOD_SET_TRANSFORMATION != null) METHOD_SET_TRANSFORMATION.invoke(display, transformation);
        } catch (Exception ignored) {}
    }

    private static void setDisplayBillboard(Display display, Display.BillboardConstraints constraints) {
        try {
            if (METHOD_SET_BILLBOARD != null) METHOD_SET_BILLBOARD.invoke(display, constraints);
        } catch (Exception ignored) {}
    }

    private static void setTextDisplayText(Display.TextDisplay display, Component text) {
        try {
            if (METHOD_SET_TEXT != null) METHOD_SET_TEXT.invoke(display, text);
        } catch (Exception ignored) {}
    }

    private static void setTextDisplayBackground(Display.TextDisplay display, int color) {
        try {
            if (METHOD_SET_BACKGROUND_COLOR != null) METHOD_SET_BACKGROUND_COLOR.invoke(display, color);
        } catch (Exception ignored) {}
    }

    private static void setTextDisplaySeeThrough(Display.TextDisplay display, boolean seeThrough) {
        try {
            if (METHOD_SET_FLAGS != null) {
                byte flags = seeThrough ? Display.TextDisplay.FLAG_SEE_THROUGH : 0;
                METHOD_SET_FLAGS.invoke(display, flags);
            }
        } catch (Exception ignored) {}
    }

    private static ActivePing spawnOrePingDisplay(ServerLevel world, BlockPos pos, BlockState state, int distMeters, long expireTime) {
        try {
            OreColorStyle style = getOreStyle(state);
            String oreName = state.getBlock().getName().getString();
            PlayerTeam team = getOrCreateOreTeam(world.getServer(), style);

            Display.BlockDisplay blockDisplay = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, world);
            blockDisplay.setPos(pos.getX() + 0.002, pos.getY() + 0.002, pos.getZ() + 0.002);
            setDisplayBlockState(blockDisplay, state);
            setDisplayBrightness(blockDisplay, new Brightness(15, 15));
            blockDisplay.setGlowingTag(true);
            setDisplayGlowColor(blockDisplay, style.glowColorHex);
            setDisplayTransformation(blockDisplay, new Transformation(
                    new Vector3f(0.0f, 0.0f, 0.0f),
                    new Quaternionf(),
                    new Vector3f(0.996f, 0.996f, 0.996f),
                    new Quaternionf()
            ));
            blockDisplay.addTag("CobblePassivePing");
            world.addFreshEntity(blockDisplay);
            if (team != null) {
                world.getServer().getScoreboard().addPlayerToTeam(blockDisplay.getScoreboardName(), team);
            }

            Display.TextDisplay textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, world);
            textDisplay.setPos(pos.getX() + 0.5, pos.getY() + 0.65, pos.getZ() + 0.5);
            setTextDisplayText(textDisplay, Component.literal(style.formatCode + "❖ " + oreName + " §7[" + distMeters + "m]"));
            setDisplayBillboard(textDisplay, Display.BillboardConstraints.CENTER);
            setTextDisplaySeeThrough(textDisplay, true);
            setTextDisplayBackground(textDisplay, 0x55000000);
            setDisplayBrightness(textDisplay, new Brightness(15, 15));
            setDisplayTransformation(textDisplay, new Transformation(
                    new Vector3f(0.0f, 0.0f, 0.0f),
                    new Quaternionf(),
                    new Vector3f(0.75f, 0.75f, 0.75f),
                    new Quaternionf()
            ));
            textDisplay.addTag("CobblePassivePing");
            world.addFreshEntity(textDisplay);

            return new ActivePing(pos, blockDisplay, textDisplay, expireTime);
        } catch (Exception e) {
            LOGGER.error("[CobblePassive] Error spawning Ore Ping Display", e);
            return null;
        }
    }

    private static PlayerTeam getOrCreateOreTeam(MinecraftServer server, OreColorStyle style) {
        try {
            Scoreboard scoreboard = server.getScoreboard();
            String teamName = "cp_ore_" + Integer.toHexString(style.glowColorHex);
            PlayerTeam team = scoreboard.getPlayerTeam(teamName);
            if (team == null) {
                team = scoreboard.addPlayerTeam(teamName);
                ChatFormatting fmt = getFormattingFromColor(style.glowColorHex);
                team.setColor(fmt);
            }
            return team;
        } catch (Exception e) {
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
        if (state.is(BlockTags.DIAMOND_ORES)) return new OreColorStyle("§b", 0x55FFFF);
        if (state.is(BlockTags.EMERALD_ORES)) return new OreColorStyle("§a", 0x55FF55);
        if (state.is(BlockTags.GOLD_ORES)) return new OreColorStyle("§6", 0xFFAA00);
        if (state.is(BlockTags.REDSTONE_ORES)) return new OreColorStyle("§c", 0xFF5555);
        if (state.is(BlockTags.LAPIS_ORES)) return new OreColorStyle("§9", 0x5555FF);
        if (state.is(Blocks.ANCIENT_DEBRIS)) return new OreColorStyle("§4", 0xAA0000);
        if (state.is(BlockTags.IRON_ORES)) return new OreColorStyle("§f", 0xFFFFFF);
        if (state.is(BlockTags.COPPER_ORES)) return new OreColorStyle("§e", 0xFFFF55);
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().toLowerCase();
        if (id.contains("diamond")) return new OreColorStyle("§b", 0x55FFFF);
        if (id.contains("emerald")) return new OreColorStyle("§a", 0x55FF55);
        if (id.contains("gold")) return new OreColorStyle("§6", 0xFFAA00);
        if (id.contains("redstone")) return new OreColorStyle("§c", 0xFF5555);
        if (id.contains("lapis")) return new OreColorStyle("§9", 0x5555FF);
        if (id.contains("debris")) return new OreColorStyle("§4", 0xAA0000);
        if (id.contains("quartz")) return new OreColorStyle("§f", 0xFFFFFF);
        return new OreColorStyle("§e", 0xFFFF55);
    }

    private static void tickExpiredPings(ServerPlayer player) {
        Map<BlockPos, ActivePing> pings = PLAYER_ACTIVE_PINGS.get(player.getUUID());
        if (pings == null || pings.isEmpty()) return;
        long now = player.level().getGameTime();
        Level world = player.level();
        Iterator<Map.Entry<BlockPos, ActivePing>> it = pings.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, ActivePing> entry = it.next();
            ActivePing ping = entry.getValue();
            BlockState state = world.getBlockState(ping.pos);
            if (now >= ping.expireTime || state.isAir() || !ping.isValid()) {
                ping.discard();
                it.remove();
            }
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
            BlockState state = getDisplayBlockState(this.blockDisplay);
            OreColorStyle style = CobblePassive.getOreStyle(state);
            String oreName = state.getBlock().getName().getString();
            setTextDisplayText(this.textDisplay, Component.literal(style.formatCode() + "❖ " + oreName + " §7[" + distMeters + "m]"));
        }
    }

    public record OreColorStyle(String formatCode, int glowColorHex) {}

    record OreTarget(BlockPos pos, BlockState state, double distSq, int distMeters) {}
}
