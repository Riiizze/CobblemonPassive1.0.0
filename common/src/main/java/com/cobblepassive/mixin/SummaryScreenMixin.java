/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.cobblemon.mod.common.client.gui.summary.Summary
 *  com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget
 *  com.cobblemon.mod.common.pokemon.Pokemon
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.cobblepassive.mixin;

import com.cobblemon.mod.common.client.gui.summary.Summary;
import com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblepassive.client.PassiveTooltipHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Summary.class})
public abstract class SummaryScreenMixin
extends Screen {
    @Shadow(remap=false)
    private ModelWidget modelWidget;
    @Shadow(remap=false)
    private Pokemon selectedPokemon;

    protected SummaryScreenMixin(Component title) {
        super(title);
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void onRenderSummaryTail(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.selectedPokemon != null && this.modelWidget != null) {
            PassiveTooltipHelper.renderPassiveSummaryIcon(context, this.font, this.modelWidget, this.selectedPokemon, mouseX, mouseY, this.width, this.height);
        }
    }

    @Inject(method={"keyPressed"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSummaryKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (PassiveTooltipHelper.handleKeyPressed(keyCode, scanCode, modifiers)) {
            cir.setReturnValue((Object)true);
        }
    }
}

