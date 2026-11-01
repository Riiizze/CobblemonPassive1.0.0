/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.tags.FluidTags
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
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

@Mixin(value={LivingEntity.class})
public abstract class LivingEntityMixin {
    @Inject(method={"canStandOnFluid"}, at={@At(value="HEAD")}, cancellable=true)
    private void onCanStandOnFluid(FluidState state, CallbackInfoReturnable<Boolean> cir) {
        Player player;
        LivingEntityMixin livingEntityMixin = this;
        if (livingEntityMixin instanceof Player && CobblePassiveHelper.hasLavaStrider(player = (Player)livingEntityMixin) && !player.isCrouching() && state.is(FluidTags.LAVA)) {
            cir.setReturnValue((Object)true);
        }
    }

    @Inject(method={"getDamageAfterArmorAbsorb"}, at={@At(value="RETURN")}, cancellable=true)
    private void onDamageAfterArmorAbsorb(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        if (source != null && source.getEntity() instanceof Player player) {
            CobblePassiveHelper.applyLifeSteal(player);
        }
        if ((Object)this instanceof ServerPlayer serverPlayer) {
            if (source != null && source.is(DamageTypeTags.IS_FIRE)) {
                int fireProt = CobblePassiveHelper.getFireProtectionLevel(serverPlayer);
                if (fireProt > 0) {
                    float reduction = Math.min(0.8f, (float)fireProt * 0.08f);
                    cir.setReturnValue(cir.getReturnValue() * (1.0f - reduction));
                }
            }
        }
    }

    @Inject(method={"handleRelativeFrictionAndCalculateMovement"}, at={@At(value="RETURN")}, cancellable=true)
    private void cobblepassive$boostVineClimbing(Vec3 motion, float friction, CallbackInfoReturnable<Vec3> cir) {
        LivingEntityMixin livingEntityMixin = this;
        if (livingEntityMixin instanceof Player) {
            BlockState blockState;
            ResourceLocation blockId;
            double bonus;
            Player player = (Player)livingEntityMixin;
            Vec3 res = (Vec3)cir.getReturnValue();
            if (res != null && res.y > 0.0 && (bonus = CobblePassiveHelper.getVineClimbBonus(player)) > 0.0 && (blockId = BuiltInRegistries.BLOCK.getKey((Object)(blockState = ((LivingEntity)this).getBlockStateOn()).getBlock())) != null && blockId.getPath().contains("vine")) {
                cir.setReturnValue((Object)new Vec3(res.x, res.y * (1.0 + bonus), res.z));
            }
        }
    }
}

