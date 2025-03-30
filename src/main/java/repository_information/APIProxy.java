package repository_information;

import com.fasterxml.jackson.databind.JsonNode;
import exceptions.CloneProhibitedException;
import repository_information.GitHub.RateResource;

import java.util.List;
import java.util.Map;

import static util.Globals.CLONE_THRESHOLD;
import static util.Globals.MAX_FILE_AMOUNT;

public class APIProxy extends AbstractProxy{


    public APIProxy(String repositoryName, String owner, RepoCache cache) {
        super(repositoryName, owner, cache);
    }

    @Override
    public JsonNode getStructure() throws CloneProhibitedException {
        if (checkForClone()) {
            // If checkForClone returns true, the rate limit is reached and the repository has been cloned.
            // The new proxy is a CloneProxy, so we can just call getStructure on it.
            return cache.getStructure();
        }

        JsonNode structure = gitAPI.getStructure(owner, repositoryName);
        if (structure == null) {
            changeToClone("couldn't get structure");
            return cache.getStructure();
        } else if (structure != null && structure.size() > CLONE_THRESHOLD) {
            // If the structure is too large: try to clone, but don't throw an exception, because it is not the
            // callers fault.
            try {
                changeToClone("large structure: " + structure.size() + " elements");
            } catch (CloneProhibitedException e) {
                return structure;
            }
        }
        return structure;
    }

    @Override
    public Map<String, String> getFiles(List<String> paths) throws CloneProhibitedException {
        if (checkForClone()) {
            return cache.getFiles(paths);
        }
        if (paths.size() > MAX_FILE_AMOUNT) {
            changeToClone("too many files requested: " + paths.size());
        }
        return super.getFiles(paths);
    }

    @Override
    public void finish() {
        //Nothing to do here
    }

    @Override
    String getSingleFile(String path) {
        return gitAPI.getFile(path, owner, repositoryName);
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
    private boolean checkForClone() throws CloneProhibitedException {
        if (checkRateLimit(RateResource.CORE, false)) {
            return false;
        }

        //try to clone
        if (changeToClone("rate limit near")) {
            return true;
        } else {
            // couldn't clone, try for the hard rate limit or wait for the rate limit to reset.
            while (!checkRateLimit(RateResource.CORE, true)) {
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
