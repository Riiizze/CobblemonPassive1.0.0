package com.cobblepassive.neoforge;

import com.cobblepassive.CobblePassive;
import com.cobblepassive.WebImmunityEffect;
import com.cobblepassive.neoforge.client.CobblePassiveNeoForgeClient;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod("cobblepassive")
public class CobblePassiveNeoForge {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, CobblePassive.MOD_ID);
    public static final DeferredHolder<MobEffect, WebImmunityEffect> WEB_IMMUNITY = MOB_EFFECTS.register("web_immunity", WebImmunityEffect::new);

    public CobblePassiveNeoForge(IEventBus modEventBus) {
        CobblePassive.LOGGER.info("[CobblePassive] Initializing NeoForge adapter layer...");
        MOB_EFFECTS.register(modEventBus);
        CobblePassive.WEB_IMMUNITY = WEB_IMMUNITY;

        CobblePassive.init();

        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, this::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(AttackEntityEvent.class, this::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(LivingDamageEvent.Pre.class, this::onLivingDamage);
        NeoForge.EVENT_BUS.addListener(BlockDropsEvent.class, this::onBlockDrops);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.BreakSpeed.class, this::onBreakSpeed);
        NeoForge.EVENT_BUS.addListener(ItemFishedEvent.class, this::onItemFished);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, this::onRegisterCommands);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            CobblePassiveNeoForgeClient.init();
        }
    }

    private void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CobblePassive.onPlayerTick(player);
        }
    }

    private void onAttackEntity(AttackEntityEvent event) {
        CobblePassive.onAttackEntity(event.getEntity(), event.getTarget());
    }

    private void onLivingDamage(LivingDamageEvent.Pre event) {
        float newDamage = CobblePassive.onLivingDamage(event.getEntity(), event.getSource(), event.getNewDamage());
        event.setNewDamage(newDamage);
    }

    private void onBlockDrops(BlockDropsEvent event) {
        if (event.getBreaker() instanceof ServerPlayer player && event.getLevel() instanceof ServerLevel level) {
            CobblePassive.onBlockDrops(level, event.getPos(), event.getState(), event.getDrops(), player, event.getTool());
        }
    }

    private void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        event.setNewSpeed(CobblePassive.onBreakSpeed(event.getEntity(), event.getState(), event.getNewSpeed()));
    }

    private void onItemFished(ItemFishedEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CobblePassive.onItemFished(player, event.getDrops(), event.getHookEntity());
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CobblePassive.registerCommands(event.getDispatcher());
    }
}
