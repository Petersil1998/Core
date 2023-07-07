package net.petersil98.core.http;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.petersil98.core.Core;
import net.petersil98.core.constant.Constants;
import net.petersil98.core.constant.Platform;
import net.petersil98.core.constant.Region;
import net.petersil98.core.http.exceptions.BadRequestException;
import net.petersil98.core.http.exceptions.ForbiddenException;
import net.petersil98.core.http.exceptions.NotFoundException;
import net.petersil98.core.http.exceptions.UnauthorizedException;
import net.petersil98.core.http.ratelimit.BlockingRateLimiter;
import net.petersil98.core.http.ratelimit.IPermit;
import net.petersil98.core.http.ratelimit.RateLimiter;
import net.petersil98.core.util.settings.Settings;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


/**
 * This class provides Methods to make requests to each supported Endpoint to the Riot Games API
 */
public class RiotAPI {

    private static final Marker MARKER = MarkerManager.getMarker(RiotAPI.class.getSimpleName());
    private static final Cache<String, HttpResponse<String>> CACHE = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(Duration.ofMinutes(10)).build();

    private static final String SUMMONER_V4 = "summoner/v4/";
    private static final String CHAMPION_MASTERY_V4 = "champion-mastery/v4/";
    private static final String LEAGUE_V4 = "league/v4/";
    private static final String SPECTATOR_V4 = "spectator/v4/";
    private static final String MATCH_V5 = "match/v5/";
    private static final String ACCOUNT_V1 = "account/v1/";
    private static final String LEAGUE_V1 = "league/v1/";
    private static final String MATCH_V1 = "match/v1/";

    public static RateLimiter rateLimiter = new BlockingRateLimiter();

    private static HttpResponse<String> request(String url, Map<String, String> params) {
        HashMap<String, String> mutableMap = new HashMap<>(params);
        mutableMap.put("api_key", Settings.getAPIKey());
        return HTTPClient.getInstance().get(url, mutableMap);
    }

    /**
     * Requests the LoL {@link RiotAPI#SUMMONER_V4} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLSummonerEndpoint(String method, String args, Platform platform, Class<T> requiredClass) {
        return requestLoLSummonerEndpoint(method, args, platform, requiredClass, new HashMap<>());
    }

    /**
     * Requests the LoL {@link RiotAPI#SUMMONER_V4} endpoint. If successful, the Response is mapped to the desired {@link TypeBase}.
     * This method is intended to be used for {@link com.fasterxml.jackson.databind.type.CollectionType CollectionTypes} or
     * {@link com.fasterxml.jackson.databind.type.MapType MapTypes}.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLSummonerEndpoint(String method, String args, Platform platform, TypeBase requiredClass) {
        return requestLoLSummonerEndpoint(method, args, platform, requiredClass, new HashMap<>());
    }

    /**
     * Requests the LoL {@link RiotAPI#SUMMONER_V4} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLSummonerEndpoint(String method, String args, Platform platform, Class<T> requiredClass, Map<String, String> filter) {
        return requestLoLSummonerEndpoint(method, args, platform, TypeFactory.defaultInstance().constructType(requiredClass), filter);
    }

    /**
     * Requests the LoL {@link RiotAPI#SUMMONER_V4} endpoint. If successful, the Response is mapped to the desired {@link JavaType} <b>{@code requiredClass}</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLSummonerEndpoint(String method, String args, Platform platform, JavaType requiredClass, Map<String, String> filter) {
        return handleCacheAndRateLimiter(
                constructUrl(SUMMONER_V4 + method + args, AppType.LOL, platform),
                SUMMONER_V4 + method, Region.byPlatform(platform), requiredClass, filter);
    }

    /**
     * Requests the LoL {@link RiotAPI#CHAMPION_MASTERY_V4} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLChampionMasteryEndpoint(String method, String args, Platform platform, Class<T> requiredClass) {
        return requestLoLChampionMasteryEndpoint(method, args, platform, requiredClass, new HashMap<>());
    }

    /**
     * Requests the LoL {@link RiotAPI#CHAMPION_MASTERY_V4} endpoint. If successful, the Response is mapped to the desired {@link TypeBase}.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * This method is intended to be used for {@link com.fasterxml.jackson.databind.type.CollectionType CollectionTypes} or
     * {@link com.fasterxml.jackson.databind.type.MapType MapTypes}.
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLChampionMasteryEndpoint(String method, String args, Platform platform, TypeBase requiredClass) {
        return requestLoLChampionMasteryEndpoint(method, args, platform, requiredClass, new HashMap<>());
    }

    /**
     * Requests the LoL {@link RiotAPI#CHAMPION_MASTERY_V4} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLChampionMasteryEndpoint(String method, String args, Platform platform, Class<T> requiredClass, Map<String, String> filter) {
        return requestLoLChampionMasteryEndpoint(method, args, platform, TypeFactory.defaultInstance().constructType(requiredClass), filter);
    }

    /**
     * Requests the LoL {@link RiotAPI#CHAMPION_MASTERY_V4} endpoint. If successful, the Response is mapped to the desired {@link JavaType} <b>{@code requiredClass}</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLChampionMasteryEndpoint(String method, String args, Platform platform, JavaType requiredClass, Map<String, String> filter) {
        return handleCacheAndRateLimiter(
                constructUrl(CHAMPION_MASTERY_V4 + method + args, AppType.LOL, platform),
                CHAMPION_MASTERY_V4 + method, Region.byPlatform(platform), requiredClass, filter);
    }

    /**
     * Requests the LoL {@link RiotAPI#LEAGUE_V4} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLLeagueEndpoint(String method, String args, Platform platform, Class<T> requiredClass) {
        return requestLoLLeagueEndpoint(method, args, platform, requiredClass, new HashMap<>());
    }

    /**
     * Requests the LoL {@link RiotAPI#LEAGUE_V4} endpoint. If successful, the Response is mapped to the desired {@link TypeBase}.
     * This method is intended to be used for {@link com.fasterxml.jackson.databind.type.CollectionType CollectionTypes} or
     * {@link com.fasterxml.jackson.databind.type.MapType MapTypes}.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLLeagueEndpoint(String method, String args, Platform platform, TypeBase requiredClass) {
        return requestLoLLeagueEndpoint(method, args, platform, requiredClass, new HashMap<>());
    }

    /**
     * Requests the LoL {@link RiotAPI#LEAGUE_V4} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLLeagueEndpoint(String method, String args, Platform platform, Class<T> requiredClass, Map<String, String> filter) {
        return requestLoLLeagueEndpoint(method, args, platform, TypeFactory.defaultInstance().constructType(requiredClass), filter);
    }

    /**
     * Requests the LoL {@link RiotAPI#LEAGUE_V4} endpoint. If successful, the Response is mapped to the desired {@link JavaType} <b>{@code requiredClass}</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLLeagueEndpoint(String method, String args, Platform platform, JavaType requiredClass, Map<String, String> filter) {
        return handleCacheAndRateLimiter(
                constructUrl(LEAGUE_V4 + method + args, AppType.LOL, platform),
                LEAGUE_V4 + method, Region.byPlatform(platform), requiredClass, filter);
    }

    /**
     * Requests the LoL {@link RiotAPI#SPECTATOR_V4} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLSpectatorEndpoint(String method, String args, Platform platform, Class<T> requiredClass) {
        return requestLoLSpectatorEndpoint(method, args, platform, requiredClass, new HashMap<>());
    }

    /**
     * Requests the LoL {@link RiotAPI#SPECTATOR_V4} endpoint. If successful, the Response is mapped to the desired {@link TypeBase}.
     * This method is intended to be used for {@link com.fasterxml.jackson.databind.type.CollectionType CollectionTypes} or
     * {@link com.fasterxml.jackson.databind.type.MapType MapTypes}.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLSpectatorEndpoint(String method, String args, Platform platform, TypeBase requiredClass) {
        return requestLoLSpectatorEndpoint(method, args, platform, requiredClass, new HashMap<>());
    }

    /**
     * Requests the LoL {@link RiotAPI#SPECTATOR_V4} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLSpectatorEndpoint(String method, String args, Platform platform, Class<T> requiredClass, Map<String, String> filter) {
        return requestLoLSpectatorEndpoint(method, args, platform, TypeFactory.defaultInstance().constructType(requiredClass), filter);
    }

    /**
     * Requests the LoL {@link RiotAPI#SPECTATOR_V4} endpoint. If successful, the Response is mapped to the desired {@link JavaType} <b>{@code requiredClass}</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLSpectatorEndpoint(String method, String args, Platform platform, JavaType requiredClass, Map<String, String> filter) {
        return handleCacheAndRateLimiter(
                constructUrl(SPECTATOR_V4 + method + args, AppType.LOL, platform),
                SPECTATOR_V4 + method, Region.byPlatform(platform), requiredClass, filter);
    }

    /**
     * Requests the LoL {@link RiotAPI#MATCH_V5} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param region Region to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLMatchEndpoint(String method, String args, Region region, Class<T> requiredClass) {
        return requestLoLMatchEndpoint(method, args, region, requiredClass, new HashMap<>());
    }

    /**
     * Requests the LoL {@link RiotAPI#MATCH_V5} endpoint. If successful, the Response is mapped to the desired {@link TypeBase}.
     * This method is intended to be used for {@link com.fasterxml.jackson.databind.type.CollectionType CollectionTypes} or
     * {@link com.fasterxml.jackson.databind.type.MapType MapTypes}.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param region Region to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLMatchEndpoint(String method, String args, Region region, TypeBase requiredClass) {
        return requestLoLMatchEndpoint(method, args, region, requiredClass, new HashMap<>());
    }

    /**
     * Requests the LoL {@link RiotAPI#MATCH_V5} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param region Region to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLMatchEndpoint(String method, String args, Region region, Class<T> requiredClass, Map<String, String> filter) {
        return requestLoLMatchEndpoint(method, args, region, TypeFactory.defaultInstance().constructType(requiredClass), filter);
    }

    /**
     * Requests the LoL {@link RiotAPI#MATCH_V5} endpoint. If successful, the Response is mapped to the desired {@link JavaType} <b>{@code requiredClass}</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param region Region to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestLoLMatchEndpoint(String method, String args, Region region, JavaType requiredClass, Map<String, String> filter) {
        return handleCacheAndRateLimiter(
                constructUrl(MATCH_V5 + method + args, AppType.LOL, region),
                MATCH_V5 + method, region, requiredClass, filter);
    }

    /**
     * Requests the Riot {@link RiotAPI#ACCOUNT_V1} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param region Region to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestRiotAccountEndpoint(String method, String args, Region region, Class<T> requiredClass) {
        return requestRiotAccountEndpoint(method, args, region, requiredClass, new HashMap<>());
    }

    /**
     * Requests the Riot {@link RiotAPI#ACCOUNT_V1} endpoint. If successful, the Response is mapped to the desired {@link TypeBase}.
     * This method is intended to be used for {@link com.fasterxml.jackson.databind.type.CollectionType CollectionTypes} or
     * {@link com.fasterxml.jackson.databind.type.MapType MapTypes}.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param region Region to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestRiotAccountEndpoint(String method, String args, Region region, TypeBase requiredClass) {
        return requestRiotAccountEndpoint(method, args, region, requiredClass, new HashMap<>());
    }

    /**
     * Requests the Riot {@link RiotAPI#ACCOUNT_V1} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param region region to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestRiotAccountEndpoint(String method, String args, Region region, Class<T> requiredClass, Map<String, String> filter) {
        return requestRiotAccountEndpoint(method, args, region, TypeFactory.defaultInstance().constructType(requiredClass), filter);
    }

    /**
     * Requests the Riot {@link RiotAPI#ACCOUNT_V1} endpoint. If successful, the Response is mapped to the desired {@link JavaType} <b>{@code requiredClass}</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param region Region to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestRiotAccountEndpoint(String method, String args, Region region, JavaType requiredClass, Map<String, String> filter) {
        return handleCacheAndRateLimiter(
                constructUrl(ACCOUNT_V1 + method + args, AppType.RIOT, region),
                ACCOUNT_V1 + method, region, requiredClass, filter);
    }

    /**
     * Requests the TfT {@link RiotAPI#LEAGUE_V1} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestTftLeagueEndpoint(String method, String args, Platform platform, Class<T> requiredClass) {
        return requestTftLeagueEndpoint(method, args, platform, requiredClass, new HashMap<>());
    }

    /**
     * Requests the TfT {@link RiotAPI#LEAGUE_V1} endpoint. If successful, the Response is mapped to the desired {@link TypeBase}.
     * This method is intended to be used for {@link com.fasterxml.jackson.databind.type.CollectionType CollectionTypes} or
     * {@link com.fasterxml.jackson.databind.type.MapType MapTypes}.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestTftLeagueEndpoint(String method, String args, Platform platform, TypeBase requiredClass) {
        return requestTftLeagueEndpoint(method, args, platform, requiredClass, new HashMap<>());
    }

    /**
     * Requests the TfT {@link RiotAPI#LEAGUE_V1} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestTftLeagueEndpoint(String method, String args, Platform platform, Class<T> requiredClass, Map<String, String> filter) {
        return requestTftLeagueEndpoint(method, args, platform, TypeFactory.defaultInstance().constructType(requiredClass), filter);
    }

    /**
     * Requests the TfT {@link RiotAPI#LEAGUE_V1} endpoint. If successful, the Response is mapped to the desired {@link JavaType} <b>{@code requiredClass}</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param platform Platform to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestTftLeagueEndpoint(String method, String args, Platform platform, JavaType requiredClass, Map<String, String> filter) {
        return handleCacheAndRateLimiter(
                constructUrl(LEAGUE_V1 + method + args, AppType.TFT, platform),
                LEAGUE_V1 + method, Region.byPlatform(platform), requiredClass, filter);
    }

    /**
     * Requests the TfT {@link RiotAPI#MATCH_V1} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param region Region to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestTftMatchEndpoint(String method, String args, Region region, Class<T> requiredClass) {
        return requestTftMatchEndpoint(method, args, region, requiredClass, new HashMap<>());
    }

    /**
     * Requests the TfT {@link RiotAPI#MATCH_V1} endpoint. If successful, the Response is mapped to the desired {@link TypeBase}.
     * This method is intended to be used for {@link com.fasterxml.jackson.databind.type.CollectionType CollectionTypes} or
     * {@link com.fasterxml.jackson.databind.type.MapType MapTypes}.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param region Region to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestTftMatchEndpoint(String method, String args, Region region, TypeBase requiredClass) {
        return requestTftMatchEndpoint(method, args, region, requiredClass, new HashMap<>());
    }

    /**
     * Requests the TfT {@link RiotAPI#MATCH_V1} endpoint. If successful, the Response is mapped to the desired Class <b>T</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param region Region to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of class <b>T</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestTftMatchEndpoint(String method, String args, Region region, Class<T> requiredClass, Map<String, String> filter) {
        return requestTftMatchEndpoint(method, args, region, TypeFactory.defaultInstance().constructType(requiredClass), filter);
    }

    /**
     * Requests the TfT {@link RiotAPI#MATCH_V1} endpoint. If successful, the Response is mapped to the desired {@link JavaType} <b>{@code requiredClass}</b>.
     * If caching is enabled, the cached response will be returned.
     * @see Settings#useCache(boolean)
     * @see TypeFactory
     * @param method Method in the Endpoint that should get called
     * @param args Extra data needed for the Request
     * @param region Region to make the request to
     * @param requiredClass Class to which the response should get mapped to
     * @param filter The filter that should get used for the request. <b>Note:</b> The Values in the Map need to be Strings,
     *               even if they represent an integer
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    public static <T> T requestTftMatchEndpoint(String method, String args, Region region, JavaType requiredClass, Map<String, String> filter) {
        return handleCacheAndRateLimiter(
                constructUrl(MATCH_V1 + method + args, AppType.TFT, region),
                MATCH_V1 + method, region, requiredClass, filter);
    }

    /**
     * Utility Method that reads the cached response if caching is enable and that deals with the Rate Limiter (blocking) while making the API request
     * @param fullUrl The full url for the request
     * @param endpointMethod The Endpoint used in the url. Used for the Rate Limiter
     * @param region The Region to which the Request is being made. Used for the Rate Limiter
     * @param requiredClass The Class which the response is cast to
     * @param filter The Filter that gets included as GET parameter in the request
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    private static <T> T handleCacheAndRateLimiter(String fullUrl, String endpointMethod, Region region, JavaType requiredClass, Map<String, String> filter) {
        if(Settings.useCache()) {
            HttpResponse<String> cachedResponse = CACHE.getIfPresent(fullUrl);
            if (cachedResponse != null) return handleAndCastResponse(cachedResponse, requiredClass);
        } else CACHE.invalidateAll();
        try(IPermit ignored = rateLimiter.acquire(region, endpointMethod)) {
            HttpResponse<String> response = request(fullUrl, filter);
            if(response.statusCode() == HttpStatus.SC_OK) {
                if(Settings.useCache()) CACHE.put(fullUrl, response);
                rateLimiter.updateRateLimitsFromHeaders(region, endpointMethod, response.headers());
            }
            return handleAndCastResponse(response, requiredClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Utility Method to deal with the response of the API request and to cast the response into the desired Type.
     * If a {@link HttpStatus} other than {@link HttpStatus#SC_OK} is returned, either the exception gets thrown (if it exists),
     * or the Error gets logged. The Error gets
     * @param response The response returned by the request
     * @param requiredTyped The Type into which the response gets cast
     * @return An object of Type <b>{@code requiredTyped}</b> if casting is successful, {@code null} otherwise
     * @throws BadRequestException Gets thrown when {@link HttpStatus#SC_BAD_REQUEST} Status Code is returned
     * @throws UnauthorizedException Gets thrown when {@link HttpStatus#SC_UNAUTHORIZED} Status Code is returned
     * @throws ForbiddenException Gets thrown when {@link HttpStatus#SC_FORBIDDEN} Status Code is returned
     * @throws NotFoundException Gets thrown when {@link HttpStatus#SC_NOT_FOUND} Status Code is returned
     */
    private static <T> T handleAndCastResponse(HttpResponse<String> response, JavaType requiredTyped) throws BadRequestException, UnauthorizedException, ForbiddenException, NotFoundException {
        try {
            switch (response.statusCode()) {
                case HttpStatus.SC_OK -> {return Core.MAPPER.readValue(response.body(), requiredTyped);}
                case HttpStatus.SC_BAD_REQUEST -> throw new BadRequestException(response.uri().toString(), response.body());
                case HttpStatus.SC_UNAUTHORIZED -> throw new UnauthorizedException(response.uri().toString(), response.body());
                case HttpStatus.SC_FORBIDDEN -> throw new ForbiddenException(response.uri().toString(), response.body());
                case HttpStatus.SC_NOT_FOUND -> throw new NotFoundException(response.uri().toString(), response.body());
                default -> Core.LOGGER.error(MARKER, String.format("Got bad status code %d. Body: %s", response.statusCode(), response.body()));
            }
        } catch (IOException e) {
            String className = requiredTyped.hasContentType() ? requiredTyped.getContentType().getRawClass().getSimpleName() : requiredTyped.getRawClass().getSimpleName();
            Core.LOGGER.error(MARKER, String.format("Failed to parse JSON to %s object", className), e);
        }
        return null;
    }

    /**
     * Utility Method to construct the full Url for a given Endpoint, {@link AppType} and {@link Region}
     * @see Constants#API_BASE_PATH
     * @param endPoint The Endpoint
     * @param app The AppType
     * @param region The Region
     * @return The full Url
     */
    private static String constructUrl(String endPoint, AppType app, Region region) {
        return (Constants.API_BASE_PATH + app + "/").replaceAll("#", region.toString()) + endPoint;
    }

    /**
     * Utility Method to construct the full Url for a given Endpoint, {@link AppType} and {@link Platform}
     * @see Constants#API_BASE_PATH
     * @param endPoint The Endpoint
     * @param app The AppType
     * @param platform The Platform
     * @return The full Url
     */
    private static String constructUrl(String endPoint, AppType app, Platform platform) {
        return (Constants.API_BASE_PATH + app + "/").replaceAll("#", platform.toString()) + endPoint;
    }

    /**
     * Enum that represents the possible App Types in the Riot API
     */
    private enum AppType {
        RIOT("riot"),
        LOL("lol"),
        TFT("tft");

        private final String name;

        AppType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
