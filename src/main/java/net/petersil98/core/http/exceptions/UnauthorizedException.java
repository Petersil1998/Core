package net.petersil98.core.http.exceptions;

/**
 * Exception thrown when a {@link org.apache.http.HttpStatus#SC_UNAUTHORIZED} Status Code is returned
 */
public class UnauthorizedException extends RuntimeException {

    private static final String TEMPLATE = "Got status code 401 (Unauthorized) for URL %s. Body: %s. Was no API Key provided?";

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String url, String body) {
        this(String.format(TEMPLATE, url, body));
    }
}
