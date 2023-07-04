package net.petersil98.core.http.exceptions;

/**
 * Exception thrown when a {@link org.apache.http.HttpStatus#SC_BAD_REQUEST} Status Code is returned
 */
public class BadRequestException extends RuntimeException {

    private static final String TEMPLATE = "Got status code 400 (Bad Request) for URL %s. Body: %s";

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String url, String body) {
        this(String.format(TEMPLATE, url, body));
    }
}
