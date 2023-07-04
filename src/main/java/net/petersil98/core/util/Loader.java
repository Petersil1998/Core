package net.petersil98.core.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import net.petersil98.core.constant.Constants;
import net.petersil98.core.util.settings.Language;
import net.petersil98.core.util.settings.Settings;
import org.apache.logging.log4j.core.util.IOUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;

import static net.petersil98.core.Core.MAPPER;

/**
 * The Loader Class is used to updated and load static Data. There will be created a Folder named "data" the Base Directory,
 * which then holds a settings File. The Settings File contains the latest version of the Data Dragon (DDragon) and the language.
 * If one of those values changes, all Files will be updated with the latest data in the specified Language.
 */
public abstract class Loader {

    private static final List<Loader> LOADERS = new ArrayList<>();

    private static final String DATA_FOLDER = "data" + File.separator;
    protected static final String BASE_PATH = System.getProperty("user.dir") + File.separator + DATA_FOLDER;

    private static final String SETTINGS = BASE_PATH + "settings.json";
    private static final String DDRAGON_VERSION = "ddragonVersion";
    private static final String LANGUAGE = "language";

    /**
     * This Method is used to add a Loader, which should get called when {@link #init()} gets called.
     * Loaders have to extend this class and therefore implement the {@link #load(boolean) load} Method
     * @param loader The Loader, which should get called on init
     */
    public static void addLoader(Loader loader) {
        LOADERS.add(loader);
    }

    /**
     * This Method needs to be called before API calls are being made,
     * but after the settings are set (at least the {@link Settings#setLanguage(Language)})
     * For this library to work properly. It updates and loads static data
     * from the Data Dragon and Community Dragon.
     *
     * @see Settings
     */
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

    /**
     * This Method gets called on Loaders, which got added by {@link #addLoader(Loader)}.
     * It should update the data files and load that data into a Collection of Objects.
     *
     * @param shouldUpdate Whether the data files should be updated
     */
    protected abstract void load(boolean shouldUpdate);

    /**
     * This Method gets the newest Version of the Data Dragon and compares if with the one in the Settings File.
     * If the Versions don't match or the Language set in {@link Settings} don't match the one in the Settings File <b>true</b> is returned,
     * <b>false</b> otherwise.
     * <br>
     * The Settings File is also updated with the new values.
     * @return A <b>boolean</b> whether the data files should be updated.
     */
    private static boolean initAndUpdateSettings(){
        String url = Constants.DDRAGON_BASE_PATH + "api/versions.json";
        try {
            InputStream in = URI.create(url).toURL().openConnection().getInputStream();
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

    /**
     * Utility Method for checking the structure of the settings file.
     * @return a {@link Map.Entry Pair} consisting of:
     * <ul>
     *     <li>A <b>Boolean</b> that indicates, whether the settings file is valid</li>
     *     <li>A {@link JsonNode} that represents the settings as a Json Object.
     *     If the Settings File isn't valid, <b>null</b> is returned instead</li>
     * </ul>
     */
    private static Map.Entry<Boolean, JsonNode> parseSettingsFile() {
        try {
            JsonNode settings = MAPPER.readTree(new File(SETTINGS));
            return new AbstractMap.SimpleEntry<>(settings.has(DDRAGON_VERSION) && settings.has(LANGUAGE), settings);
        } catch (IOException ignored) {
            return new AbstractMap.SimpleEntry<>(false, null);
        }
    }
}