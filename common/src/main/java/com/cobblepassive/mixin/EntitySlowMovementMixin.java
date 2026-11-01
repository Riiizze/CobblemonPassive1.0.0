/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.cobblemon.mod.common.entity.pokemon.PokemonEntity
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.cobblepassive.mixin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblepassive.CobblePassive;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Entity.class})
public abstract class EntitySlowMovementMixin {
    @Shadow
    public float fallDistance;

    @Inject(method={"makeStuckInBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void onMakeStuckInBlock(BlockState state, Vec3 multiplier, CallbackInfo ci) {
        EntitySlowMovementMixin entitySlowMovementMixin;
        if (state.is(Blocks.COBWEB) && (entitySlowMovementMixin = this) instanceof Player) {
            Player player = (Player)entitySlowMovementMixin;
            if (CobblePassive.WEB_IMMUNITY != null && player.hasEffect(CobblePassive.WEB_IMMUNITY)) {
                this.fallDistance = 0.0f;
                ci.cancel();
            }
        }
    }

    @Inject(method={"isUnderWater"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsUnderWater(CallbackInfoReturnable<Boolean> cir) {
        Player player;
        EntitySlowMovementMixin entitySlowMovementMixin = this;
        if (entitySlowMovementMixin instanceof Player && (player = (Player)entitySlowMovementMixin).getVehicle() instanceof PokemonEntity && player.isEyeInFluid(FluidTags.WATER)) {
            cir.setReturnValue((Object)true);
        }
    }
}

