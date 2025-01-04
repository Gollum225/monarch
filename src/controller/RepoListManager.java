package controller;

import model.RepoList;
import model.Repository;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Manages the list of repositories and provides the next repository to check.
 * Keeps track of processed repositories. Gets new repositories, if the amount of unprocessed is under {@link #THRESHOLD}.
 */
public class RepoListManager {

    private final RepoList repoList = RepoList.getInstace();

    /**
     * Starts getting new repositories, when the amount of unprocessed repositories is below this threshold.
     */
    private final int THRESHOLD = 10;

    /**
     * Amount of unprocessed, ready repositories.
     */
    private int unprocessedRepos = 0;


    public void saveResult(Repository repo) {
        // save the result
    }

    public Repository getNextRepo() throws TimeoutException {
        unprocessedRepos--;
        if (unprocessedRepos < THRESHOLD) {
            getNewRepos();
        }

        // if no new repositories are available, wait for 10 seconds.
        // If still no new repositories are available, throw a TimeoutException
        for (int i = 0; i < 10; i++) {
            if (repoList.size() == 0) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    continue; // test again, if list is already filled
                }
            } else {
                break;
            }
            throw new TimeoutException("No new repositories available.");
        }
        return repoList.getNext();
    }

    private void getNewRepos() {
        //TODO: get new repositories
    }

    /**
     * Writes the result of the search to files.
     * @param repo
     */
    public void finishRepo(Repository repo) {
     //TODO
    }
}
