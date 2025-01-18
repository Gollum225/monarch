package repository_information;

import controller.RepoCache;
import repository_information.GitHub.GithubCommunication;
import repository_information.GitHub.GithubRateLimitCheck;

/**
 * Strategy pattern. This causes CloneProxy to automatically call either the method for the API or for the local file.
 */
public abstract class AbstractProxy implements RepoFunctions{
    final String repositoryName;
    final String owner;
    final RepoCache cache;
    final RateLimitMandatories rateLimitMandatories = GithubRateLimitCheck.getInstance();

    public AbstractProxy(String repositoryName, String owner, RepoCache cache) {
        this.repositoryName = repositoryName;
        this.owner = owner;
        this.cache = cache;
    }

    private boolean cloneRepo() {
        return GithubCommunication.cloneRepo(owner, repositoryName);
    }

    boolean changeToClone() {
        System.out.println("\u001B[34m" + "Changing to clone: " + repositoryName + " of owner: " + owner + "\u001B[0m");
        if (cloneRepo()) {
            cache.setProxy(new CloneProxy(repositoryName, owner, cache));
            return true;
        }
        return false;
    }

    public abstract void finish();
}
