package repository_information;

import com.fasterxml.jackson.databind.JsonNode;
import exceptions.APIOverloaded;
import exceptions.CloneProhibitedException;
import repository_information.GitHub.GithubCommunication;
import repository_information.GitHub.GithubRateLimitCheck;
import repository_information.GitHub.RateResource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.Globals.MAX_FILE_AMOUNT;

public class APIProxy {


    public APIProxy(String repositoryName, String owner) {
        this.repositoryName = repositoryName;
        this.owner = owner;
    }
    final String repositoryName;
    final String owner;

    final GitMandatories gitAPI = GithubCommunication.getInstance();
    final RateLimitMandatories rateLimitMandatories = GithubRateLimitCheck.getInstance();



    public JsonNode getStructure() throws CloneProhibitedException, APIOverloaded {
        if (checkForClone()) {
            // If checkForClone returns true, the rate limit is reached and the repository has been cloned.
            // The new proxy is a CloneProxy, so we can just call getStructure on it.
            throw new APIOverloaded("rate limit reached");
        }

        return gitAPI.getStructure(owner, repositoryName);

    }

    public Map<String, String> getFiles(List<String> paths) throws CloneProhibitedException, APIOverloaded {
        if (checkForClone()) {
            throw new APIOverloaded();
        }
        if (paths.size() > MAX_FILE_AMOUNT) {
            throw new APIOverloaded("too many files requested: " + paths.size());
        }
        Map<String, String> results = new HashMap<>();

        for (String path : paths) {
            results.put(path, getSingleFile(path));
        }
        return results;
    }



    public String[] getOwnersRepos() {
        if (rateLimitMandatories.checkHardRateLimit(RateResource.GRAPHQL)) {
            return gitAPI.getOwnersRepositories(owner);
        } else {
            return new String[0];
        }
    }

    private String getSingleFile(String path) {
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
     * Checks if the rate limit is reached, and if so, changes to the clone.
     * If the repository couldn't be cloned, it may wait for the rate limit to reset.
     *
     * @return if the repository has been cloned.
     */
    private boolean checkForClone() throws APIOverloaded {
        if (checkRateLimit(RateResource.CORE, false)) {
            return false;
        }
        throw new APIOverloaded("rate limit reached");

    }
    public JsonNode generalInfo() {
        return gitAPI.generalInfo(owner, repositoryName);
    }

}
