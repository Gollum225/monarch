package repository_information;

import com.fasterxml.jackson.databind.JsonNode;
import controller.RepoCache;
import repository_information.GitHub.GithubCommunication;
import repository_information.GitHub.RateResource;

public class APIProxy extends AbstractProxy{


    public APIProxy(String repositoryName, String owner, RepoCache cache) {
        super(repositoryName, owner, cache);
    }

    @Override
    public JsonNode getStructure() {
        if (checkForClone()) {
            // If checkForClone returns true, the rate limit is reached and the repository has been cloned.
            // The new proxy is a CloneProxy, so we can just call getStructure on it.
            return cache.getStructure();
        }

        JsonNode structure = GithubCommunication.getStructure(owner, repositoryName);
        if (structure == null || (structure != null && structure.size() > 1500)) {
            changeToClone();
            return cache.getStructure();
        }
        return structure;
    }

    @Override
    public String getFile(String path, String url) {
        if (checkForClone()) {
            return cache.getFile(path, url);
        }
        String fileContent = GithubCommunication.getFile(url);

        if (fileContent == null) {
            changeToClone();
            return cache.getFile(path, url);
        }
        return GithubCommunication.getFile(url);
    }

    @Override
    public void finish() {
        //Nothing to do here
    }

    private boolean checkRateLimit() {
        return rateLimitMandatories.checkRateLimit();
    }

    /**
     * Checks the rate limit, if the call is connected to a specific resource.
     *
     * @param resource the resource to check the rate limit for.
     * @param critical if the request has a higher priority to be sent per API.
     * @return if the request can be made per API.
     */
    private boolean checkRateLimit(RateResource resource, boolean critical) {
        if (rateLimitMandatories.checkMildRateLimit(resource)) {
            return true;
        } else if (critical) {
            return rateLimitMandatories.checkHardRateLimit(resource);
        }
        return false;
    }

    /**
     * Checks if the rate limit is reached and if so, changes to the clone.
     * If the repository couldn't be cloned, it may wait for the rate limit to reset.
     *
     * @return if the repository has been cloned.
     */
    private boolean checkForClone() {
        if (checkRateLimit(RateResource.GRAPHQL, false)) {
            return false;
        }

        //try to clone
        if (changeToClone()) {
            return true;
        } else {
            // couldn't clone, try for the hard rate limit or wait for the rate limit to reset.
            while (!checkRateLimit(RateResource.GRAPHQL, true)) {
                try {
                    Thread.sleep(rateLimitMandatories.getTimeTillReset(RateResource.GRAPHQL));
                } catch (InterruptedException e) {
                    //Do nothing, wait for the next loop.
                }
            }
        }
        return false;
    }
}
