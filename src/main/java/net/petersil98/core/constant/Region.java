package net.petersil98.core.constant;

/**
 * Enumeration of the possible Regions in the Account Endpoint
 */
public enum Region {
    AMERICA("americas"),
    EUROPE("europe"),
    ASIA("asia"),
    E_SPORTS("esports");

    private final String name;

    Region(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
