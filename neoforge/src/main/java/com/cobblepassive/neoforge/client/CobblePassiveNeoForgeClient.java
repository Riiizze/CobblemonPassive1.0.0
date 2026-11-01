package com.cobblepassive.neoforge.client;

import com.cobblemon.mod.common.client.gui.summary.Summary;
import com.cobblepassive.CobblePassive;
import com.cobblepassive.CobblePassiveHelper;
import com.cobblepassive.client.CobblePassiveUiConfigScreen;
import com.cobblepassive.client.PassiveTooltipHelper;
import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.material.FogType;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

public class CobblePassiveNeoForgeClient {

    public static void init() {
        CobblePassive.LOGGER.info("[CobblePassive] Registering NeoForge client listeners...");
        NeoForge.EVENT_BUS.addListener(ViewportEvent.RenderFog.class, CobblePassiveNeoForgeClient::onRenderFog);
        NeoForge.EVENT_BUS.addListener(ViewportEvent.ComputeFogColor.class, CobblePassiveNeoForgeClient::onComputeFogColor);
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, CobblePassiveNeoForgeClient::onRegisterClientCommands);
        NeoForge.EVENT_BUS.addListener(ScreenEvent.MouseButtonPressed.Pre.class, CobblePassiveNeoForgeClient::onScreenMouseClicked);
    }

    private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cpui").executes(ctx -> {
            Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new CobblePassiveUiConfigScreen(null)));
            return 1;
        }));
        event.getDispatcher().register(Commands.literal("cobblepassiveui").executes(ctx -> {
            Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new CobblePassiveUiConfigScreen(null)));
            return 1;
        }));
    }

    private static void onScreenMouseClicked(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() instanceof Summary) {
            if (PassiveTooltipHelper.handleMouseClicked(event.getMouseX(), event.getMouseY(), event.getButton())) {
                event.setCanceled(true);
            }
        }
    }

    public static void onRenderFog(ViewportEvent.RenderFog event) {
        double visionAmount;
        if ((event.getType() == FogType.LAVA || event.getCamera().getFluidInCamera() == FogType.LAVA) && (visionAmount = CobblePassiveHelper.getLavaVisionAmount()) > 0.0) {
            float viewDistance = Minecraft.getInstance().gameRenderer.getRenderDistance();
            float farPlane = Math.max(viewDistance * 0.8f, 64.0f);
            if (visionAmount < 1.0) {
                farPlane = (float)Math.max((double)viewDistance * ((double)0.8f * visionAmount), 32.0 * visionAmount);
            }
            event.setNearPlaneDistance(-8.0f);
            event.setFarPlaneDistance(farPlane);
            event.setFogShape(FogShape.SPHERE);
            event.setCanceled(true);
        }
    }

    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        double visionAmount;
        if (event.getCamera().getFluidInCamera() == FogType.LAVA && (visionAmount = CobblePassiveHelper.getLavaVisionAmount()) > 0.0) {
            float factor = (float)Math.min(1.0, visionAmount);
            event.setRed(0.85f * factor + event.getRed() * (1.0f - factor));
            event.setGreen(0.5f * factor + event.getGreen() * (1.0f - factor));
            event.setBlue(0.2f * factor + event.getBlue() * (1.0f - factor));
        }
    }
}
