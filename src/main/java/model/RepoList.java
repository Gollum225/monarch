package model;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Holds multiple {@link Repository}.
 */
public class RepoList {

    /**
     * The instance of the {@link RepoList} for the Singleton pattern.
     */
    private static RepoList instance;

    /**
     * List of {@link Repository}.
     * Set up as a Queue.
     */
    private final LinkedHashSet<Repository> unprocessedRepos = new LinkedHashSet<>();
    private final Set<String> startedRepos = new HashSet<>();

    /**
     * Private constructor for the Singleton pattern.
     */
    private RepoList() {
    }


    /**
     * Singleton pattern.
     *
     * @return The instance of the {@link RepoList}.
     */
    public static RepoList getInstance() {
        if (instance == null) {
            instance = new RepoList();
        }
        return instance;
    }

    /**
     * Gets the next {@link Repository} from the list.
     *
     * @return next unprocessed {@link Repository}
     */
    public synchronized Repository getNext() {
        if (unprocessedRepos.isEmpty()) {
            return null;
        }
        Repository currentRepo = unprocessedRepos.removeFirst();
        startedRepos.add(currentRepo.getIdentifier());
        return currentRepo;
    }

    /**
     * Getter for the number of unprocessed {@link Repository}.
     *
     * @return unprocessed {@link Repository}
     */
    public int size() {
        return unprocessedRepos.size();
    }

    /**
     * Adds multiple {@link Repository} to the list.
     * @param repos to be added
     * @return true, if all repos are new and could be added
     */
    public boolean addMultipleRepos(Repository[] repos) {
        boolean everythingNew = true;
        for (Repository repo : repos) {
            everythingNew = this.unprocessedRepos.add(repo);
        }
        return everythingNew;
    }

    /**
     * Adds a single {@link Repository} to the list.
     * @param repo to be added
     * @return true, if the repo is new and could be added
     */
    public boolean addSingleRepo(Repository repo) {
        if (startedRepos.contains(repo.getIdentifier())) {
            return false;
        }
        return unprocessedRepos.add(repo);
    }
}
