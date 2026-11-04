package com.cobblepassive.mixin;

import com.cobblepassive.CobblePassiveHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
    private void onCanStandOnFluid(FluidState state, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player) {
            if (CobblePassiveHelper.hasLavaStrider(player) && !player.isCrouching()) {
                if (state.is(FluidTags.LAVA)) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At("RETURN"), cancellable = true)
    private void onDamageAfterArmorAbsorb(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        if (source != null && source.getEntity() instanceof Player player) {
            CobblePassiveHelper.applyLifeSteal(player);
        }

        if ((Object) this instanceof ServerPlayer player) {
            if (source.is(DamageTypeTags.IS_FIRE)) {
                int fireProt = CobblePassiveHelper.getFireProtectionLevel(player);
                if (fireProt > 0) {
                    float reduction = Math.min(0.80f, (float) fireProt * 0.08f);
                    cir.setReturnValue(cir.getReturnValue() * (1.0f - reduction));
                }
            }
        }
    }

    @Inject(method = "handleRelativeFrictionAndCalculateMovement", at = @At("RETURN"), cancellable = true)
    private void cobblepassive$boostVineClimbing(Vec3 motion, float friction, CallbackInfoReturnable<Vec3> cir) {
        if ((Object) this instanceof Player player) {
            Vec3 res = cir.getReturnValue();
            if (res != null && res.y > 0.0) {
                double bonus = CobblePassiveHelper.getVineClimbBonus(player);
                if (bonus > 0.0) {
                    BlockState blockState = ((LivingEntity) (Object) this).getBlockStateOn();
                    ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
                    if (blockId != null && blockId.getPath().contains("vine")) {
                        cir.setReturnValue(new Vec3(res.x, res.y * (1.0 + bonus), res.z));
                    }
                }
            }
        }
    }
}
