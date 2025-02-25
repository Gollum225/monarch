package repository_information;

import com.fasterxml.jackson.databind.JsonNode;
import exceptions.CloneProhibitedException;
import repository_information.GitHub.GithubCommunication;
import repository_information.GitHub.GithubRateLimitCheck;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.Globals.CLONED_REPOS_PATH;
import static util.Globals.MAX_CLONE_SIZE;

/**
 * Strategy pattern. This causes CloneProxy to automatically call either the method for the API or for the local file.
 */
public abstract class AbstractProxy implements RepoFunctions{
    final String repositoryName;
    final String owner;
    final RepoCache cache;
    final RateLimitMandatories rateLimitMandatories = GithubRateLimitCheck.getInstance();

    /**
     * If the repository is too large to clone. The limit is set in {@link util.Globals#MAX_CLONE_SIZE}.
     */
    boolean cloneProhibited = false;

    final Path repoPath;

    public AbstractProxy(String repositoryName, String owner, RepoCache cache) {
        this.repositoryName = repositoryName;
        this.owner = owner;
        this.cache = cache;
        repoPath = CLONED_REPOS_PATH.resolve(owner).resolve(repositoryName);

    }

    private boolean cloneRepo() throws CloneProhibitedException {
        if (cloneProhibited) {
            System.out.println("\u001B[31m" + "Couldn't clone " + repositoryName + "of: " + owner + "\u001B[0m");
            throw new CloneProhibitedException();
        } else if (getRepoSize() > MAX_CLONE_SIZE) {
            cloneProhibited = true;
            System.out.println("\u001B[31m" + "Couldn't clone " + repositoryName + "of: " + owner + "\u001B[0m");
            throw new CloneProhibitedException();
        }
        return GithubCommunication.cloneRepo(owner, repositoryName, repoPath);
    }

    boolean changeToClone() throws CloneProhibitedException {
        System.out.println("\u001B[34m" + "Trying to clone: " + repositoryName + " of owner: " + owner + "\u001B[0m");

        return change();
    }

    public boolean changeToClone(String reason) throws CloneProhibitedException {
        System.out.println("\u001B[34m" + "Trying to clone: " + repositoryName + " of owner: " + owner + " because of " + reason + "\u001B[0m");
        return change();
    }
    private boolean change() throws CloneProhibitedException {
        if (cloneRepo()) {
            cache.setProxy(new CloneProxy(repositoryName, owner, cache));
            return true;
        }
        return false;
    }

    public int getRepoSize() {
        return cache.generalInfo().get("size").asInt();
    }

    @Override
    public JsonNode generalInfo() {
        return GithubCommunication.generalInfo(owner, repositoryName);
    }

    public abstract void finish();

    abstract String getSingleFile(String path);

    @Override
    public Map<String, String> getFiles(List<String> paths)  throws CloneProhibitedException{
        int size = paths.size();
        Map<String, String> results = new HashMap<>();

        for (String path : paths) {
            results.put(path, getSingleFile(path));
        }
        return results;

    }
}
