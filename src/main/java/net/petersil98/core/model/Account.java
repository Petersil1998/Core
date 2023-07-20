package net.petersil98.core.model;

import net.petersil98.core.constant.Region;
import net.petersil98.core.http.RiotAPI;

import java.util.Objects;

/**
 * Data Class that represents a Player's Account
 */
public class Account {

    private String gameName;
    private String tagLine;
    private String puuid;

    /**
     * Get an Account by its <b>PUUID</b> in a specific {@link Region}. PUUIDs are unique globally.
     * All IDs are encrypted with the used API Key, so you need to you the same API Key when working with this IDs
     * @param puuid The PUUID of the Summoner
     * @param region The Region to make the request to
     * @return An Account if the Request was successful, <code>null</code> otherwise
     */
    public static Account getAccountByPuuid(String puuid, Region region) {
        return RiotAPI.requestRiotAccountEndpoint("accounts/by-puuid/", puuid, region, Account.class);
    }

    /**
     * Get an Account by its <b>Riot ID</b> consisting of its <b>Game Name</b> and <b>Tag Line</b> in a {@link Region}.
     * @param name The Game Name of the Account
     * @param tag The Tag Line of the Account
     * @param region The Region to make the request to
     * @return An Account if the Request was successful, <code>null</code> otherwise
     */
    public static Account getAccountByRiotId(String name, String tag, Region region) {
        return RiotAPI.requestRiotAccountEndpoint("accounts/by-riot-id/", name + "/" + tag, region, Account.class);
    }

    public String getGameName() {
        return this.gameName;
    }

    public String getTagLine() {
        return this.tagLine;
    }

    public String getPuuid() {
        return this.puuid;
    }

    @Override
    public String toString() {
        return this.gameName + "#" + this.tagLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(puuid, account.puuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(puuid);
    }
}
