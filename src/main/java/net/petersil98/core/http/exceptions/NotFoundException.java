package net.petersil98.core.http.exceptions;

/**
 * Exception thrown when a {@link org.apache.http.HttpStatus#SC_NOT_FOUND} Status Code is returned
 */
public class NotFoundException extends RuntimeException {

    private static final String TEMPLATE = "Got status code 404 (Not found) for URL %s. Body: %s. Probably because there is no resource with the given identifier.";

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String url, String body) {
        this(String.format(TEMPLATE, url, body));
    }
}
