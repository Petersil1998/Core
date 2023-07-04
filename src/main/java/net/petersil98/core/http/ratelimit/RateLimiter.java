package net.petersil98.core.http.ratelimit;

import net.petersil98.core.constant.Region;

import java.net.http.HttpHeaders;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class that represents a Rate Limiter
 */
public abstract class RateLimiter {

    protected final Map<Region, List<RateLimit>> appLimitsPerRegion = new HashMap<>();
    protected final DoubleKeyMap<Region, String, List<RateLimit>> methodRateLimitsPerRegion = new DoubleKeyMap<>();

    /**
     * Method to acquire a Permit, which is needed in order to make a request. This Method is intended to be used in a <b>blocking</b> context
     * @param region The region to which the request should be made
     * @param endpointMethod The Method of a given Endpoint to which the request should be made
     * @return A Permit once the Request can be made safely
     */
    public abstract IPermit acquire(Region region, String endpointMethod);

    /**
     * Method to update the internal Rate Limits used to give out Permits. The Rate Limits are updated based on the
     * headers <i>x-app-rate-limit</i> and <i>x-method-rate-limit</i>.
     * @param region The region to which the request has been made
     * @param endpointMethod The Method of a given Endpoint to which the request has been made
     * @param headers The Headers containing the fields <i>x-app-rate-limit</i> and <i>x-method-rate-limit</i>.
     */
    public void updateRateLimitsFromHeaders(Region region, String endpointMethod, HttpHeaders headers) {
        if (!this.appLimitsPerRegion.containsKey(region)) {
            this.appLimitsPerRegion.put(region, parseRateLimits("x-app-rate-limit", headers));
            this.appLimitsPerRegion.get(region).forEach(rateLimit -> {
                try {
                    rateLimit.acquire().close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        if (!this.methodRateLimitsPerRegion.has(region, endpointMethod)) {
            this.methodRateLimitsPerRegion.put(region, endpointMethod, parseRateLimits("x-method-rate-limit", headers));
            this.methodRateLimitsPerRegion.get(region, endpointMethod).forEach(rateLimit -> {
                try {
                    rateLimit.acquire().close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Utility method to parse the Rate Limits as provided in the {@link HttpHeaders} into a List of Rate Limits
     * @param headerName The name of the header which contains the Rate Limit information
     * @param headers The HTTP headers
     * @return List of Rate Limits if the parsing was successful, {@code null} otherwise
     */
    private List<RateLimit> parseRateLimits(String headerName, HttpHeaders headers) {
        return headers.firstValue(headerName).map(header ->
                Arrays.stream(header.split(",")).map(limit -> {
                    String[] split = limit.split(":");
                    return new RateLimit(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                }).toList()).orElse(null);
    }
}
