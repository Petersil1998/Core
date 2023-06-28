package net.petersil98.core.util.settings;

import net.petersil98.core.util.Loader;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalUnit;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class Settings {

    private static ServerConfig currentConfig = ServerConfig.EUW_CONFIG;
    private static Language language = Language.EN_US;
    private static Supplier<String> encryptedKey;
    private static Function<String, String> decryptor = (s -> s);
    private static boolean shouldCache = true;

    public static ServerConfig getCurrentConfig() {
        return Settings.currentConfig;
    }

    public static void setServerConfig(ServerConfig newConfig) {
        if (newConfig != null) Settings.currentConfig = newConfig;
    }

    public static Language getLanguage() {
        return Settings.language;
    }

    public static void setLanguage(Language newLanguage) {
        if (newLanguage != null) Settings.language = newLanguage;
    }

    public static String getAPIKey() {
        return Settings.decryptor.apply(Settings.encryptedKey.get());
    }

    public static void setAPIKey(Supplier<String> encryptedKey) {
        if (encryptedKey != null) Settings.encryptedKey = encryptedKey;
    }

    public static void setDecryptor(Function<String, String> decryptor) {
        if (decryptor != null) Settings.decryptor = decryptor;
    }

    public static void setShouldCache(boolean shouldCache) {
        Settings.shouldCache = shouldCache;
    }

    public static boolean shouldCache() {
        return shouldCache;
    }
}
