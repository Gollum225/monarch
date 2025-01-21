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

    private final int REFILL_AMOUNT = 10;

    /**
     * Amount of unprocessed, ready repositories.
     */
    private int unprocessedRepos = 0;



    public synchronized Repository getNextRepo() throws TimeoutException {
        checkRepoAmount();


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
        unprocessedRepos--;

        return repoList.getNext();
    }

    private void getNewRepos(int amount) {

        List<Repository> repos;
        try {
             repos = GithubCommunication.getRepository(amount);
        } catch (JsonProcessingException e) {
            throw new InputMismatchException("Error while getting new repositories.");
        }

        for (Repository repo : repos) {
            if (repoList.addRepo(repo)) {
                unprocessedRepos++;
            }
        }

        checkRepoAmount();
    }

    /**
     * Writes the result of the search to files.
     * @param repo
     */
    public void finishRepo(Repository repo) {
        repo.finish();
        System.out.println("\u001B[32m" + "repo: " + repo.getRepositoryName() + " of " + repo.getOwner() + " got: " + repo.getOverallPoints() + " points" + "\u001B[0m");
     //TODO
    }

    /**
     * Checks if the amount of unprocessed repositories is below the threshold.
     * Triggers the refill if necessary
     */
    private void checkRepoAmount() {
        if (repoList.size() < THRESHOLD) {
            getNewRepos(REFILL_AMOUNT);
        }
    }
}
