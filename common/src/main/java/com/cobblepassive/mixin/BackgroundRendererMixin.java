/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.Camera
 *  net.minecraft.client.renderer.FogRenderer
 *  net.minecraft.client.renderer.FogRenderer$FogMode
 *  net.minecraft.world.level.material.FogType
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.cobblepassive.mixin;

import com.cobblepassive.CobblePassiveHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={FogRenderer.class})
public class BackgroundRendererMixin {
    @Inject(method={"setupFog"}, at={@At(value="TAIL")})
    private static void cobblepassive$clearLavaFog(Camera camera, FogRenderer.FogMode fogMode, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        if (camera.getFluidInCamera() == FogType.LAVA) {
            double visionAmount = CobblePassiveHelper.getLavaVisionAmount();
            if (visionAmount >= 1.0) {
                RenderSystem.setShaderFogStart((float)-8.0f);
                RenderSystem.setShaderFogEnd((float)Math.max(viewDistance * 0.8f, 64.0f));
            } else if (visionAmount > 0.0) {
                RenderSystem.setShaderFogStart((float)-2.0f);
                RenderSystem.setShaderFogEnd((float)((float)Math.max((double)viewDistance * ((double)0.8f * visionAmount), 32.0 * visionAmount)));
            }
        }
    }
}

