package net.petersil98.core.constant;

/**
 * Enumeration of the possible Regions for the Account Endpoint
 */
public class Region {
    public static final Region AMERICA = new Region("america");
    public static final Region EUROPE = new Region("europe");
    public static final Region ASIA = new Region("asia");
    public static final Region SEA = new Region("sea");


    private final String name;

    protected Region(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
