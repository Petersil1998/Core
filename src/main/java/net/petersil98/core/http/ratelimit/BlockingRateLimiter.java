package net.petersil98.core.http.ratelimit;

import net.petersil98.core.constant.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * Concrete Implementation of the Abstract {@link RateLimit} class
 */
public class BlockingRateLimiter extends RateLimiter {

    private final List<AggregatePermit> permits = new CopyOnWriteArrayList<>();

    private final Map<Region, List<Thread>> threadsWaitingForAppPermit = new ConcurrentHashMap<>();
    private final DoubleKeyMap<Region, String, List<Thread>> threadsWaitingForMethodPermit = new DoubleKeyMap<>();
    private final Map<Region, List<Thread>> threadsWaitingForAppExceeded = new ConcurrentHashMap<>();
    private final DoubleKeyMap<Region, String, List<Thread>> threadsWaitingForMethodExceeded = new DoubleKeyMap<>();

    /**
     * Constructor for this class. It creates a Thread, which checks periodically whether permits in {@link #permits} are invalid and can be released.
     * It also checks if exceeded Rate Limits are now safe to be removed.
     */
    public BlockingRateLimiter() {
        Thread permitReleaser = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    releaseAppPermits();
                    releaseMethodPermits();
                    releaseAggregatePermits();
                    checkExceededAppRateLimits();
                    checkExceededMethodRateLimits();
                    try {
                        Thread.sleep(getTimeToWait());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            private void releaseAppPermits() {
                synchronized (appRateLimits) {
                    appRateLimits.forEach((region, rateLimits) -> rateLimits
                            .forEach(rateLimit -> List.copyOf(rateLimit.getPermits())
                                    .stream()
                                    .filter(permit -> permit != null && permit.isInvalid())
                                    .forEach(permit -> {
                                        permit.remove();
                                        if (threadsWaitingForAppPermit.containsKey(region)
                                                && !threadsWaitingForAppPermit.get(region).isEmpty()) {
                                            Thread t = threadsWaitingForAppPermit.get(region).remove(0);
                                            synchronized (t) {
                                                t.notify();
                                            }
                                        }
                                    })
                            )
                    );
                }
            }

            private void releaseMethodPermits() {
                synchronized (methodRateLimits) {
                    methodRateLimits.forEach((region, method, rateLimits) -> rateLimits
                            .forEach(rateLimit -> List.copyOf(rateLimit.getPermits())
                                    .stream()
                                    .filter(permit -> permit != null && permit.isInvalid())
                                    .forEach(permit -> {
                                        permit.remove();
                                        if (threadsWaitingForMethodPermit.containsKey(region, method)
                                                && !threadsWaitingForMethodPermit.get(region, method).isEmpty()) {
                                            Thread t = threadsWaitingForMethodPermit.get(region, method).remove(0);
                                            synchronized (t) {
                                                t.notify();
                                            }
                                        }
                                    })
                            )
                    );
                }
            }

            private void releaseAggregatePermits() {
                List<AggregatePermit> copy = List.copyOf(permits);
                for (AggregatePermit aggregatePermit : copy) {
                    if (aggregatePermit.isInvalid()) {
                        aggregatePermit.remove();
                    }
                }
            }

            private void checkExceededAppRateLimits() {
                synchronized (exceededAppRateLimits) {
                    exceededAppRateLimits.entrySet().removeIf(entry -> {
                        boolean shouldRemove = !entry.getValue().isStillExceeded();
                        if(shouldRemove) {
                            Thread t = threadsWaitingForAppExceeded.get(entry.getKey()).remove(0);
                            synchronized (t) {t.notify();}
                        }
                        return shouldRemove;
                    });
                }
            }

            private void checkExceededMethodRateLimits() {
                synchronized (exceededMethodRateLimits) {
                    exceededMethodRateLimits.entrySet().removeIf(entry -> {
                        boolean shouldRemove = !entry.getValue().isStillExceeded();
                        if(shouldRemove) {
                            Thread t = threadsWaitingForMethodExceeded.get(entry.getKey1(), entry.getKey2()).remove(0);
                            synchronized (t) {t.notify();}
                        }
                        return shouldRemove;
                    });
                }
            }

            private long getTimeToWait() {
                long timeToWaitForPermit;
                synchronized (permits) {
                    timeToWaitForPermit = permits.stream().mapToLong(aggregatePermit -> {
                        synchronized (aggregatePermit) {
                            return aggregatePermit.permits.stream()
                                    .filter(permit -> !permit.isInvalid())
                                    .mapToLong(RateLimit.Permit::getRemainingLifespan)
                                    .min().orElse(Long.MAX_VALUE);
                        }
                    }).min().orElse(500);
                }
                long timeToWaitForExceedRateLimit;
                synchronized (exceededMethodRateLimits) {
                    synchronized (exceededAppRateLimits) {
                        timeToWaitForExceedRateLimit = Stream.concat(
                                        exceededMethodRateLimits.entrySet().stream().map(DoubleKeyMap.Entry::getValue),
                                        exceededAppRateLimits.values().stream())
                                .filter(ExceededRateLimit::isStillExceeded)
                                .mapToLong(ExceededRateLimit::getRemainingTime)
                                .min().orElse(500);
                    }
                }
                return Math.min(timeToWaitForExceedRateLimit, timeToWaitForPermit);
            }
        });
        permitReleaser.start();
    }

    /**
     * Method to acquire a Permit, which is needed in order to make a request.
     * If the request is the first in a given region or the first in a given region for a given method, then a {@link DummyPermit} is returned.
     * Otherwise, this Method "parks" the current Thread in a List where it waits for a Permit to become available for this Region and Method.
     * This Method is <b>blocking</b> and <b>Thread-safe</b>
     * @param region The region to which the request should be made
     * @param endpointMethod The Method of a given Endpoint to which the request should be made
     * @return A Permit once the Request can be made safely
     */
    @Override
    public IPermit acquire(Region region, String endpointMethod) {
        while (true) {
            if(this.exceededAppRateLimits.containsKey(region)) {
                parkCurrentThread(threadsWaitingForAppExceeded, region);
            } else if(this.exceededMethodRateLimits.containsKey(region, endpointMethod)) {
                parkCurrentThread(threadsWaitingForMethodExceeded, region, endpointMethod);
            } else {
                if (!this.appRateLimits.containsKey(region)) return new DummyPermit();
                synchronized (this.appRateLimits.get(region)) {
                    if (!this.methodRateLimits.containsKey(region, endpointMethod)) return new DummyPermit();
                    synchronized (this.methodRateLimits.get(region, endpointMethod)) {
                        List<RateLimit> appLimits = this.appRateLimits.get(region);
                        List<RateLimit> methodLimits = this.methodRateLimits.get(region, endpointMethod);
                        if (appLimits.stream().anyMatch(rateLimit -> !rateLimit.isPermitAvailable())) {
                            parkCurrentThread(threadsWaitingForAppPermit, region);
                        } else if (methodLimits.stream().anyMatch(rateLimit -> !rateLimit.isPermitAvailable())) {
                            parkCurrentThread(threadsWaitingForMethodPermit, region, endpointMethod);
                        } else {
                            AggregatePermit permit = new AggregatePermit(Stream.concat(appLimits.stream(), methodLimits.stream()).map(RateLimit::acquire).toList(), this);
                            permits.add(permit);
                            return permit;
                        }
                    }
                }
            }
        }
    }

    /**
     * Utility Method to "park" the current Thread and make it wait until new Permits are available
     * @see Thread#wait()
     * @param map The map to "park" the current Thread
     * @param region The Region to which the Thread needs a Permit
     */
    private void parkCurrentThread(Map<Region, List<Thread>> map, Region region) {
        Thread currentThread = Thread.currentThread();
        if(!map.containsKey(region)) map.put(region, new ArrayList<>());
        map.get(region).add(currentThread);
        try {
            synchronized (currentThread) {
                currentThread.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Utility Method to "park" the current Thread and make it wait until new Permits are available
     * @see Thread#wait()
     * @param map The map to "park" the current Thread
     * @param region The Region to which the Thread needs a Permit
     * @param method The endpoint Method
     */
    private void parkCurrentThread(DoubleKeyMap<Region, String, List<Thread>> map, Region region, String method) {
        Thread currentThread = Thread.currentThread();
        if(!map.containsKey(region, method)) map.put(region, method, List.of());
        map.get(region, method).add(currentThread);
        try {
            synchronized (currentThread) {
                currentThread.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * An Implementation of the {@link IPermit} interface. This is a Wrapper Class for a collection of Permits used when multiple Permits are needed
     * (e.g. A Permit is needed for both the Region per API Key and the Method in the Region)
     */
    private static class AggregatePermit implements IPermit {
        private final Collection<RateLimit.Permit> permits;
        private final BlockingRateLimiter rateLimiter;

        /**
         * Constructor
         * @param permits A List of Permits aggregated by this object
         * @param rateLimiter Reference to the Blocking Rate Limiter in which this Permit was given and is tracked
         */
        private AggregatePermit(Collection<RateLimit.Permit> permits, BlockingRateLimiter rateLimiter) {
            this.permits = permits;
            this.rateLimiter = rateLimiter;
        }

        /**
         * Cancels all the associated Permits of this Aggregate
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
