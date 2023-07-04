package net.petersil98.core.http.exceptions;

/**
 * Exception thrown when a {@link org.apache.http.HttpStatus#SC_FORBIDDEN} Status Code is returned
 */
public class ForbiddenException extends RuntimeException {

    private static final String TEMPLATE = "Got status code 403 (Forbidden) for URL %s. Body: %s. Did the API Key expired or is it invalid?";

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String url, String body) {
        this(String.format(TEMPLATE, url, body));
    }
}
