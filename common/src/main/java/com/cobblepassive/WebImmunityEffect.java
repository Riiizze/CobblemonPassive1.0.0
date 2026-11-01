package com.cobblepassive;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class WebImmunityEffect extends MobEffect {

    public WebImmunityEffect() {
        super(MobEffectCategory.BENEFICIAL, 5636351);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Web Immune");
    }

    @Override
    public String getDescriptionId() {
        return "effect.cobblepassive.web_immunity";
    }
}
