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

@Mixin(Summary.class)
public abstract class SummaryScreenMixin extends Screen {
    @Shadow(remap = false)
    private ModelWidget modelWidget;

    @Shadow(remap = false)
    private Pokemon selectedPokemon;

    protected SummaryScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderSummaryTail(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.selectedPokemon != null && this.modelWidget != null) {
            PassiveTooltipHelper.renderPassiveSummaryIcon(
                    context,
                    this.font,
                    this.modelWidget,
                    this.selectedPokemon,
                    mouseX,
                    mouseY,
                    this.width,
                    this.height
            );
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onSummaryKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (PassiveTooltipHelper.handleKeyPressed(keyCode, scanCode, modifiers)) {
            cir.setReturnValue(true);
        }
    }
}
