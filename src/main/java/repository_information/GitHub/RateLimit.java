package repository_information.GitHub;

import java.util.Date;

public class RateLimit {

    /**
     * Number of requests per hour.
     */
    private int maxRequests = 1;

    /**
     * Number of requests left.
     */
    private int requestsLeft = 1;

    /**
     * Time when the rate limit resets.
     */
    private Date resetTime;

    /**
     * RateLimit type of the object.
     */
    private final RateResource resourceType;

    /**
     * Creates a new RateLimit object.
     *
     * @param resourceType type of the rate limit
     */
    public RateLimit(RateResource resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * Getter for maximum requests per hour.
     * Will not decrease when used.
     * .
     * @return number of requests per hour
     */
    public int getMaxRequests() {
        return maxRequests;
    }

    /**
     * Setter for maximum requests per hour.
     *
     * @param maxRequests number of requests per hour
     */
    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    /**
     * Getter for requests left.
     * Represents the current state of the API regarding that limit.
     *
     * @return number of requests left
     */
    public int getRequestsLeft() {
        return requestsLeft;
    }

    /**
     * Setter for requests left, till the time resets.
     *
     * @param requestsLeft number of requests left
     */
    public void setRequestsLeft(int requestsLeft) {
        this.requestsLeft = requestsLeft;
    }

    /**
     * Getter for the time when the rate limit resets.
     *
     * @return time when the rate limit resets
     */
    public Date getResetTime() {
        return resetTime;
    }

    /**
     * Setter for the time when the rate limit resets.
     *
     * @param resetTime time when the rate limit resets
     */
    public void setResetTime(Date resetTime) {
        this.resetTime = resetTime;
    }

    /**
     * Getter for the type of the rate limit.
     * Can also be internally stored at the place where the rate limit is set and used.
     *
     * @return type of the rate limit
     */
    public RateResource getResourceType() {
        return resourceType;
    }
}