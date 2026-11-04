package com.cobblepassive.network;

import com.cobblepassive.CobblePassive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server-to-Client packet payload synchronizing the remaining active skill cooldown.
 */
public record ActiveSkillCooldownPayload(long cooldownRemainingMs, long totalCooldownMs) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ActiveSkillCooldownPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CobblePassive.MOD_ID, "active_skill_cooldown"));

    public static final StreamCodec<FriendlyByteBuf, ActiveSkillCooldownPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            ActiveSkillCooldownPayload::cooldownRemainingMs,
            ByteBufCodecs.VAR_LONG,
            ActiveSkillCooldownPayload::totalCooldownMs,
            ActiveSkillCooldownPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
