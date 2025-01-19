package repository_information;

import controller.RepoCache;
import repository_information.GitHub.GithubCommunication;
import repository_information.GitHub.GithubRateLimitCheck;

import java.nio.file.Path;

import static util.Globals.CLONED_REPOS_PATH;

/**
 * Strategy pattern. This causes CloneProxy to automatically call either the method for the API or for the local file.
 */
public abstract class AbstractProxy implements RepoFunctions{
    final String repositoryName;
    final String owner;
    final RepoCache cache;
    final RateLimitMandatories rateLimitMandatories = GithubRateLimitCheck.getInstance();

    final Path repoPath;

    public AbstractProxy(String repositoryName, String owner, RepoCache cache) {
        this.repositoryName = repositoryName;
        this.owner = owner;
        this.cache = cache;
        repoPath = CLONED_REPOS_PATH.resolve(owner).resolve(repositoryName);

    }

    private boolean cloneRepo() {
        return GithubCommunication.cloneRepo(owner, repositoryName, repoPath);
    }

    boolean changeToClone() {
        System.out.println("\u001B[34m" + "Changing to clone: " + repositoryName + " of owner: " + owner + "\u001B[0m");

        return change();
    }

    public boolean changeToClone(String reason) {
        System.out.println("\u001B[34m" + "Changing to clone: " + repositoryName + " of owner: " + owner + " because of " + reason + "\u001B[0m");
        return change();
    }
    private boolean change() {
        if (cloneRepo()) {
            cache.setProxy(new CloneProxy(repositoryName, owner, cache));
            return true;
        }
        return false;
    }

    public abstract void finish();
}
