package repository_information;

import repository_information.GitHub.RateResource;

import java.util.Date;

/**
 * To abstract the mandatory functions, a class handling the rate limits of a git repository should have.
 * This interface is based on the assumption that the git system has different types of rate limits, which are defined in {@link repository_information.GitHub.RateResource}.
 */
public interface RateLimitMandatories {

    void setRateLimit(RateResource resource, int maxRequests, int requestsLeft, Date resetTime);

    /**
     * Checks if the rate limit comes to an end.
     *
     * @return true, if further requests can be sent.
     */
    boolean checkRateLimit();

    /**
     * Checks if the rate limit becomes scarce for a specific resource.
     *
     * @param resource the resource to check the rate limit for.
     * @return true, if further requests can be sent safely.
     */
    boolean checkMildRateLimit(RateResource resource);

    /**
     * Checks if the rate limit comes to an end for a specific resource.
     *
     * @param resource the resource to check the rate limit for.
     * @return true, if further requests can be sent.
     */
    boolean checkHardRateLimit(RateResource resource);

    /**
     * Returns the time till the rate limit resets for a specific resource in milliseconds.
     *
     * @param rateResource the resource the reset time is requested.
     * @return number of milliseconds since January 1, 1970, 00:00:00 GMT till the rate limit resets
     */
    long getTimeTillReset(RateResource rateResource);
}
