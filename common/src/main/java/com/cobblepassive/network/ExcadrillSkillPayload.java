package com.cobblepassive.network;

import com.cobblepassive.CobblePassive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-to-Server packet payload triggered when the player presses the Excadrill Active Skill keybind.
 */
public record ExcadrillSkillPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ExcadrillSkillPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CobblePassive.MOD_ID, "excadrill_skill"));

    public static final StreamCodec<FriendlyByteBuf, ExcadrillSkillPayload> STREAM_CODEC =
            StreamCodec.unit(new ExcadrillSkillPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
