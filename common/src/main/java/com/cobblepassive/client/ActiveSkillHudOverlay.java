package com.cobblepassive.client;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.activestate.ActivePokemonState;
import com.cobblemon.mod.common.pokemon.activestate.InactivePokemonState;
import com.cobblemon.mod.common.pokemon.activestate.PokemonState;
import com.cobblemon.mod.common.pokemon.activestate.SentOutState;
import com.cobblepassive.CobblePassive;
import com.cobblepassive.CobblePassiveHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Renders the central HUD icon overlay for Pokémon active skills above the hotbar.
 * Appears ONLY when a Pokémon with an active skill is currently sent out in the world.
 */
public final class ActiveSkillHudOverlay {

    public static final ResourceLocation EXCADRILL_READY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CobblePassive.MOD_ID, "textures/gui/skill_excadrill_ready.png");
    public static final ResourceLocation EXCADRILL_COOLDOWN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CobblePassive.MOD_ID, "textures/gui/skill_excadrill_cooldown.png");

    public static final ResourceLocation MEGANIUM_READY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CobblePassive.MOD_ID, "textures/gui/skill_meganium_ready.png");
    public static final ResourceLocation MEGANIUM_COOLDOWN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CobblePassive.MOD_ID, "textures/gui/skill_meganium_cooldown.png");

    public static final ResourceLocation RAYQUAZA_READY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CobblePassive.MOD_ID, "textures/gui/skill_rayquaza_ready.png");
    public static final ResourceLocation RAYQUAZA_COOLDOWN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CobblePassive.MOD_ID, "textures/gui/skill_rayquaza_cooldown.png");

    public static final ResourceLocation RILLABOOM_READY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CobblePassive.MOD_ID, "textures/gui/skill_rillaboom_ready.png");
    public static final ResourceLocation RILLABOOM_COOLDOWN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CobblePassive.MOD_ID, "textures/gui/skill_rillaboom_cooldown.png");

    private static final java.util.Map<ActiveSkillType, Long> SKILL_COOLDOWNS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<ActiveSkillType, Long> TOTAL_COOLDOWNS = new java.util.concurrent.ConcurrentHashMap<>();
    private static long lastRenderNanos = 0L;

    private ActiveSkillHudOverlay() {
    }

    /**
     * Updates the remaining active skill cooldown from authoritative server sync or local prediction.
     */
    public static void setCooldown(long remainingMs, long totalMs) {
        ActiveSkillType skill = getSentOutActiveSkill();
        if (skill == null) {
            return;
        }
        if (remainingMs <= 0L) {
            SKILL_COOLDOWNS.remove(skill);
            return;
        }
        SKILL_COOLDOWNS.put(skill, System.currentTimeMillis() + remainingMs);
        if (totalMs > 0L) {
            TOTAL_COOLDOWNS.put(skill, totalMs);
        }
    }

    /**
     * Called locally when player presses the Active Skill keybind for immediate 0-ping UI responsiveness.
     * Only triggers if a Pokémon with an active skill is currently sent out in the world.
     */
    public static void onLocalSkillTriggered() {
        if (isPlayerInBattle()) {
            return;
        }
        ActiveSkillType skill = getSentOutActiveSkill();
        if (skill == null) {
            return;
        }
        long now = System.currentTimeMillis();
        long cooldownUntil = SKILL_COOLDOWNS.getOrDefault(skill, 0L);
        if (now >= cooldownUntil) {
            long total = TOTAL_COOLDOWNS.getOrDefault(skill, 30_000L);
            SKILL_COOLDOWNS.put(skill, now + total);
        }
    }

    /**
     * Determines whether the given Pokémon is currently sent out in the world.
     */
    public static boolean isPokemonSentOut(Minecraft client, Pokemon pokemon) {
        if (pokemon == null || pokemon.isFainted()) {
            return false;
        }

        // 1. Check Cobblemon State (SentOutState, ActivePokemonState, or non-null icon)
        try {
            PokemonState state = pokemon.getState();
            if (state != null) {
                if (state instanceof InactivePokemonState) {
                    return false;
                }
                if (state instanceof SentOutState || state instanceof ActivePokemonState) {
                    return true;
                }
                String name = state.getName();
                if (name != null && (name.equalsIgnoreCase("sentout") || name.equalsIgnoreCase("active"))) {
                    return true;
                }
                if (state.getIcon(pokemon) != null) {
                    return true;
                }
            }
        } catch (Throwable ignored) {
        }

        // 2. Check direct entity reference
        try {
            if (pokemon.getEntity() != null && !pokemon.getEntity().isRemoved()) {
                return true;
            }
        } catch (Throwable ignored) {
        }

        // 3. Fallback: Scan client level entities for matching UUID
        try {
            if (client != null && client.level != null) {
                for (Entity entity : client.level.entitiesForRendering()) {
                    if (entity instanceof PokemonEntity pe) {
                        if (pokemon.getUuid().equals(pe.getPokemon().getUuid())) {
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return false;
    }

    /**
     * Determines if the local player is currently involved in an active Cobblemon battle.
     */
    public static boolean isPlayerInBattle() {
        try {
            if (com.cobblemon.mod.common.client.CobblemonClient.INSTANCE.getBattle() != null) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    /**
     * Checks if the player currently has any Pokémon sent out in the world that possesses an active skill.
     * Returns the skill definition or null if no active skill Pokémon is currently sent out.
     */
    public static ActiveSkillType getSentOutActiveSkill() {
        try {
            Minecraft client = Minecraft.getInstance();
            if (client == null || client.player == null) {
                return null;
            }

            Iterable<Pokemon> party = CobblePassiveHelper.getClientParty();
            if (party == null) {
                return null;
            }

            for (Pokemon pokemon : party) {
                if (pokemon == null || pokemon.isFainted()) {
                    continue;
                }

                if (!isPokemonSentOut(client, pokemon)) {
                    continue;
                }

                String species = pokemon.getSpecies() != null ? pokemon.getSpecies().getName().toLowerCase() : "";
                if ("excadrill".equals(species)) {
                    return ActiveSkillType.EXCADRILL;
                } else if ("meganium".equals(species)) {
                    return ActiveSkillType.MEGANIUM;
                } else if ("rayquaza".equals(species)) {
                    return ActiveSkillType.RAYQUAZA;
                } else if ("rillaboom".equals(species)) {
                    return ActiveSkillType.RILLABOOM;
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /**
     * Renders the Active Skill UI icon directly above the hotbar in the center of the screen.
     */
    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        long nowNanos = System.nanoTime();
        // Guard against duplicate rendering within the same frame (< 2ms) across multiple hooks
        if (nowNanos - lastRenderNanos < 2_000_000L) {
            return;
        }
        lastRenderNanos = nowNanos;

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.options.hideGui || client.player == null) {
            return;
        }

        if (client.player.isSpectator()) {
            return;
        }

        // Hide overlay when player is in an active Cobblemon battle
        if (isPlayerInBattle()) {
            return;
        }

        // Hide overlay when menus / inventories are opened (allow ChatScreen)
        if (client.screen != null && !(client.screen instanceof ChatScreen)) {
            return;
        }

        // Only display the UI when a Pokémon with an active skill is currently sent out
        ActiveSkillType skill = getSentOutActiveSkill();
        if (skill == null) {
            return;
        }

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        // 64x64 icon (native full resolution, 1x larger than previous 48x48) centered horizontally right above the hotbar
        int iconSize = 64;
        int x = (screenWidth - iconSize) / 2;

        int y;
        if (client.player.isCreative()) {
            y = screenHeight - 90;
        } else if (client.player.experienceLevel > 0) {
            y = screenHeight - 104;
        } else {
            y = screenHeight - 96;
        }

        long now = System.currentTimeMillis();
        long cooldownUntil = SKILL_COOLDOWNS.getOrDefault(skill, 0L);
        boolean onCooldown = now < cooldownUntil;

        ResourceLocation iconTexture = onCooldown ? skill.cooldownTexture : skill.readyTexture;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Blit 64x64 texture (1:1 native scale)
        guiGraphics.blit(iconTexture, x, y, iconSize, iconSize, 0.0F, 0.0F, 64, 64, 64, 64);

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        // If on cooldown, render remaining countdown seconds in the center of the translucent icon with 2.0x font scale
        if (onCooldown) {
            long remainingMs = Math.max(0L, cooldownUntil - now);
            int seconds = (int) Math.ceil(remainingMs / 1000.0D);
            if (seconds > 0) {
                String text = String.valueOf(seconds);
                float scale = 2.0F;
                int textWidth = client.font.width(text);
                float scaledTextWidth = textWidth * scale;
                float scaledTextHeight = client.font.lineHeight * scale;

                float textX = x + (iconSize - scaledTextWidth) / 2.0F;
                float textY = y + (iconSize - scaledTextHeight) / 2.0F + 1.0F;

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(textX, textY, 0.0F);
                guiGraphics.pose().scale(scale, scale, 1.0F);
                guiGraphics.drawString(client.font, text, 0, 0, 0xFFFFFFFF, true);
                guiGraphics.pose().popPose();
            }
        }
    }

    /**
     * Represents registered Pokémon active skills and their associated ready/cooldown textures.
     */
    public enum ActiveSkillType {
        EXCADRILL(EXCADRILL_READY_TEXTURE, EXCADRILL_COOLDOWN_TEXTURE),
        MEGANIUM(MEGANIUM_READY_TEXTURE, MEGANIUM_COOLDOWN_TEXTURE),
        RAYQUAZA(RAYQUAZA_READY_TEXTURE, RAYQUAZA_COOLDOWN_TEXTURE),
        RILLABOOM(RILLABOOM_READY_TEXTURE, RILLABOOM_COOLDOWN_TEXTURE);

        private final ResourceLocation readyTexture;
        private final ResourceLocation cooldownTexture;

        ActiveSkillType(ResourceLocation readyTexture, ResourceLocation cooldownTexture) {
            this.readyTexture = readyTexture;
            this.cooldownTexture = cooldownTexture;
        }

        public ResourceLocation getReadyTexture() {
            return readyTexture;
        }

        public ResourceLocation getCooldownTexture() {
            return cooldownTexture;
        }
    }
}
