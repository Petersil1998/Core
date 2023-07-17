package net.petersil98.core.http.exceptions;

/**
 * Exception thrown when a {@link org.apache.http.HttpStatus#SC_BAD_REQUEST} Status Code is returned
 */
public class RateLimitExceededException extends RuntimeException {

    private static final String TEMPLATE = "Got status code 429 (Rate Limit Exceeded) for URL %s. Body: %s";

    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String url, String body) {
        this(String.format(TEMPLATE, url, body));
    }
}
