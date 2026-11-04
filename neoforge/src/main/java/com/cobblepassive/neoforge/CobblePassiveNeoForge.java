package com.cobblepassive.neoforge;

import com.cobblepassive.CobblePassive;
import com.cobblepassive.CobblePassiveHelper;
import com.cobblepassive.WebImmunityEffect;
import com.cobblepassive.client.ActiveSkillHudOverlay;
import com.cobblepassive.neoforge.client.CobblePassiveNeoForgeClient;
import com.cobblepassive.network.ActiveSkillCooldownPayload;
import com.cobblepassive.network.ActiveSkillPayload;
import com.cobblepassive.network.ExcadrillSkillPayload;
import com.cobblepassive.skill.ActiveSkillDispatcher;
import com.cobblepassive.skill.ExcadrillDrillSkill;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CobblePassive.MOD_ID)
public class CobblePassiveNeoForge {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, CobblePassive.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> WEB_IMMUNITY_HOLDER =
            MOB_EFFECTS.register("web_immunity", WebImmunityEffect::new);

    public CobblePassiveNeoForge(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
        modEventBus.addListener(this::onRegisterPayloads);

        CobblePassive.init();
        registerForgeEvents();

        if (FMLEnvironment.dist.isClient()) {
            CobblePassiveNeoForgeClient.init(modEventBus);
        }
    }

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // C2S Active Skill Packets
        registrar.playToServer(
                ActiveSkillPayload.TYPE,
                ActiveSkillPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        ActiveSkillDispatcher.activate(serverPlayer);
                    }
                })
        );

        registrar.playToServer(
                ExcadrillSkillPayload.TYPE,
                ExcadrillSkillPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        ExcadrillDrillSkill.activate(serverPlayer);
                    }
                })
        );

        // S2C Cooldown Sync Packet
        registrar.playToClient(
                ActiveSkillCooldownPayload.TYPE,
                ActiveSkillCooldownPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ActiveSkillHudOverlay.setCooldown(payload.cooldownRemainingMs(), payload.totalCooldownMs());
                })
        );
    }

    private void registerForgeEvents() {
        IEventBus bus = NeoForge.EVENT_BUS;
        bus.addListener(this::onRegisterCommands);
        bus.addListener(this::onPlayerLogout);
        bus.addListener(this::onServerTick);
        bus.addListener(this::onBlockBreak);
        bus.addListener(this::onAttackEntity);
        bus.addListener(this::onLivingIncomingDamage);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        CobblePassive.registerCommands(event.getDispatcher());
    }

    private void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp && sp.getServer() != null) {
            CobblePassive.cleanupPlayerRadar(sp.getUUID(), sp.getServer());
        }
    }

    private void onServerTick(ServerTickEvent.Post event) {
        if (CobblePassive.WEB_IMMUNITY == null && WEB_IMMUNITY_HOLDER.isBound()) {
            CobblePassive.WEB_IMMUNITY = WEB_IMMUNITY_HOLDER;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            CobblePassive.onPlayerTick(player);
        }
    }

    private void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!player.level().isClientSide() && player.level() instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            CobblePassive.onBlockBroken(serverLevel, serverPlayer, event.getPos(), event.getState());
        }
    }

    private void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() && event.getTarget() instanceof LivingEntity target && !player.isSpectator()) {
            CobblePassive.onAttackEntity(player, player.level(), target);
        }
    }

    private void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            CobblePassiveHelper.onPlayerDamaged(player, event.getSource(), event.getAmount());
        }
    }
}
