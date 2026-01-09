package schnerry.seymouranalyzer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import schnerry.seymouranalyzer.Seymouranalyzer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class compatible with Cloth Config
 * Stores all mod settings with proper getters/setters
 */
public class ClothConfig {
    private static ClothConfig INSTANCE;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File configDir;
    private final File configFile;
    private final File dataFile;

    // Toggle settings - Analysis Features
    private boolean infoBoxEnabled = true;
    private boolean highlightsEnabled = true;
    private boolean wordsEnabled = true;
    private boolean patternsEnabled = true;
    private boolean dupesEnabled = true;

    // Toggle settings - Filter Options
    private boolean fadeDyesEnabled = true;
    private boolean customColorsEnabled = true;
    private boolean showHighFades = true;
    private boolean threePieceSetsEnabled = true;
    private boolean pieceSpecificEnabled = false;

    // Toggle settings - Scanning
    private boolean itemFramesEnabled = false;

    // Match priorities - Higher in list = higher priority for highlights
    private java.util.List<MatchPriority> matchPriorities = getDefaultMatchPriorities();

    // Custom data
    private Map<String, String> customColors = new HashMap<>();
    private Map<String, String> wordList = new HashMap<>();

    private ClothConfig() {
        configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "seymouranalyzer");
        configFile = new File(configDir, "config.json");
        dataFile = new File(configDir, "data.json");

        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        load();
    }

    public static ClothConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClothConfig();
        }
        return INSTANCE;
    }

    public void load() {
        try {
            if (configFile.exists()) {
                JsonObject json = GSON.fromJson(new FileReader(configFile), JsonObject.class);

                if (json.has("infoBoxEnabled")) infoBoxEnabled = json.get("infoBoxEnabled").getAsBoolean();
                if (json.has("highlightsEnabled")) highlightsEnabled = json.get("highlightsEnabled").getAsBoolean();
                if (json.has("fadeDyesEnabled")) fadeDyesEnabled = json.get("fadeDyesEnabled").getAsBoolean();
                if (json.has("threePieceSetsEnabled")) threePieceSetsEnabled = json.get("threePieceSetsEnabled").getAsBoolean();
                if (json.has("pieceSpecificEnabled")) pieceSpecificEnabled = json.get("pieceSpecificEnabled").getAsBoolean();
                if (json.has("wordsEnabled")) wordsEnabled = json.get("wordsEnabled").getAsBoolean();
                if (json.has("patternsEnabled")) patternsEnabled = json.get("patternsEnabled").getAsBoolean();
                if (json.has("customColorsEnabled")) customColorsEnabled = json.get("customColorsEnabled").getAsBoolean();
                if (json.has("dupesEnabled")) dupesEnabled = json.get("dupesEnabled").getAsBoolean();
                if (json.has("showHighFades")) showHighFades = json.get("showHighFades").getAsBoolean();
                if (json.has("itemFramesEnabled")) itemFramesEnabled = json.get("itemFramesEnabled").getAsBoolean();

                if (json.has("matchPriorities")) {
                    matchPriorities = new java.util.ArrayList<>();
                    json.getAsJsonArray("matchPriorities").forEach(element -> {
                        MatchPriority priority = MatchPriority.fromName(element.getAsString());
                        if (priority != null) {
                            matchPriorities.add(priority);
                        }
                    });
                    // Add any missing priorities at the end
                    for (MatchPriority priority : MatchPriority.values()) {
                        if (!matchPriorities.contains(priority)) {
                            matchPriorities.add(priority);
                        }
                    }
                }

                Seymouranalyzer.LOGGER.info("Loaded config from file");
            }
        } catch (Exception e) {
            Seymouranalyzer.LOGGER.error("Failed to load config", e);
        }

        // Load custom data
        try {
            if (dataFile.exists()) {
                JsonObject json = GSON.fromJson(new FileReader(dataFile), JsonObject.class);

                if (json.has("customColors")) {
                    JsonObject colors = json.getAsJsonObject("customColors");
                    colors.entrySet().forEach(entry -> {
                        customColors.put(entry.getKey(), entry.getValue().getAsString());
                    });
                }

                if (json.has("wordList")) {
                    JsonObject words = json.getAsJsonObject("wordList");
                    words.entrySet().forEach(entry -> {
                        wordList.put(entry.getKey(), entry.getValue().getAsString());
                    });
                }
            }
        } catch (Exception e) {
            Seymouranalyzer.LOGGER.error("Failed to load data", e);
        }
    }

    public void save() {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("infoBoxEnabled", infoBoxEnabled);
            json.addProperty("highlightsEnabled", highlightsEnabled);
            json.addProperty("fadeDyesEnabled", fadeDyesEnabled);
            json.addProperty("threePieceSetsEnabled", threePieceSetsEnabled);
            json.addProperty("pieceSpecificEnabled", pieceSpecificEnabled);
            json.addProperty("wordsEnabled", wordsEnabled);
            json.addProperty("patternsEnabled", patternsEnabled);
            json.addProperty("customColorsEnabled", customColorsEnabled);
            json.addProperty("dupesEnabled", dupesEnabled);
            json.addProperty("showHighFades", showHighFades);
            json.addProperty("itemFramesEnabled", itemFramesEnabled);

            com.google.gson.JsonArray prioritiesArray = new com.google.gson.JsonArray();
            matchPriorities.forEach(priority -> prioritiesArray.add(priority.name()));
            json.add("matchPriorities", prioritiesArray);

            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(json, writer);
            }

            Seymouranalyzer.LOGGER.info("Saved config to file");
        } catch (Exception e) {
            Seymouranalyzer.LOGGER.error("Failed to save config", e);
        }
    }

    public void saveData() {
        try {
            JsonObject json = new JsonObject();

            JsonObject colors = new JsonObject();
            customColors.forEach(colors::addProperty);
            json.add("customColors", colors);

            JsonObject words = new JsonObject();
            wordList.forEach(words::addProperty);
            json.add("wordList", words);

            try (FileWriter writer = new FileWriter(dataFile)) {
                GSON.toJson(json, writer);
            }
        } catch (Exception e) {
            Seymouranalyzer.LOGGER.error("Failed to save data", e);
        }
    }

    // Getters and Setters for Analysis Features
    public boolean isInfoBoxEnabled() {
        return infoBoxEnabled;
    }

    public void setInfoBoxEnabled(boolean infoBoxEnabled) {
        this.infoBoxEnabled = infoBoxEnabled;
    }

    public boolean isHighlightsEnabled() {
        return highlightsEnabled;
    }

    public void setHighlightsEnabled(boolean highlightsEnabled) {
        this.highlightsEnabled = highlightsEnabled;
    }

    public boolean isWordsEnabled() {
        return wordsEnabled;
    }

    public void setWordsEnabled(boolean wordsEnabled) {
        this.wordsEnabled = wordsEnabled;
    }

    public boolean isPatternsEnabled() {
        return patternsEnabled;
    }

    public void setPatternsEnabled(boolean patternsEnabled) {
        this.patternsEnabled = patternsEnabled;
    }

    public boolean isDupesEnabled() {
        return dupesEnabled;
    }

    public void setDupesEnabled(boolean dupesEnabled) {
        this.dupesEnabled = dupesEnabled;
    }

    // Getters and Setters for Filter Options
    public boolean isFadeDyesEnabled() {
        return fadeDyesEnabled;
    }

    public void setFadeDyesEnabled(boolean fadeDyesEnabled) {
        this.fadeDyesEnabled = fadeDyesEnabled;
    }

    public boolean isCustomColorsEnabled() {
        return customColorsEnabled;
    }

    public void setCustomColorsEnabled(boolean customColorsEnabled) {
        this.customColorsEnabled = customColorsEnabled;
    }

    public boolean isShowHighFades() {
        return showHighFades;
    }

    public void setShowHighFades(boolean showHighFades) {
        this.showHighFades = showHighFades;
    }

    public boolean isThreePieceSetsEnabled() {
        return threePieceSetsEnabled;
    }

    public void setThreePieceSetsEnabled(boolean threePieceSetsEnabled) {
        this.threePieceSetsEnabled = threePieceSetsEnabled;
    }

    public boolean isPieceSpecificEnabled() {
        return pieceSpecificEnabled;
    }

    public void setPieceSpecificEnabled(boolean pieceSpecificEnabled) {
        this.pieceSpecificEnabled = pieceSpecificEnabled;
    }

    // Getters and Setters for Scanning
    public boolean isItemFramesEnabled() {
        return itemFramesEnabled;
    }

    public void setItemFramesEnabled(boolean itemFramesEnabled) {
        this.itemFramesEnabled = itemFramesEnabled;
    }

    // Custom data
    public Map<String, String> getCustomColors() {
        return customColors;
    }

    public Map<String, String> getWordList() {
        return wordList;
    }

    // Match Priorities
    public java.util.List<MatchPriority> getMatchPriorities() {
        return matchPriorities;
    }

    public void setMatchPriorities(java.util.List<MatchPriority> matchPriorities) {
        this.matchPriorities = matchPriorities;
        // Clear highlight cache so items re-calculate with new priorities
        schnerry.seymouranalyzer.render.ItemSlotHighlighter.getInstance().clearCache();
    }

    /**
     * Get priority index (lower number = higher priority)
     * Returns -1 if not found
     */
    public int getPriorityIndex(MatchPriority priority) {
        return matchPriorities.indexOf(priority);
    }

    /**
     * Default priority order
     * Search > Dupe > Word > Pattern > Custom T1/T2 > Normal T0/T1/T2 > Fade T0/T1/T2
     * Note: Custom colors only have T1 and T2 (no T0)
     */
    public static java.util.List<MatchPriority> getDefaultMatchPriorities() {
        return java.util.Arrays.asList(
            MatchPriority.SEARCH,
            MatchPriority.DUPE,
            MatchPriority.WORD,
            MatchPriority.PATTERN,
            MatchPriority.CUSTOM_T1,
            MatchPriority.CUSTOM_T2,
            MatchPriority.NORMAL_T0,
            MatchPriority.NORMAL_T1,
            MatchPriority.NORMAL_T2,
            MatchPriority.FADE_T0,
            MatchPriority.FADE_T1,
            MatchPriority.FADE_T2
        );
    }
}

