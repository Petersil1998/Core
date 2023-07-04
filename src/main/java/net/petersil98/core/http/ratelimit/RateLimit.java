package net.petersil98.core.http.ratelimit;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Rate Limit for either the API Key or a Method of an Endpoint. Both are counted separately in each Region
 */
class RateLimit {
    private final int rateLimit;
    private final int rateLimitIntervalInSeconds;
    private final List<IPermit> permits;

    /**
     * Constructor
     * @param rateLimit The maximum Amount of request allowed in a given interval
     * @param rateLimitInterval The interval
     */
    public RateLimit(int rateLimit, int rateLimitInterval) {
        this.rateLimit = rateLimit;
        this.rateLimitIntervalInSeconds = rateLimitInterval;
        this.permits = new ArrayList<>(rateLimit);
    }

    /**
     * Method to acquire a Permit. This Method is <b>blocking</b>
     * @return A Permit
     */
    public IPermit acquire() {
        while (!isPermitAvailable()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Permit p = new Permit(this);
        permits.add(p);
        return p;
    }

    /**
     * Checks, whether a Permit is available. This Method is Thread-safe
     * @return Whether a Permit is available
     */
    public boolean isPermitAvailable() {
        synchronized (this.permits) {
            return this.permits.size() < this.rateLimit;
        }
    }

    /**
     * An Implementation of the {@link IPermit} interface
     */
    public static class Permit implements IPermit {

        private final RateLimit rateLimit;
        private boolean canceled = false;
        private boolean closed = false;
        private long closedTimestamp = -1;

        /**
         * Constructor
         * @param rateLimit Reference to the Rate Limit in which this Permit was given and is tracked
         */
        public Permit(RateLimit rateLimit) {
            this.rateLimit = rateLimit;
        }

        /**
         * Removes this permit from the pool of given Permits in {@link RateLimit#permits}
         */
        @Override
        public void remove() {
            this.rateLimit.permits.remove(this);
        }

        /**
         * Marks this Permit is being used
         */
        @Override
        public void close() {
            this.closed = true;
            this.closedTimestamp = System.currentTimeMillis();
        }

        /**
         * Checks whether this Permit is invalid
         * @return Whether this Permit is invalid
         */
        @Override
        public boolean isInvalid() {
            return this.canceled ||
                    (this.closed && System.currentTimeMillis() >= closedTimestamp + rateLimit.rateLimitIntervalInSeconds * 1000L);
        }

        /**
         * Cancels the Permit
         */
        @Override
        public void cancel() {
            this.canceled = true;
        }
    }
}