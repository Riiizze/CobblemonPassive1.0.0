package com.cobblepassive.fabric;

import com.cobblepassive.CobblePassive;
import com.cobblepassive.WebImmunityEffect;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public class CobblePassiveFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CobblePassive.LOGGER.info("[CobblePassive] Initializing Fabric adapter layer...");

        CobblePassive.WEB_IMMUNITY = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(CobblePassive.MOD_ID, "web_immunity"),
                new WebImmunityEffect()
        );

        CobblePassive.init();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                CobblePassive.onPlayerTick(player);
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            CobblePassive.onAttackEntity(player, entity);
            return InteractionResult.PASS;
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            CobblePassive.onLivingDamage(entity, source, amount);
            return true;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CobblePassive.registerCommands(dispatcher);
        });
    }
}
