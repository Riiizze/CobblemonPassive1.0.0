/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.cobblepassive;

import com.cobblepassive.CobblePassive;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PassiveConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Paths.get("config", new String[0]);
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("cobblepassive.json");
    private static final Map<String, PassiveEntry> passives = new HashMap<String, PassiveEntry>();
    public static boolean requireNotFainted = true;
    public static boolean requireSentOut = true;
    public static boolean allowShoulderState = true;
    public static boolean enableShoulderMount = true;
    public static boolean showStatusEffectIcons = true;
    public static final UiSettings ui = new UiSettings();

    public static boolean requireNotFainted() {
        return requireNotFainted;
    }

    public static boolean requireSentOut() {
        return requireSentOut;
    }

    public static boolean allowShoulderState() {
        return allowShoulderState || enableShoulderMount;
    }

    public static PassiveEntry getPassive(String species) {
        if (species == null) {
            return null;
        }
        String key = species.toLowerCase().trim();
        PassiveEntry entry = passives.get(key);
        if (entry != null) {
            return entry;
        }
        String norm = key.replace("-", "").replace("_", "");
        for (Map.Entry<String, PassiveEntry> e : passives.entrySet()) {
            if (!e.getKey().toLowerCase().replace("-", "").replace("_", "").equals(norm)) continue;
            return e.getValue();
        }
        return null;
    }

    public static Map<String, PassiveEntry> getAllPassives() {
        return Collections.unmodifiableMap(passives);
    }

    public static int size() {
        return passives.size();
    }

    public static void load() {
        passives.clear();
        try {
            JsonObject root;
            if (!Files.exists(CONFIG_FILE, new LinkOption[0])) {
                PassiveConfig.copyDefaultFromResources();
            }
            if (!Files.exists(CONFIG_FILE, new LinkOption[0])) {
                return;
            }
            try (BufferedReader reader = Files.newBufferedReader(CONFIG_FILE, StandardCharsets.UTF_8);){
                root = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            }
            try (InputStream defIn = PassiveConfig.class.getResourceAsStream("/default_pokepassive.json");){
                if (defIn != null) {
                    String defJsonStr = new String(defIn.readAllBytes(), StandardCharsets.UTF_8);
                    JsonObject defRoot = JsonParser.parseString((String)defJsonStr).getAsJsonObject();
                    JsonObject defPokemon = null;
                    if (defRoot.has("passives") && defRoot.get("passives").isJsonObject()) {
                        defPokemon = defRoot.getAsJsonObject("passives");
                    } else if (defRoot.has("pokemon") && defRoot.get("pokemon").isJsonObject()) {
                        defPokemon = defRoot.getAsJsonObject("pokemon");
                    }
                    if (defPokemon != null) {
                        String[] syncKeys;
                        String[] userCro;
                        JsonObject userGol;
                        JsonObject userZub;
                        JsonObject userCelebi;
                        JsonObject userMag;
                        JsonObject userSlug;
                        JsonElement el;
                        String targetKey;
                        String string = root.has("passives") ? "passives" : (targetKey = root.has("pokemon") ? "pokemon" : "passives");
                        if (!root.has(targetKey) || !root.get(targetKey).isJsonObject()) {
                            root.add(targetKey, (JsonElement)new JsonObject());
                        }
                        JsonObject userPokemon = root.getAsJsonObject(targetKey);
                        boolean updated = false;
                        for (Map.Entry defEntry : defPokemon.entrySet()) {
                            if (userPokemon.has((String)defEntry.getKey())) continue;
                            userPokemon.add((String)defEntry.getKey(), (JsonElement)defEntry.getValue());
                            updated = true;
                        }
                        if (userPokemon.has("slugma") && defPokemon.has("slugma") && (el = userPokemon.get("slugma")).isJsonObject() && ((userSlug = el.getAsJsonObject()).has("lavaStriderSeconds") || !userSlug.has("lavaFogClearPercent"))) {
                            userPokemon.add("slugma", defPokemon.get("slugma"));
                            updated = true;
                        }
                        if (userPokemon.has("magcargo") && defPokemon.has("magcargo") && (el = userPokemon.get("magcargo")).isJsonObject() && ((userMag = el.getAsJsonObject()).has("lavaStriderSeconds") || !userMag.has("lavaFogClearPercent"))) {
                            userPokemon.add("magcargo", defPokemon.get("magcargo"));
                            updated = true;
                        }
                        if (userPokemon.has("celebi") && defPokemon.has("celebi") && (el = userPokemon.get("celebi")).isJsonObject() && (userCelebi = el.getAsJsonObject()).has("cropGrowthChance") && userCelebi.get("cropGrowthChance").getAsDouble() < 0.49) {
                            userPokemon.add("celebi", defPokemon.get("celebi"));
                            updated = true;
                        }
                        if (userPokemon.has("chelebi") && defPokemon.has("chelebi") && (el = userPokemon.get("chelebi")).isJsonObject() && (userCelebi = el.getAsJsonObject()).has("cropGrowthChance") && userCelebi.get("cropGrowthChance").getAsDouble() < 0.49) {
                            userPokemon.add("chelebi", defPokemon.get("chelebi"));
                            updated = true;
                        }
                        if (userPokemon.has("zubat") && defPokemon.has("zubat") && (el = userPokemon.get("zubat")).isJsonObject() && ((userZub = el.getAsJsonObject()).has("effects") || !userZub.has("lifeSteal") || userZub.get("lifeSteal").getAsFloat() != 1.0f)) {
                            userPokemon.add("zubat", defPokemon.get("zubat"));
                            updated = true;
                        }
                        if (userPokemon.has("golbat") && defPokemon.has("golbat") && (el = userPokemon.get("golbat")).isJsonObject() && ((userGol = el.getAsJsonObject()).has("effects") || !userGol.has("lifeSteal") || userGol.get("lifeSteal").getAsFloat() != 1.0f)) {
                            userPokemon.add("golbat", defPokemon.get("golbat"));
                            updated = true;
                        }
                        if (userPokemon.has("crobat") && defPokemon.has("crobat") && (el = userPokemon.get("crobat")).isJsonObject() && ((userCro = el.getAsJsonObject()).has("effects") || !userCro.has("lifeSteal") || userCro.get("lifeSteal").getAsFloat() != 1.0f)) {
                            userPokemon.add("crobat", defPokemon.get("crobat"));
                            updated = true;
                        }
                        if (userPokemon.has("servine") && defPokemon.has("servine")) {
                            userPokemon.add("servine", defPokemon.get("servine"));
                            updated = true;
                        }
                        if (userPokemon.has("serperior") && defPokemon.has("serperior")) {
                            userPokemon.add("serperior", defPokemon.get("serperior"));
                            updated = true;
                        }
                        for (String key : syncKeys = new String[]{"feebas", "milotic", "ferroseed", "ferrothorn", "hoothoot", "noctowl", "timburr", "gurdurr", "conkeldurr", "tyrogue", "hitmonchan", "hitmonlee", "hitmontop", "charmander", "charmeleon", "charizard", "sharpedo"}) {
                            if (!defPokemon.has(key)) continue;
                            userPokemon.add(key, defPokemon.get(key));
                            updated = true;
                        }
                        if (userPokemon.has("sharkpedo")) {
                            if (!userPokemon.has("sharpedo")) {
                                userPokemon.add("sharpedo", userPokemon.get("sharkpedo"));
                            }
                            userPokemon.remove("sharkpedo");
                            updated = true;
                        }
                        if (userPokemon.has("chi_yu")) {
                            userPokemon.remove("chi_yu");
                            updated = true;
                        }
                        if (userPokemon.has("chiyu")) {
                            userPokemon.remove("chiyu");
                            updated = true;
                        }
                        if (userPokemon.has("dragonite")) {
                            userPokemon.remove("dragonite");
                            updated = true;
                        }
                        if (userPokemon.has("drakloak")) {
                            userPokemon.remove("drakloak");
                            updated = true;
                        }
                        if (updated) {
                            Files.writeString(CONFIG_FILE, (CharSequence)GSON.toJson((JsonElement)root), StandardCharsets.UTF_8, new OpenOption[0]);
                            CobblePassive.LOGGER.info("[CobblePassive] Merged new default Pokemon passives into cobblepassive.json");
                        }
                    }
                }
            }
            catch (Exception mergeEx) {
                CobblePassive.LOGGER.warn("[CobblePassive] Could not merge defaults: {}", (Object)mergeEx.getMessage());
            }
            if (root.has("requireNotFainted")) {
                requireNotFainted = root.get("requireNotFainted").getAsBoolean();
            }
            if (root.has("requireSentOut")) {
                requireSentOut = root.get("requireSentOut").getAsBoolean();
            }
            if (root.has("allowShoulderState")) {
                enableShoulderMount = allowShoulderState = root.get("allowShoulderState").getAsBoolean();
            } else if (root.has("enableShoulderMount")) {
                allowShoulderState = enableShoulderMount = root.get("enableShoulderMount").getAsBoolean();
            }
            if (root.has("showStatusEffectIcons")) {
                showStatusEffectIcons = root.get("showStatusEffectIcons").getAsBoolean();
            }
            if (root.has("ui") && root.get("ui").isJsonObject()) {
                JsonObject u = root.getAsJsonObject("ui");
                if (u.has("iconOffsetX")) {
                    PassiveConfig.ui.iconOffsetX = u.get("iconOffsetX").getAsInt();
                }
                if (u.has("iconOffsetY")) {
                    PassiveConfig.ui.iconOffsetY = u.get("iconOffsetY").getAsInt();
                }
                if (u.has("iconSize")) {
                    PassiveConfig.ui.iconSize = Math.max(4, u.get("iconSize").getAsInt());
                }
                if (u.has("hudOffsetX")) {
                    PassiveConfig.ui.hudOffsetX = u.get("hudOffsetX").getAsInt();
                }
                if (u.has("hudOffsetY")) {
                    PassiveConfig.ui.hudOffsetY = u.get("hudOffsetY").getAsInt();
                }
                if (u.has("hudWidth")) {
                    PassiveConfig.ui.hudWidth = Math.max(80, u.get("hudWidth").getAsInt());
                }
                if (u.has("hudMinHeight")) {
                    PassiveConfig.ui.hudMinHeight = Math.max(40, u.get("hudMinHeight").getAsInt());
                }
                if (u.has("hudBgColor")) {
                    PassiveConfig.ui.hudBgColor = u.get("hudBgColor").getAsInt();
                }
                if (u.has("hudLayer")) {
                    PassiveConfig.ui.hudLayer = u.get("hudLayer").getAsInt();
                }
                if (u.has("fontScale")) {
                    PassiveConfig.ui.fontScale = Math.max(0.5f, Math.min(2.0f, u.get("fontScale").getAsFloat()));
                }
            }
            JsonObject p = null;
            if (root.has("passives") && root.get("passives").isJsonObject()) {
                p = root.getAsJsonObject("passives");
            } else if (root.has("pokemon") && root.get("pokemon").isJsonObject()) {
                p = root.getAsJsonObject("pokemon");
            }
            if (p != null) {
                for (Map.Entry entry : p.entrySet()) {
                    if (!((JsonElement)entry.getValue()).isJsonObject()) continue;
                    String species = ((String)entry.getKey()).toLowerCase().trim();
                    if (species.equals("sharkpedo")) {
                        species = "sharpedo";
                    }
                    if (species.equals("chi_yu") || species.equals("chiyu") || species.equals("dragonite") || species.equals("drakloak")) continue;
                    JsonObject data = ((JsonElement)entry.getValue()).getAsJsonObject();
                    PassiveEntry passiveEntry = new PassiveEntry();
                    if (data.has("effects") && data.get("effects").isJsonArray()) {
                        for (JsonElement effEl : data.getAsJsonArray("effects")) {
                            Optional holder;
                            String effectId;
                            ResourceLocation rl;
                            JsonObject effObj;
                            if (!effEl.isJsonObject() || !(effObj = effEl.getAsJsonObject()).has("effect") || (rl = ResourceLocation.tryParse((String)(effectId = effObj.get("effect").getAsString()))) == null || (holder = BuiltInRegistries.MOB_EFFECT.getHolder(rl)).isEmpty()) continue;
                            int amplifier = effObj.has("amplifier") ? effObj.get("amplifier").getAsInt() : 0;
                            boolean nightOnly = effObj.has("nightOnly") && effObj.get("nightOnly").getAsBoolean();
                            String biomeRequired = effObj.has("biomeRequired") ? effObj.get("biomeRequired").getAsString().toLowerCase() : null;
                            String itemInHand = effObj.has("itemInHand") ? effObj.get("itemInHand").getAsString() : null;
                            Integer maxY = effObj.has("maxY") ? Integer.valueOf(effObj.get("maxY").getAsInt()) : null;
                            passiveEntry.effects.add(new PotionEffect((Holder<MobEffect>)((Holder)holder.get()), amplifier, nightOnly, biomeRequired, itemInHand, maxY));
                        }
                    }
                    if (data.has("lightLevel")) {
                        passiveEntry.lightLevel = Math.max(0, Math.min(15, data.get("lightLevel").getAsInt()));
                    }
                    if (data.has("thornsDamage")) {
                        passiveEntry.thornsDamage = data.get("thornsDamage").getAsFloat();
                    }
                    if (data.has("fireAspectSeconds")) {
                        passiveEntry.fireAspectSeconds = Math.max(0, data.get("fireAspectSeconds").getAsInt());
                    }
                    if (data.has("fortuneLevel")) {
                        passiveEntry.fortuneLevel = Math.max(0, data.get("fortuneLevel").getAsInt());
                    }
                    if (data.has("oreRadarRadius")) {
                        passiveEntry.oreRadarRadius = Math.max(0, data.get("oreRadarRadius").getAsInt());
                    }
                    if (data.has("radarIntervalSeconds")) {
                        passiveEntry.radarIntervalSeconds = Math.max(1, data.get("radarIntervalSeconds").getAsInt());
                    }
                    if (data.has("pingDurationSeconds")) {
                        passiveEntry.pingDurationSeconds = Math.max(1, data.get("pingDurationSeconds").getAsInt());
                    }
                    if (data.has("targetOres") && data.get("targetOres").isJsonArray()) {
                        passiveEntry.targetOres = new ArrayList<String>();
                        for (JsonElement el : data.getAsJsonArray("targetOres")) {
                            passiveEntry.targetOres.add(el.getAsString());
                        }
                    }
                    if (data.has("sneakSpeedBonus")) {
                        passiveEntry.sneakSpeedBonus = data.get("sneakSpeedBonus").getAsFloat();
                    }
                    if (data.has("fastDetox")) {
                        passiveEntry.fastDetox = data.get("fastDetox").getAsBoolean();
                    }
                    if (data.has("respirationLevel")) {
                        passiveEntry.respirationLevel = Math.max(0, data.get("respirationLevel").getAsInt());
                    }
                    if (data.has("bonusAirSupply")) {
                        passiveEntry.bonusAirSupply = data.get("bonusAirSupply").getAsInt();
                    }
                    if (data.has("fireProtectionLevel")) {
                        passiveEntry.fireProtectionLevel = Math.max(0, data.get("fireProtectionLevel").getAsInt());
                    }
                    if (data.has("lavaStriderSeconds")) {
                        passiveEntry.lavaStriderSeconds = Math.max(0, data.get("lavaStriderSeconds").getAsInt());
                    }
                    if (data.has("lavaStriderBlocksPerSec")) {
                        passiveEntry.lavaStriderBlocksPerSec = Math.max(0.1, data.get("lavaStriderBlocksPerSec").getAsDouble());
                    }
                    if (data.has("clearLavaVision")) {
                        passiveEntry.clearLavaVision = data.get("clearLavaVision").getAsBoolean();
                    }
                    if (data.has("lavaFogClearPercent")) {
                        passiveEntry.lavaFogClearPercent = Math.max(0.0, Math.min(1.0, data.get("lavaFogClearPercent").getAsDouble()));
                        if (passiveEntry.lavaFogClearPercent > 0.0) {
                            passiveEntry.clearLavaVision = true;
                        }
                    }
                    if (data.has("waterNightVision")) {
                        passiveEntry.waterNightVision = data.get("waterNightVision").getAsBoolean();
                    }
                    if (data.has("cropGrowthRadius")) {
                        passiveEntry.cropGrowthRadius = Math.max(0, data.get("cropGrowthRadius").getAsInt());
                    }
                    if (data.has("cropGrowthChance")) {
                        passiveEntry.cropGrowthChance = Math.max(0.0, data.get("cropGrowthChance").getAsDouble());
                    }
                    if (data.has("cropGrowthIntervalSeconds")) {
                        passiveEntry.cropGrowthIntervalSeconds = Math.max(0.1, data.get("cropGrowthIntervalSeconds").getAsDouble());
                    }
                    if (data.has("cropGrowthMinCrops")) {
                        passiveEntry.cropGrowthMinCrops = Math.max(0, data.get("cropGrowthMinCrops").getAsInt());
                    }
                    if (data.has("maxOresDetected")) {
                        passiveEntry.maxOresDetected = Math.max(1, data.get("maxOresDetected").getAsInt());
                    }
                    if (data.has("lifeStealMewBonus")) {
                        passiveEntry.lifeStealMewBonus = data.get("lifeStealMewBonus").getAsFloat();
                    }
                    if (data.has("mobGlowingIntervalSeconds")) {
                        passiveEntry.mobGlowingIntervalSeconds = Math.max(1, data.get("mobGlowingIntervalSeconds").getAsInt());
                    }
                    if (data.has("fastDetoxMaxDurationSeconds")) {
                        passiveEntry.fastDetoxMaxDurationSeconds = Math.max(1, data.get("fastDetoxMaxDurationSeconds").getAsInt());
                    }
                    if (data.has("lavaVisionDistance")) {
                        passiveEntry.lavaVisionDistance = data.get("lavaVisionDistance").getAsFloat();
                    }
                    if (data.has("mewDnaTierBonus")) {
                        passiveEntry.mewDnaTierBonus = Math.max(1, data.get("mewDnaTierBonus").getAsInt());
                    }
                    if (data.has("lifeSteal")) {
                        passiveEntry.lifeSteal = Math.max(0.0f, data.get("lifeSteal").getAsFloat());
                    }
                    if (data.has("vineClimbSpeedBonus")) {
                        passiveEntry.vineClimbSpeedBonus = Math.max(0.0, data.get("vineClimbSpeedBonus").getAsDouble());
                    }
                    if (data.has("smiteBonusDamage")) {
                        passiveEntry.smiteBonusDamage = data.get("smiteBonusDamage").getAsFloat();
                    }
                    if (data.has("boneCrusherDamage")) {
                        passiveEntry.boneCrusherDamage = data.get("boneCrusherDamage").getAsFloat();
                    }
                    if (data.has("reachBonus")) {
                        passiveEntry.reachBonus = Math.max(0.0, data.get("reachBonus").getAsDouble());
                    }
                    if (data.has("breakSpeedBonus")) {
                        passiveEntry.breakSpeedBonus = Math.max(0.0, data.get("breakSpeedBonus").getAsDouble());
                    }
                    if (data.has("fishingLuckLevel")) {
                        passiveEntry.fishingLuckLevel = Math.max(0, data.get("fishingLuckLevel").getAsInt());
                    }
                    if (data.has("fishingLureLevel")) {
                        passiveEntry.fishingLureLevel = Math.max(0, data.get("fishingLureLevel").getAsInt());
                    }
                    if (data.has("inflictPoisonSeconds")) {
                        passiveEntry.inflictPoisonSeconds = Math.max(0, data.get("inflictPoisonSeconds").getAsInt());
                    }
                    if (data.has("isMewDnaResonance")) {
                        passiveEntry.isMewDnaResonance = data.get("isMewDnaResonance").getAsBoolean();
                    }
                    if (data.has("handBonusDamage")) {
                        passiveEntry.handBonusDamage = data.get("handBonusDamage").getAsFloat();
                    }
                    if (data.has("webImmunity")) {
                        passiveEntry.webImmunity = data.get("webImmunity").getAsBoolean();
                    }
                    if (data.has("hungerFeedIntervalSeconds")) {
                        passiveEntry.hungerFeedIntervalSeconds = Math.max(0, data.get("hungerFeedIntervalSeconds").getAsInt());
                    }
                    if (data.has("hungerFeedAmount")) {
                        passiveEntry.hungerFeedAmount = Math.max(1, data.get("hungerFeedAmount").getAsInt());
                    }
                    if (data.has("mobGlowingRadius")) {
                        passiveEntry.mobGlowingRadius = Math.max(0, data.get("mobGlowingRadius").getAsInt());
                    }
                    if (data.has("mobGlowingNightOnly")) {
                        passiveEntry.mobGlowingNightOnly = data.get("mobGlowingNightOnly").getAsBoolean();
                    }
                    passives.put(species, passiveEntry);
                }
            }
            CobblePassive.LOGGER.info("[CobblePassive] Loaded {} Pokemon passives.", (Object)passives.size());
        }
        catch (Exception e) {
            CobblePassive.LOGGER.error("[CobblePassive] Error loading config: {}", (Object)e.getMessage(), (Object)e);
        }
    }

    public static void saveUiSettings(UiSettings newSettings) {
        if (newSettings != null && newSettings != ui) {
            ui.iconOffsetX = newSettings.iconOffsetX;
            ui.iconOffsetY = newSettings.iconOffsetY;
            ui.iconSize = newSettings.iconSize;
            ui.hudOffsetX = newSettings.hudOffsetX;
            ui.hudOffsetY = newSettings.hudOffsetY;
            ui.hudWidth = newSettings.hudWidth;
            ui.hudMinHeight = newSettings.hudMinHeight;
            ui.hudBgColor = newSettings.hudBgColor;
            ui.hudLayer = newSettings.hudLayer;
            ui.fontScale = newSettings.fontScale;
        }
        try {
            JsonObject root;
            if (Files.exists(CONFIG_FILE, new LinkOption[0])) {
                try (BufferedReader reader = Files.newBufferedReader(CONFIG_FILE, StandardCharsets.UTF_8);){
                    root = JsonParser.parseReader((Reader)reader).getAsJsonObject();
                }
            } else {
                root = new JsonObject();
            }
            JsonObject uiObj = new JsonObject();
            uiObj.addProperty("iconOffsetX", (Number)PassiveConfig.ui.iconOffsetX);
            uiObj.addProperty("iconOffsetY", (Number)PassiveConfig.ui.iconOffsetY);
            uiObj.addProperty("iconSize", (Number)PassiveConfig.ui.iconSize);
            uiObj.addProperty("hudOffsetX", (Number)PassiveConfig.ui.hudOffsetX);
            uiObj.addProperty("hudOffsetY", (Number)PassiveConfig.ui.hudOffsetY);
            uiObj.addProperty("hudWidth", (Number)PassiveConfig.ui.hudWidth);
            uiObj.addProperty("hudMinHeight", (Number)PassiveConfig.ui.hudMinHeight);
            uiObj.addProperty("hudBgColor", String.format("0x%08X", PassiveConfig.ui.hudBgColor));
            uiObj.addProperty("hudLayer", (Number)PassiveConfig.ui.hudLayer);
            uiObj.addProperty("fontScale", (Number)Float.valueOf(PassiveConfig.ui.fontScale));
            root.add("uiSettings", (JsonElement)uiObj);
            try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_FILE, StandardCharsets.UTF_8, new OpenOption[0]);){
                GSON.toJson((JsonElement)root, (Appendable)writer);
            }
            CobblePassive.LOGGER.info("[CobblePassive] UI settings saved to {} successfully!", (Object)CONFIG_FILE);
        }
        catch (Exception ex) {
            CobblePassive.LOGGER.error("[CobblePassive] Failed to save UI config: {}", (Object)ex.getMessage());
        }
    }

    private static void copyDefaultFromResources() {
        try {
            Files.createDirectories(CONFIG_DIR, new FileAttribute[0]);
            try (InputStream in = PassiveConfig.class.getResourceAsStream("/default_pokepassive.json");){
                if (in != null) {
                    Files.copy(in, CONFIG_FILE, new CopyOption[0]);
                    CobblePassive.LOGGER.info("[CobblePassive] Copied default config to {}", (Object)CONFIG_FILE);
                }
            }
        }
        catch (Exception e) {
            CobblePassive.LOGGER.error("[CobblePassive] Could not copy default config: {}", (Object)e.getMessage());
        }
    }

    public static class PassiveEntry {
        public List<PotionEffect> effects = new ArrayList<PotionEffect>();
        public int lightLevel = 0;
        public float thornsDamage = 0.0f;
        public int fireAspectSeconds = 0;
        public int fortuneLevel = 0;
        public int oreRadarRadius = 0;
        public int radarIntervalSeconds = 7;
        public int pingDurationSeconds = 7;
        public List<String> targetOres = new ArrayList<String>();
        public float sneakSpeedBonus = 0.0f;
        public boolean fastDetox = false;
        public int respirationLevel = 0;
        public int bonusAirSupply = 0;
        public int fireProtectionLevel = 0;
        public int lavaStriderSeconds = 0;
        public double lavaStriderBlocksPerSec = 2.0;
        public boolean clearLavaVision = false;
        public double lavaFogClearPercent = 1.0;
        public boolean waterNightVision = false;
        public int cropGrowthRadius = 0;
        public double cropGrowthChance = 1.0;
        public double cropGrowthIntervalSeconds = 1.0;
        public int cropGrowthMinCrops = 2;
        public int maxOresDetected = 8;
        public float lifeSteal = 0.0f;
        public float lifeStealMewBonus = 1.0f;
        public double vineClimbSpeedBonus = 0.0;
        public float smiteBonusDamage = 0.0f;
        public float boneCrusherDamage = 0.0f;
        public double reachBonus = 0.0;
        public double breakSpeedBonus = 0.0;
        public int fishingLuckLevel = 0;
        public int fishingLureLevel = 0;
        public int inflictPoisonSeconds = 0;
        public boolean isMewDnaResonance = false;
        public int mewDnaTierBonus = 1;
        public float handBonusDamage = 0.0f;
        public boolean webImmunity = false;
        public int hungerFeedIntervalSeconds = 0;
        public int hungerFeedAmount = 2;
        public int mobGlowingRadius = 0;
        public boolean mobGlowingNightOnly = false;
        public int mobGlowingIntervalSeconds = 2;
        public int fastDetoxMaxDurationSeconds = 3;
        public float lavaVisionDistance = 64.0f;
        private final Set<Block> cachedTargetBlocks = new HashSet<Block>();
        private final Set<TagKey<Block>> cachedTargetTags = new HashSet<TagKey<Block>>();
        private boolean cacheInitialized = false;

        public void initTargetCache() {
            this.cachedTargetBlocks.clear();
            this.cachedTargetTags.clear();
            if (this.targetOres == null || this.targetOres.isEmpty()) {
                this.cachedTargetTags.add((TagKey<Block>)BlockTags.DIAMOND_ORES);
                this.cachedTargetTags.add((TagKey<Block>)BlockTags.EMERALD_ORES);
                this.cachedTargetTags.add((TagKey<Block>)BlockTags.GOLD_ORES);
                this.cachedTargetTags.add((TagKey<Block>)BlockTags.IRON_ORES);
                this.cachedTargetTags.add((TagKey<Block>)BlockTags.COPPER_ORES);
                this.cachedTargetTags.add((TagKey<Block>)BlockTags.LAPIS_ORES);
                this.cachedTargetTags.add((TagKey<Block>)BlockTags.REDSTONE_ORES);
                this.cachedTargetBlocks.add(Blocks.ANCIENT_DEBRIS);
            } else {
                for (String ore : this.targetOres) {
                    if (ore.startsWith("#")) {
                        ResourceLocation tagLoc = ResourceLocation.tryParse((String)ore.substring(1));
                        if (tagLoc == null) continue;
                        this.cachedTargetTags.add((TagKey<Block>)TagKey.create((ResourceKey)Registries.BLOCK, (ResourceLocation)tagLoc));
                        continue;
                    }
                    ResourceLocation blockLoc = ResourceLocation.tryParse((String)(ore.contains(":") ? ore : "minecraft:" + ore));
                    if (blockLoc == null) continue;
                    BuiltInRegistries.BLOCK.getOptional(blockLoc).ifPresent(this.cachedTargetBlocks::add);
                }
            }
            this.cacheInitialized = true;
        }

        public boolean matchesBlock(BlockState state) {
            if (state == null || state.isAir()) {
                return false;
            }
            if (!this.cacheInitialized) {
                this.initTargetCache();
            }
            if (this.cachedTargetBlocks.contains(state.getBlock())) {
                return true;
            }
            for (TagKey<Block> tag : this.cachedTargetTags) {
                if (!state.is(tag)) continue;
                return true;
            }
            return false;
        }
    }

    public static class UiSettings {
        public int iconOffsetX = 53;
        public int iconOffsetY = 4;
        public int iconSize = 10;
        public int hudOffsetX = 14;
        public int hudOffsetY = 1;
        public int hudWidth = 151;
        public int hudMinHeight = 72;
        public int hudBgColor = -13812604;
        public int hudLayer = 1500;
        public float fontScale = 1.0f;

        public UiSettings copy() {
            UiSettings s = new UiSettings();
            s.iconOffsetX = this.iconOffsetX;
            s.iconOffsetY = this.iconOffsetY;
            s.iconSize = this.iconSize;
            s.hudOffsetX = this.hudOffsetX;
            s.hudOffsetY = this.hudOffsetY;
            s.hudWidth = this.hudWidth;
            s.hudMinHeight = this.hudMinHeight;
            s.hudBgColor = this.hudBgColor;
            s.hudLayer = this.hudLayer;
            s.fontScale = this.fontScale;
            return s;
        }

        public void reset() {
            this.iconOffsetX = 53;
            this.iconOffsetY = 4;
            this.iconSize = 10;
            this.hudOffsetX = 14;
            this.hudOffsetY = 1;
            this.hudWidth = 151;
            this.hudMinHeight = 72;
            this.hudBgColor = -13812604;
            this.hudLayer = 1500;
            this.fontScale = 1.0f;
        }
    }

    public record PotionEffect(Holder<MobEffect> effect, int amplifier, boolean nightOnly, String biomeRequired, String itemInHand, Integer maxY) {
    }
}

