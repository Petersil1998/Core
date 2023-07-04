package net.petersil98.core.http.ratelimit;

/**
 * Interface for a Permit
 */
public interface IPermit extends AutoCloseable {

    /**
     * Cancels the Permit and makes it invalid
     */
    void cancel();

    /**
     * Removes itself from the list of used Permits, such that new Permits can be given out
     */
    void remove();

    /**
     * Checks whether this Permit is invalid
     * @return Whether this Permit is invalid
     */
    boolean isInvalid();
}
