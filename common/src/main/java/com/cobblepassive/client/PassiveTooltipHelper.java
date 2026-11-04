package com.cobblepassive.client;

import com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblepassive.PassiveConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class PassiveTooltipHelper {
    public static final ResourceLocation HUD_TEXTURE = ResourceLocation.fromNamespaceAndPath("cobblepassive", "textures/gui/passive_hud.png");
    public static final ResourceLocation ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath("cobblepassive", "textures/gui/icon_passive.png");

    private static final Set<String> SHOULDER_POKEMON = Set.of(
            "pikachu", "eevee", "pidgey", "caterpie", "weedle", "rattata", "spearow",
            "zubat", "paras", "venonat", "diglett", "meowth", "poliwag", "abra",
            "bellsprout", "geodude", "magnemite", "shellder", "krabby", "voltorb",
            "exeggcute", "cubone", "horsea", "staryu", "magikarp", "ditto",
            "omanyte", "kabuto", "dratini", "mew", "combee", "Sableye", "sableye",
            "mimikyu", "gimmighoul", "cutiefly", "ribombee", "joltik", "flabebe"
    );
    public static boolean isEditMode = false;

    public static PassiveInfo getPassiveInfo(Pokemon pokemon) {
        if (pokemon == null) {
            return new PassiveInfo("None", "No Pokemon selected.", false, false);
        }
        String species = pokemon.getSpecies().getName().toLowerCase();
        boolean shoulder = pokemon.getForm().getShoulderMountable() || SHOULDER_POKEMON.contains(species);
        PassiveConfig.PassiveEntry entry = PassiveConfig.getPassive(species);
        if (entry == null) {
            return new PassiveInfo("None", "This Pokémon does not have a registered passive ability.", false, shoulder);
        }

        if (entry.isMewDnaResonance) {
            return new PassiveInfo(
                    "Genesis Resonance",
                    "Harmonizes with all active party members, elevating all of their passive abilities by +1 Tier.",
                    true,
                    shoulder
            );
        }

        if (species.equals("charcadet")) {
            return new PassiveInfo(
                    "Flame Armor",
                    "Reduces incoming fire & lava damage by 24% and extinguishes flames 45% faster.",
                    true,
                    shoulder
            );
        }
        if (species.equals("armarouge") || species.equals("ceruledge")) {
            return new PassiveInfo(
                    "Blazing Armor",
                    "Reduces incoming fire & lava damage by 32% and extinguishes flames 60% faster.",
                    true,
                    shoulder
            );
        }
        if (species.equals("slugma")) {
            return new PassiveInfo(
                    "Magma Sight",
                    "Reduces lava fog by 50% when submerged, extending visibility in lava up to 16 blocks.",
                    true,
                    shoulder
            );
        }
        if (species.equals("magcargo") || species.equals("macargo")) {
            return new PassiveInfo(
                    "Magma Sight",
                    "Completely eliminates lava fog when submerged, granting crystal clear vision while swimming in lava.",
                    true,
                    shoulder
            );
        }
        if (species.equals("sharpedo")) {
            return new PassiveInfo(
                    "Abyssal Sight",
                    "Grants 11 seconds of Night Vision whenever your head or body touches water, while active or while being ridden.",
                    true,
                    shoulder
            );
        }
        if (species.equals("hoothoot") || species.equals("noctowl")) {
            return new PassiveInfo(
                    "Nocturnal Sight",
                    "Highlights all nearby mobs with a glowing outline within 10 blocks during the night.",
                    true,
                    shoulder
            );
        }
        if (species.equals("feebas") || species.equals("milotic")) {
            return new PassiveInfo(
                    "Sacred Waters",
                    "Grants Luck of the Sea III and Lure III while holding or using a fishing rod, making fish bite much faster and upgrading catches.",
                    true,
                    shoulder
            );
        }
        if (species.equals("ferroseed")) {
            return new PassiveInfo(
                    "Iron Barbs",
                    "Reflects 1.5 thorns damage back to direct attackers when you take damage.",
                    true,
                    shoulder
            );
        }
        if (species.equals("ferrothorn")) {
            return new PassiveInfo(
                    "Iron Barbs & Toxin",
                    "Reflects 3.0 thorns damage and inflicts Poison (4s) back to direct attackers when you take damage.",
                    true,
                    shoulder
            );
        }
        if (species.equals("timburr")) {
            return new PassiveInfo(
                    "Builder's Force I",
                    "Increases block/entity reach by +0.5 blocks and mining break speed by +25%.",
                    true,
                    shoulder
            );
        }
        if (species.equals("gurdurr")) {
            return new PassiveInfo(
                    "Builder's Force II",
                    "Increases block/entity reach by +1.0 block and mining break speed by +50%.",
                    true,
                    shoulder
            );
        }
        if (species.equals("conkeldurr")) {
            return new PassiveInfo(
                    "Builder's Force III",
                    "Increases block/entity reach by +1.5 blocks and mining break speed by +75%.",
                    true,
                    shoulder
            );
        }
        if (species.equals("drilbur")) {
            return new PassiveInfo(
                    "Earth Miner I",
                    "Increases soil & sand mining speed by +40% and stone mining speed by +25%.",
                    true,
                    shoulder
            );
        }
        if (species.equals("excadrill")) {
            return new PassiveInfo(
                    "Earth Miner II & Drill",
                    "Passive: +80% soil & +50% stone mining speed. Active Skill [Keybind]: Teleports Excadrill to target block (up to 25m), spins its drill, and instantly shatters 15 blocks straight down (30s CD).",
                    true,
                    shoulder
            );
        }
        if (species.equals("meganium")) {
            return new PassiveInfo(
                    "Verdant Aroma",
                    "Active Skill [Keybind]: Meganium releases a revitalizing floral aroma that instantly applies Bone Meal to all crops in a 9x9 area around you (30s CD).",
                    true,
                    shoulder
            );
        }
        if (species.equals("rayquaza")) {
            return new PassiveInfo(
                    "Air Lock",
                    "Active Skill [Keybind]: Rayquaza commands the atmospheric winds to instantly clear all rain, storms, and thunderstorms across the sky (60s CD).",
                    true,
                    shoulder
            );
        }
        if (species.equals("rillaboom")) {
            return new PassiveInfo(
                    "Grass Surge",
                    "Active Skill [Keybind]: Rillaboom triggers Grass Surge, generating concentric Grassy Terrain rings for 10s. Grounded players heal 1 HP/s and sent-out Pokémon recover 1/16 max HP/s (30s CD).",
                    true,
                    shoulder
            );
        }
        if (species.equals("charmander")) {
            return new PassiveInfo(
                    "Blazing Tail I",
                    "Ignites targets for 3 seconds upon melee attacks.",
                    true,
                    shoulder
            );
        }
        if (species.equals("charmeleon")) {
            return new PassiveInfo(
                    "Blazing Tail II",
                    "Ignites targets for 5 seconds upon melee attacks.",
                    true,
                    shoulder
            );
        }
        if (species.equals("charizard")) {
            return new PassiveInfo(
                    "Dragon Flare",
                    "Ignites targets for 7 seconds upon melee attacks.",
                    true,
                    shoulder
            );
        }
        if (species.equals("tyrogue")) {
            return new PassiveInfo(
                    "Brawler's Strike",
                    "Increases bare-handed attack damage by +2.0 (1 full heart) while sent out.",
                    true,
                    shoulder
            );
        }
        if (species.equals("hitmonchan") || species.equals("hitmonlee") || species.equals("hitmontop")) {
            String name = species.equals("hitmonchan") ? "Iron Fist" : (species.equals("hitmonlee") ? "High Kick" : "Triple Kick");
            return new PassiveInfo(
                    name,
                    "Massively increases bare-handed attack damage by +4.0 (2 full hearts) while sent out.",
                    true,
                    shoulder
            );
        }
        if (species.equals("celebi") || species.equals("chelebi")) {
            return new PassiveInfo(
                    "Voice of the Forest",
                    "Detects crops & saplings in an 8-block area around player. If >2 crops are present, accelerates growth of all crops simultaneously.",
                    true,
                    shoulder
            );
        }
        if (species.equals("snivy") || species.equals("servine") || species.equals("serperior")) {
            return new PassiveInfo(
                    "Forest Sprinter",
                    "Increases movement speed (Speed I) while exploring Forest biomes.",
                    true,
                    shoulder
            );
        }
        if (species.equals("crobat")) {
            return new PassiveInfo(
                    "Leech Fang",
                    "Heals the player for 2 HP (1 full heart) upon each successful attack against an entity. (Boosted to 3 HP with Mew DNA Resonance).",
                    true,
                    shoulder
            );
        }
        if (species.equals("zubat") || species.equals("golbat")) {
            return new PassiveInfo(
                    "Leech Fang",
                    "Heals the player for 1 HP (0.5 heart) upon each successful attack against an entity. (Boosted to 2 HP with Mew DNA Resonance).",
                    true,
                    shoulder
            );
        }
        if (species.equals("tangela")) {
            return new PassiveInfo(
                    "Vine Weaver I",
                    "Increases climbing speed on vines by +25% while active.",
                    true,
                    shoulder
            );
        }
        if (species.equals("tangrowth")) {
            return new PassiveInfo(
                    "Vine Weaver II",
                    "Massively increases climbing speed on vines by +50% while active.",
                    true,
                    shoulder
            );
        }

        if (entry.oreRadarRadius > 0) {
            return new PassiveInfo(
                    "Gemstone Radar",
                    "Scans for valuable ores (Diamond, Emerald, Gold, Ancient Debris, etc.) within a " + entry.oreRadarRadius
                            + "-block radius every " + entry.radarIntervalSeconds
                            + " seconds. Ores are highlighted with a glowing outline matching their type.",
                    true,
                    shoulder
            );
        }
        if (entry.hungerFeedIntervalSeconds > 0) {
            return new PassiveInfo(
                    "Sweet Honey Gatherer",
                    "Gradually satisfies hunger (+2 Hunger points) every " + entry.hungerFeedIntervalSeconds + " seconds while active.",
                    true,
                    shoulder
            );
        }
        if (entry.webImmunity) {
            return new PassiveInfo(
                    "Cobweb Immunity",
                    "Grants immunity to slowing effects from cobwebs, allowing full movement through webs.",
                    true,
                    shoulder
            );
        }
        if (entry.thornsDamage > 0.0f) {
            return new PassiveInfo(
                    "Spiky Thorns",
                    "Damages attackers when you are hit (deals " + entry.thornsDamage + " damage).",
                    true,
                    shoulder
            );
        }
        if (entry.fireAspectSeconds > 0) {
            return new PassiveInfo(
                    "Fire Aspect",
                    "Ignites targets upon melee hit for " + entry.fireAspectSeconds + " seconds.",
                    true,
                    shoulder
            );
        }
        if (entry.fortuneLevel > 0) {
            return new PassiveInfo(
                    "Fortune Boost",
                    "Increases drop rates and quantities from block breaking (Fortune " + entry.fortuneLevel + ").",
                    true,
                    shoulder
            );
        }
        if (entry.respirationLevel > 0) {
            String desc = "Extends underwater breathing time (Respiration " + entry.respirationLevel + ")";
            if (entry.bonusAirSupply > 0) {
                desc += " and expands maximum lung capacity (+" + entry.bonusAirSupply + " air).";
            } else {
                desc += ".";
            }
            return new PassiveInfo(
                    "Aquatic Respiration",
                    desc,
                    true,
                    shoulder
            );
        }
        if (entry.sneakSpeedBonus > 0.0f) {
            int pct = Math.round(entry.sneakSpeedBonus * 100.0f);
            String title = species.equals("arbok") ? "Silent Stalker II" : (species.equals("ekans") ? "Silent Stalker I" : "Phantom Step");
            return new PassiveInfo(
                    title,
                    "Increases movement speed while sneaking by +" + pct + "%.",
                    true,
                    shoulder
            );
        }
        if (entry.fastDetox) {
            String name = species.equals("snorlax") ? "Thick Fat Immunity" : "Natural Detox";
            String desc = species.equals("snorlax")
                    ? "Rapidly purges Poison & Wither and grants Resistance I."
                    : "Rapidly neutralizes Poison and Wither effects to 3 seconds.";
            return new PassiveInfo(name, desc, true, shoulder);
        }
        if (entry.smiteBonusDamage > 0.0f) {
            String name = entry.smiteBonusDamage >= 6.0f ? "Thunderbolt Smite" : "Static Shock";
            return new PassiveInfo(
                    name,
                    "Deals +" + entry.smiteBonusDamage + " bonus damage against undead monsters (Zombies, Skeletons, etc.).",
                    true,
                    shoulder
            );
        }
        if (entry.boneCrusherDamage > 0.0f) {
            String name = entry.boneCrusherDamage >= 7.0f ? "Bone Club Mastery" : "Skull Basher";
            return new PassiveInfo(
                    name,
                    "Deals +" + entry.boneCrusherDamage + " bonus damage against skeletal monsters (Skeletons, Strays, etc.).",
                    true,
                    shoulder
            );
        }
        if (entry.reachBonus > 0.0) {
            String name = entry.reachBonus >= 2.0 ? "Telekinetic Mastery" : (entry.reachBonus >= 1.5 ? "Psychic Grasp" : "Mind Reach");
            return new PassiveInfo(
                    name,
                    "Extends block interaction and attack reach by +" + entry.reachBonus + " blocks.",
                    true,
                    shoulder
            );
        }
        if (entry.handBonusDamage > 0.0f) {
            return new PassiveInfo(
                    "Iron Fist",
                    "Increases bare-handed melee damage and grants Haste while mining.",
                    true,
                    shoulder
            );
        }
        return new PassiveInfo("Passive Ability", "Provides a passive bonus while active.", true, shoulder);
    }

    public static void renderPassiveSummaryIcon(
            GuiGraphics context,
            Font textRenderer,
            ModelWidget modelWidget,
            Pokemon pokemon,
            int mouseX,
            int mouseY,
            int screenWidth,
            int screenHeight
    ) {
        if (modelWidget == null || pokemon == null) return;
        PassiveInfo info = getPassiveInfo(pokemon);
        if (!info.hasPassive() && !isEditMode) return;

        PassiveConfig.UiSettings ui = PassiveConfig.ui;
        int iconSize = ui.iconSize;
        int iconX = modelWidget.getX() + ui.iconOffsetX;
        int iconY = modelWidget.getY() + ui.iconOffsetY;
        boolean hovered = mouseX >= iconX && mouseX <= iconX + iconSize && mouseY >= iconY && mouseY <= iconY + iconSize;

        RenderSystem.enableDepthTest();
        context.pose().pushPose();
        context.pose().translate(0.0f, 0.0f, (float) ui.hudLayer);
        RenderSystem.enableBlend();
        if (hovered || isEditMode) {
            RenderSystem.setShaderColor(1.2f, 1.2f, 0.9f, 1.0f);
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.95f);
        }
        context.blit(ICON_TEXTURE, iconX, iconY, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        context.pose().popPose();
        RenderSystem.disableDepthTest();

        if (hovered || isEditMode) {
            int hudX = iconX + ui.hudOffsetX;
            int hudY = iconY + ui.hudOffsetY;
            if (hudX + ui.hudWidth > screenWidth - 4) {
                hudX = iconX - ui.hudWidth - 4;
            }
            if (hudY + ui.hudMinHeight > screenHeight - 4) {
                hudY = screenHeight - ui.hudMinHeight - 4;
            }
            if (hudY < 4) {
                hudY = 4;
            }
            renderCustomHudBox(
                    context,
                    textRenderer,
                    info.name(),
                    info.description(),
                    hudX,
                    hudY,
                    ui.hudWidth,
                    ui.hudMinHeight,
                    ui.hudBgColor,
                    ui.hudLayer,
                    ui.fontScale,
                    info.hasPassive(),
                    info.shoulderMountable()
            );
        }
    }

    public static void renderCustomHudBox(
            GuiGraphics context,
            Font textRenderer,
            String name,
            String description,
            int x,
            int y,
            int width,
            int minHeight,
            int bgColor,
            int zLayer,
            float fontScale,
            boolean hasPassive,
            boolean shoulderMountable
    ) {
        context.pose().pushPose();
        context.pose().translate(0.0f, 0.0f, (float) (zLayer + 10));

        int innerWidth = width - 12;
        int textWidthForWrap = Math.max(40, (int) ((float) innerWidth / fontScale));
        List<FormattedCharSequence> descLines = textRenderer.split(Component.literal(description), textWidthForWrap);

        int titleHeight = 12;
        int statusHeight = 11;
        int descLineHeight = (int) (10.0f * fontScale);
        int calculatedHeight = 8 + titleHeight + statusHeight + 4 + descLines.size() * descLineHeight + 6;
        int height = Math.max(minHeight, calculatedHeight);

        // Background
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, bgColor);
        context.fill(x + 1, y, x + width - 1, y + 1, -12566464);
        context.fill(x + 1, y + height - 1, x + width - 1, y + height, -12566464);
        context.fill(x, y + 1, x + 1, y + height - 1, -12566464);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, -12566464);

        int currentY = y + 6;
        context.drawString(textRenderer, "✦ " + name, x + 6, currentY, 0xFFE066, false);
        currentY += titleHeight;

        String conditionText;
        int condColor;
        if (!hasPassive) {
            conditionText = "○ Inactive";
            condColor = 0x888888;
        } else if (PassiveConfig.requireSentOut) {
            conditionText = shoulderMountable ? "● Active: Sent Out / Shoulder" : "● Active: Sent Out";
            condColor = 0x55FF55;
        } else {
            conditionText = "● Active: In Party";
            condColor = 0x55FF55;
        }
        context.drawString(textRenderer, conditionText, x + 6, currentY, condColor, false);
        currentY += statusHeight + 2;

        context.pose().pushPose();
        context.pose().translate((float) (x + 6), (float) currentY, 0.0f);
        context.pose().scale(fontScale, fontScale, 1.0f);
        int localY = 0;
        for (FormattedCharSequence line : descLines) {
            context.drawString(textRenderer, line, 0, localY, 0xCCCCCC, false);
            localY += 10;
        }
        context.pose().popPose();

        context.pose().popPose();
    }

    public static boolean handleKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 297) { // F8 toggle
            isEditMode = !isEditMode;
            return true;
        }
        return false;
    }

    public record PassiveInfo(
            String name,
            String description,
            boolean hasPassive,
            boolean shoulderMountable
    ) {}
}
