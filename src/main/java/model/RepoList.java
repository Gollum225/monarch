package model;

import java.util.LinkedHashSet;

/**
 * Holds multiple {@link Repository}.
 */
public class RepoList {

    private static RepoList instance;

    /**
     * List of {@link Repository}.
     * Set up as a Queue.
     */
    private LinkedHashSet<Repository> repos = new LinkedHashSet<>();

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

    public Repository getNext() {
        if (repos.isEmpty()) {
            return null;
        }
        return repos.iterator().next();
    }

    public int size() {
        return repos.size();
    }

    /**
     * Adds multiple {@link Repository} to the list.
     * @param repos to be added
     * @return true, if all repos are new and could be added
     */
    public boolean addRepo(Repository[] repos) {
        boolean everythingNew = true;
        for (Repository repo : repos) {
            everythingNew = this.repos.add(repo);
        }
        return everythingNew;
    }
}
