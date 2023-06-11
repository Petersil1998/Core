package net.petersil98.core.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import net.petersil98.core.constant.Constants;
import net.petersil98.core.util.settings.Settings;
import org.apache.logging.log4j.core.util.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.*;

import static net.petersil98.core.Core.MAPPER;

public abstract class Loader {

    private static final List<Loader> LOADERS = new ArrayList<>();

    private static final String DATA_FOLDER = "data" + File.separator;
    private static final String BASE_PATH = System.getProperty("user.dir") + File.separator + DATA_FOLDER;
    private static final String LOL_BASE_PATH = BASE_PATH + "lol" + File.separator;
    private static final String TFT_BASE_PATH = BASE_PATH + "tft" + File.separator;

    public static Map<Integer, List<Integer>> ITEMS_FROM = new HashMap<>();
    public static Map<Integer, List<Integer>> ITEMS_INTO = new HashMap<>();
    public static Map<Integer, Integer> ITEMS_SPECIAL_RECIPE = new HashMap<>();

    private static final String SETTINGS = BASE_PATH + "settings.json";
    private static final String DDRAGON_VERSION = "ddragonVersion";
    private static final String LANGUAGE = "language";

    public static void addLoader(Loader loader) {
        LOADERS.add(loader);
    }

    public static void init() {
        new File(DATA_FOLDER).mkdirs();
        try {
            new File(SETTINGS).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean shouldUpdate = initAndUpdateSettings();
        LOADERS.forEach(loader -> loader.load(shouldUpdate));
    }

    protected abstract void load(boolean shouldUpdate);

    private static boolean initAndUpdateSettings(){
        String url = Constants.DDRAGON_BASE_PATH + "api/versions.json";
        try {
            InputStream in = new URL(url).openConnection().getInputStream();
            String[] versions = MAPPER.readValue(IOUtils.toString(new InputStreamReader(in)), TypeFactory.defaultInstance().constructArrayType(String.class));
            Constants.DDRAGON_VERSION = versions[0];
            Map.Entry<Boolean, JsonNode> result = parseSettingsFile();
            boolean areFieldsSet = result.getKey();
            JsonNode settings = result.getValue();
            if(!areFieldsSet || !settings.get(DDRAGON_VERSION).asText().equals(versions[0])
                    || !settings.get(LANGUAGE).asText().equals(Settings.getLanguage().toString())) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(SETTINGS));
                ObjectNode root = MAPPER.createObjectNode();
                root.put(DDRAGON_VERSION, versions[0]);
                root.put(LANGUAGE, Settings.getLanguage().toString());
                writer.write(root.toString());
                writer.close();
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private static Map.Entry<Boolean, JsonNode> parseSettingsFile() {
        try {
            JsonNode settings = MAPPER.readTree(new File(SETTINGS));
            return new AbstractMap.SimpleEntry<>(settings.has(DDRAGON_VERSION) && settings.has(LANGUAGE), settings);
        } catch (IOException ignored) {
            return new AbstractMap.SimpleEntry<>(false, null);
        }
    }
}