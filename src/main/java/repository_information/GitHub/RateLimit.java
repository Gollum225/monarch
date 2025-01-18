package repository_information.GitHub;

import java.util.Date;

public class RateLimit {

    /**
     * Amount of requests per hour.
     */
    private int maxRequests;

    /**
     * Amount of requests left.
     */
    private int requestsLeft;

    /**
     * Time when the rate limit resets.
     */
    private Date resetTime;

    private RateResource resourceType;

    public RateLimit(RateResource resourceType) {
        this.resourceType = resourceType;
    }


    public int getMaxRequests() {
        return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public int getRequestsLeft() {
        return requestsLeft;
    }

    public void setRequestsLeft(int requestsLeft) {
        this.requestsLeft = requestsLeft;
    }

    public Date getResetTime() {
        return resetTime;
    }

    public void setResetTime(Date resetTime) {
        this.resetTime = resetTime;
    }
}