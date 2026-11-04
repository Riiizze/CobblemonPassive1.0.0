package com.cobblepassive.fabric;

import com.cobblepassive.CobblePassive;
import com.cobblepassive.CobblePassiveHelper;
import com.cobblepassive.WebImmunityEffect;
import com.cobblepassive.network.ActiveSkillCooldownPayload;
import com.cobblepassive.network.ActiveSkillPayload;
import com.cobblepassive.network.ExcadrillSkillPayload;
import com.cobblepassive.skill.ActiveSkillDispatcher;
import com.cobblepassive.skill.ExcadrillDrillSkill;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class CobblePassiveFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CobblePassive.WEB_IMMUNITY = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(CobblePassive.MOD_ID, "web_immunity"),
                new WebImmunityEffect()
        );

        CobblePassive.init();
        registerNetworkPayloads();
        registerEvents();
    }

    private void registerNetworkPayloads() {
        // C2S Active Skill Packets
        PayloadTypeRegistry.playC2S().register(ActiveSkillPayload.TYPE, ActiveSkillPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ActiveSkillPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> ActiveSkillDispatcher.activate(player));
        });

        PayloadTypeRegistry.playC2S().register(ExcadrillSkillPayload.TYPE, ExcadrillSkillPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ExcadrillSkillPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> ExcadrillDrillSkill.activate(player));
        });

        // S2C Cooldown Sync Packet
        PayloadTypeRegistry.playS2C().register(ActiveSkillCooldownPayload.TYPE, ActiveSkillCooldownPayload.STREAM_CODEC);
    }

    private void registerEvents() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                CobblePassive.registerCommands(dispatcher));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                CobblePassive.cleanupPlayerRadar(handler.getPlayer().getUUID(), server));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                CobblePassive.onPlayerTick(player);
            }
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClientSide() && world instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
                CobblePassive.onBlockBroken(serverLevel, serverPlayer, pos, state);
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && entity instanceof LivingEntity target && !player.isSpectator()) {
                CobblePassive.onAttackEntity(player, world, target);
            }
            return InteractionResult.PASS;
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof Player player && !entity.level().isClientSide()) {
                CobblePassiveHelper.onPlayerDamaged(player, source, amount);
            }
            return true;
        });
    }
}
