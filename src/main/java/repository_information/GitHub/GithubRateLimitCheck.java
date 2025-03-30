package repository_information.GitHub;

import repository_information.RateLimitMandatories;

import java.util.*;

import static util.Globals.RATE_LIMIT_THRESHOLD;

public class GithubRateLimitCheck implements RateLimitMandatories {

    private static GithubRateLimitCheck rateLimitCheck;

    private Map<RateResource, RateLimit> rateLimits;

    private GithubRateLimitCheck() {
        rateLimits = new HashMap<>();
        for (RateResource resource : RateResource.values()) {
            rateLimits.put(resource, new RateLimit(resource));
        }

    }

    /**
     * Singleton pattern to get the rate limit check.
     *
     * @return the rate limit check.
     */
    public static GithubRateLimitCheck getInstance() {
        if (rateLimitCheck == null) {
            rateLimitCheck = new GithubRateLimitCheck();

        }
        return rateLimitCheck;
    }

    @Override
    public synchronized void setRateLimit(RateResource resource, int maxRequests, int requestsLeft, Date resetTime) {
        RateLimit rateLimit = rateLimits.get(resource);
        rateLimit.setMaxRequests(maxRequests);
        rateLimit.setRequestsLeft(requestsLeft);
        rateLimit.setResetTime(resetTime);
    }

    @Override
    public boolean checkRateLimit() {
        for (RateResource rateLimit : rateLimits.keySet()) {
            if (!checkMildRateLimit(rateLimit)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean checkMildRateLimit(RateResource resource) {
        RateLimit rateLimit = rateLimits.get(resource);
        if (rateLimit.getRequestsLeft() >= (rateLimit.getMaxRequests()) * RATE_LIMIT_THRESHOLD) {
            return true;
        }
        System.out.println("Rate limit for " + resource + " at " + RATE_LIMIT_THRESHOLD * 100 + "%. Trying to switch to cloning.");
        System.out.println("Rate limit resets at " + rateLimit.getResetTime());
        return false;
    }

    @Override
    public boolean checkHardRateLimit(RateResource resource) {
        return rateLimits.get(resource).getMaxRequests() > 0;
    }

    @Override
    public long getTimeTillReset(RateResource rateResource) {
        RateLimit rateLimit = rateLimits.get(rateResource);
        if (rateLimit.getResetTime() != null) {
            return rateLimit.getResetTime().getTime() - new Date().getTime();
        }
        return Long.MAX_VALUE;
    }


}
