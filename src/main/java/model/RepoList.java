package model;

import java.util.LinkedHashSet;

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
    private LinkedHashSet<Repository> unprocessedRepos = new LinkedHashSet<>();
    private LinkedHashSet<Repository> startedRepos = new LinkedHashSet<>();

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
    public static RepoList getInstace() {
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
        startedRepos.add(currentRepo);
        return currentRepo;
    }

    /**
     * Getter for the amount of unprocessed {@link Repository}.
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
    public boolean addRepo(Repository[] repos) {
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
    public boolean addRepo(Repository repo) {
        System.out.println("Added repo: " + repo.getRepositoryName() + " by " + repo.getOwner());
        return unprocessedRepos.add(repo);
    }
}
