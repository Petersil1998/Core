package net.petersil98.core.constant;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Enumeration of the possible Platforms for the Account Endpoint
 */
public class Platform {

    private final String name;

    protected Platform(String name) {
        this.name = name;
    }

    /**
     * Utility Method to get a platform by its name (case-insensitive)
     * @param platformName The Name of the Platform
     * @return The platform if found, {@code null} otherwise
     */
    public static Platform getPlatform(String platformName) {
        for (Platform platform: Platform.values(Platform.class)) {
            if (platform.name.equalsIgnoreCase(platformName)) return platform;
        }
        return null;
    }

    /**
     * Utility Method to get all the constants of a class similar to how <b><code>values()</code></b> works in enums.
     * A Field is considered a constant if its <b>public</b>, <b>static</b> and <b>final</b>
     * @param clazz Class containing the constants
     * @return List of the Constants
     */
    protected static <T extends Platform> List<T> values(Class<T> clazz) {
        List<T> platforms = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                try {
                    platforms.add((T) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return platforms;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
