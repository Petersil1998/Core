package net.petersil98.core.http.ratelimit;

import net.petersil98.core.constant.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Concrete Implementation of the Abstract {@link RateLimit} class
 */
public class BlockingRateLimiter extends RateLimiter {

    private final List<AggregatePermit> permits = new ArrayList<>();

    /**
     * Constructor for this class. It creates a Thread, which checks periodically whether permits in {@link #permits} are invalid, and if so, removes them
     */
    public BlockingRateLimiter() {
        Thread thread = new Thread(() -> {
            while (true) {
                synchronized (this.permits) {
                    List<AggregatePermit> copy = List.copyOf(this.permits);
                    for (AggregatePermit aggregatePermit: copy) {
                        aggregatePermit.permits.stream().filter(IPermit::isInvalid).forEach(IPermit::remove);
                        if (aggregatePermit.isInvalid()) {
                            aggregatePermit.remove();
                        }
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    /**
     * Method to acquire a Permit, which is needed in order to make a request.
     * If the request is the first in a given region or the first in a given region for a given method, then a {@link DummyPermit} is returned.
     * Otherwise, this Method constantly checks if there is a Permit available for this Region and Method, and if so, a Permit is granted.
     * This Method is <b>blocking</b> and <b>Thread-safe</b>
     * @param region The region to which the request should be made
     * @param endpointMethod The Method of a given Endpoint to which the request should be made
     * @return A Permit once the Request can be made safely
     */
    @Override
    public IPermit acquire(Region region, String endpointMethod) {
        while (true) {
            if(!this.appLimitsPerRegion.containsKey(region)) return new DummyPermit();
            synchronized (this.appLimitsPerRegion.get(region)) {
                List<RateLimit> appLimits = this.appLimitsPerRegion.get(region);
                if(!this.methodRateLimitsPerRegion.has(region, endpointMethod)) return new DummyPermit();
                synchronized (this.methodRateLimitsPerRegion.get(region, endpointMethod)) {
                    List<RateLimit> methodLimits = this.methodRateLimitsPerRegion.get(region, endpointMethod);
                    if(appLimits.stream().allMatch(RateLimit::isPermitAvailable) && methodLimits.stream().allMatch(RateLimit::isPermitAvailable)) {
                        AggregatePermit permit = new AggregatePermit(Stream.concat(appLimits.stream(), methodLimits.stream()).map(RateLimit::acquire).toList(), this);
                        permits.add(permit);
                        return permit;
                    }
                }
            }
        }
    }

    /**
     * An Implementation of the {@link IPermit} interface. This is a Wrapper Class for a collection of Permits used when multiple Permits are needed
     * (e.g. A Permit is needed for both the Region per API Key and the Method in the Region)
     */
    private static class AggregatePermit implements IPermit {
        private final Collection<IPermit> permits;
        private final BlockingRateLimiter rateLimiter;

        /**
         * Constructor
         * @param permits A List of Permits aggregated by this object
         * @param rateLimiter Reference to the Blocking Rate Limiter in which this Permit was given and is tracked
         */
        private AggregatePermit(Collection<IPermit> permits, BlockingRateLimiter rateLimiter) {
            this.permits = permits;
            this.rateLimiter = rateLimiter;
        }

        /**
         * Cancels all thr associated Permits of this Aggregate
         */
        @Override
        public void cancel() {
            this.permits.forEach(IPermit::cancel);
        }

        /**
         * Removes this permit from the pool of given Permits in {@link BlockingRateLimiter#permits}
         */
        @Override
        public void remove() {
            rateLimiter.permits.remove(this);
        }

        /**
         * Checks whether this Permit is invalid. Its invalid if <i>all</i> the associated Permits are invalid
         * @return Whether this Permit is invalid
         */
        @Override
        public boolean isInvalid() {
            return this.permits.stream().allMatch(IPermit::isInvalid);
        }

        /**
         * Marks this Permit and all associated Permits as being used
         */
        @Override
        public void close() {
            this.permits.forEach(permit -> {
                try {
                    permit.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Dummy Implementation of the {@link IPermit} interface
     */
    private static class DummyPermit implements IPermit {

        @Override
        public void cancel() {}

        @Override
        public void remove() {}

        @Override
        public boolean isInvalid() {
            return true;
        }

        @Override
        public void close() {}
    }
}
