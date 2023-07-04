package net.petersil98.core.util;

/**
 * Custom Exception, that gets thrown when an invalid value for the API Filter are passed
 */
public class InvalidFilterException extends IllegalArgumentException {

    public InvalidFilterException() {
        super();
    }

    public InvalidFilterException(String message) {
        super(message);
    }

    public InvalidFilterException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFilterException(Throwable cause) {
        super(cause);
    }
}
