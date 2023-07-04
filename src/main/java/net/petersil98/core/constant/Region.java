package net.petersil98.core.constant;

/**
 * Enumeration of the possible Region in the Riot API
 */
public enum Region {
    AMERICA("america"),
    EUROPE("europe"),
    ASIA("asia"),
    SEA("sea");

    private final String name;

    Region(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * This Method returns the corresponding Region for a given Platform
     * @param platform The Platform
     * @return The corresponding Region
     */
    public static Region byPlatform(Platform platform) {
        switch (platform) {
            case EUW, EUNE, RU, TR -> {
                return EUROPE;
            }
            case BR, NA, LAS, LAN -> {
                return AMERICA;
            }
            case JP, KR -> {
                return ASIA;
            }
            case OCE -> {
                return SEA;
            }
        }
        return null;
    }
}
