package net.petersil98.core.constant;

/**
 * Enumeration of the possible Ranked Tiers in the Riot API
 */
public enum RankedTier {
    IRON(false),
    BRONZE(false),
    SILVER(false),
    GOLD(false),
    PLATINUM(false),
    DIAMOND(false),
    MASTER(true),
    GRANDMASTER(true),
    CHALLENGER(true);

    private final boolean isApex;

    RankedTier(boolean isApex) {
        this.isApex = isApex;
    }

    public boolean isApex() {
        return this.isApex;
    }
}
