package net.petersil98.core.util.settings;

import net.petersil98.core.util.Loader;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class holds the current Settings used by the library:
 * <ul>
 *     <li><b>Language</b></li>
 *     <li><b>API Key</b></li>
 *     <li><b>Decryptor</b> used to decrypt the API Key</li>
 * </ul>
 * The Language should be set at the start of the application. The language only affects the language of the static data.
 * Changing the language only takes affect if {@link Loader#init()} is called afterward.
 * <br>
 * The API Key and Decryptor need to be set before API Requests are being made.
 */
public class Settings {
    private static Language language = Language.EN_US;
    private static Supplier<String> encryptedKey;
    private static Function<String, String> decryptor = (s -> s);
    private static boolean shouldCache = false;

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

    public static void useCache(boolean shouldCache) {
        Settings.shouldCache = shouldCache;
    }

    public static boolean useCache() {
        return shouldCache;
    }
}
