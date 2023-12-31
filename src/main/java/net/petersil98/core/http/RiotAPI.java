package net.petersil98.core.http;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.petersil98.core.Core;
import net.petersil98.core.constant.Platform;
import net.petersil98.core.constant.Region;
import net.petersil98.core.http.exceptions.*;
import net.petersil98.core.http.ratelimit.BlockingRateLimiter;
import net.petersil98.core.http.ratelimit.IPermit;
import net.petersil98.core.http.ratelimit.RateLimiter;
import net.petersil98.core.util.Util;
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

    private static final String API_BASE_PATH = "https://#.api.riotgames.com/";

    private static final String ACCOUNT_V1 = "account/v1/";

    protected static RateLimiter rateLimiter = new BlockingRateLimiter();

    /**
     * Utility Method that delegates the request to the {@link HTTPClient}
     * @param url The full Url
     * @return The response
     */
    private static HttpResponse<String> request(String url) {
        return HTTPClient.getInstance().get(url);
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
     * Utility Method that reads the cached response if caching is enable and that deals with the Rate Limiter (blocking) while making the API request
     * @param url The full url for the request
     * @param endpointMethod The Endpoint used in the url. Used for the Rate Limiter
     * @param region The Region to which the Request is being made. Used for the Rate Limiter
     * @param requiredClass The Class which the response is cast to
     * @param filter The Filter that gets included as GET parameter in the request
     * @return An object of Type <b>{@code requiredClass}</b> if casting is successful, {@code null} otherwise
     */
    protected static <T> T handleCacheAndRateLimiter(String url, String endpointMethod, Region region, JavaType requiredClass, Map<String, String> filter) {
        String urlWithGetParams = url + "?" + Util.buildParameters(filter);
        if(Settings.useCache()) {
            HttpResponse<String> cachedResponse = CACHE.getIfPresent(urlWithGetParams);
            if (cachedResponse != null) return handleAndCastResponse(cachedResponse, requiredClass);
        } else CACHE.invalidateAll();
        try(IPermit ignored = rateLimiter.acquire(region, endpointMethod)) {
            HttpResponse<String> response = request(urlWithGetParams);
            if(response.statusCode() == HttpStatus.SC_OK) {
                if(Settings.useCache()) CACHE.put(urlWithGetParams, response);
                rateLimiter.updateRateLimitsFromHeaders(region, endpointMethod, response.headers());
            } else if(response.statusCode() == 429) {
                Core.LOGGER.warn("Rate Limit has been exceeded for endpoint " + endpointMethod + "!");
                rateLimiter.handleRateLimitExceeded(region, endpointMethod, response.headers());
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
    protected static <T> T handleAndCastResponse(HttpResponse<String> response, JavaType requiredTyped) throws BadRequestException, UnauthorizedException, ForbiddenException, NotFoundException {
        try {
            switch (response.statusCode()) {
                case HttpStatus.SC_OK -> {return Core.MAPPER.readValue(response.body(), requiredTyped);}
                case HttpStatus.SC_BAD_REQUEST -> throw new BadRequestException(response.uri().toString(), response.body());
                case HttpStatus.SC_UNAUTHORIZED -> throw new UnauthorizedException(response.uri().toString(), response.body());
                case HttpStatus.SC_FORBIDDEN -> throw new ForbiddenException(response.uri().toString(), response.body());
                case HttpStatus.SC_NOT_FOUND -> throw new NotFoundException(response.uri().toString(), response.body());
                case 429 -> throw new RateLimitExceededException(response.uri().toString(), response.body());
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
     * @see #API_BASE_PATH
     * @param endPoint The Endpoint
     * @param app The AppType
     * @param region The Region
     * @return The full Url
     */
    protected static String constructUrl(String endPoint, AppType app, Region region) {
        return (API_BASE_PATH + app + "/").replaceAll("#", region.toString()) + endPoint;
    }

    /**
     * Utility Method to construct the full Url for a given Endpoint, {@link AppType} and {@link Platform}
     * @see #API_BASE_PATH
     * @param endPoint The Endpoint
     * @param app The AppType
     * @param platform The Platform
     * @return The full Url
     */
    protected static String constructUrl(String endPoint, AppType app, Platform platform) {
        return (API_BASE_PATH + app + "/").replaceAll("#", platform.toString()) + endPoint;
    }

    /**
     * Enum that represents the possible App Types in the Riot API
     */
    protected enum AppType {
        RIOT("riot"),
        LOL("lol"),
        TFT("tft"),
        LOR("lor"),
        VAL("val");

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
