package net.petersil98.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.petersil98.core.constant.Platform;
import net.petersil98.core.http.RiotAPI;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Summoner {

    private String id;
    private String accountId;
    private String puuid;
    private String name;
    @JsonProperty(value = "profileIconId")
    private int profileIcon;
    private long revisionDate;
    private int summonerLevel;

    public static Summoner getSummonerByName(String summonerName, Platform platform) {
        return RiotAPI.requestLoLSummonerEndpoint("summoners/by-name/", URLEncoder.encode(summonerName, StandardCharsets.UTF_8), platform, Summoner.class);
    }

    public static Summoner getSummonerByAccountID(String accountID, Platform platform) {
        return RiotAPI.requestLoLSummonerEndpoint("summoners/by-account/", accountID, platform, Summoner.class);
    }

    public static Summoner getSummonerByPUUID(String puuid, Platform platform) {
        return RiotAPI.requestLoLSummonerEndpoint("summoners/by-puuid/", puuid, platform, Summoner.class);
    }

    public static Summoner getSummonerByID(String id, Platform platform) {
        return RiotAPI.requestLoLSummonerEndpoint("summoners/", id, platform, Summoner.class);
    }

    public String getId() {
        return this.id;
    }

    public String getAccountId() {
        return this.accountId;
    }

    public String getPuuid() {
        return this.puuid;
    }

    public String getName() {
        return this.name;
    }

    public int getProfileIcon() {
        return this.profileIcon;
    }

    public long getRevisionDate() {
        return this.revisionDate;
    }

    public int getSummonerLevel() {
        return this.summonerLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Summoner summoner = (Summoner) o;
        return Objects.equals(puuid, summoner.puuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(puuid);
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
