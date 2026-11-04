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

@Mixin(Entity.class)
public abstract class EntitySlowMovementMixin {
    @Shadow
    public float fallDistance;

    @Inject(method = "makeStuckInBlock", at = @At("HEAD"), cancellable = true)
    private void onMakeStuckInBlock(BlockState state, Vec3 multiplier, CallbackInfo ci) {
        if (state.is(Blocks.COBWEB)) {
            if ((Object) this instanceof Player player) {
                if (CobblePassive.WEB_IMMUNITY != null && player.hasEffect(CobblePassive.WEB_IMMUNITY)) {
                    this.fallDistance = 0.0f;
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "isUnderWater", at = @At("HEAD"), cancellable = true)
    private void onIsUnderWater(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player) {
            if (player.getVehicle() instanceof PokemonEntity) {
                if (player.isEyeInFluid(FluidTags.WATER)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
