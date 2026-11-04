package com.cobblepassive.network;

import com.cobblepassive.CobblePassive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-to-Server packet payload triggered when the player presses the unified Active Skill keybind.
 */
public record ActiveSkillPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ActiveSkillPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CobblePassive.MOD_ID, "active_skill"));

    public static final StreamCodec<FriendlyByteBuf, ActiveSkillPayload> STREAM_CODEC =
            StreamCodec.unit(new ActiveSkillPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
