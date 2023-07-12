package net.petersil98.core.model;

import net.petersil98.core.constant.Region;
import net.petersil98.core.http.RiotAPI;

import java.util.Objects;

public class Account {
    private String gameName;
    private String tagLine;
    private String puuid;

    public static Account getAccountByPuuid(String puuid, Region region) {
        return RiotAPI.requestRiotAccountEndpoint("accounts/by-puuid/", puuid, region, Account.class);
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
