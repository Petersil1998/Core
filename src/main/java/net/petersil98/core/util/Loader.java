package net.petersil98.core.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import net.petersil98.core.constant.Constants;
import net.petersil98.core.util.settings.Language;
import net.petersil98.core.util.settings.Settings;
import org.apache.logging.log4j.core.util.IOUtils;

import java.io.*;
import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.petersil98.core.Core.MAPPER;

/**
 * The Loader Class is used to updated and load static Data. There will be created a Folder named "data" the Base Directory,
 * which then holds a settings File. The Settings File contains the latest version of the Data Dragon (DDragon) and the language.
 * If one of those values changes, all Files will be updated with the latest data in the specified Language.
 */
public abstract class Loader {

    private static final List<Loader> LOADERS = new ArrayList<>();
    private static Language latestLanguage;
    private static final Thread UPDATE_CHECKER = new Thread(() -> {
        while (true) {
            if(latestLanguage != Settings.getLanguage()) {
                latestLanguage = Settings.getLanguage();
                LOADERS.forEach(Loader::load);
            } else {
                LOADERS.stream().filter(Loader::shouldReloadData).forEach(Loader::load);
            }
            try {
                Thread.sleep(Duration.of(30, ChronoUnit.MINUTES));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    /**
     * This Method is used to add a Loader, which should get called when {@link #init()} gets called.
     * Loaders have to extend this class and therefore implement the {@link #load} Method
     * @param loader The Loader, which should get called on init
     */
    public static void addLoader(Loader loader) {
        LOADERS.add(loader);
    }

    /**
     * This Method needs to be called before API calls are being made,
     * but after the settings are set (at least the {@link Settings#setLanguage(Language)})
     * For this library to work properly. It updates and loads static data from the Data Dragon and Community Dragon.
     * This Method also starts a Thread which periodically checks for a new Version of the Data Dragon.
     *
     * @see Settings
     */
    public static void init() {
        latestLanguage = Settings.getLanguage();
        LOADERS.forEach(Loader::load);
        if(UPDATE_CHECKER.getState().equals(Thread.State.NEW)) UPDATE_CHECKER.start();
    }

    /**
     * This Method gets called on Loaders, which got added by {@link #addLoader(Loader)}.
     * It should get the latest data load it into a Collection of Objects.
     */
    protected abstract void load();

    /**
     * This Method gets called on Loaders, which got added by {@link #addLoader(Loader)}.
     * It should get the newest Version for the static Data and compare it with the latest used Version.
     * If the Versions don't match or the Language set in {@link Settings} don't match the one in {@link #latestLanguage} <b>true</b> is returned,
     * <b>false</b> otherwise.
     * @return A <b>boolean</b> whether the Data Collections should be updated.
     */
    protected abstract boolean shouldReloadData();
}