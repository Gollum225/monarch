package controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import model.RepoList;
import model.Repository;
import repository_information.GitHub.GithubCommunication;
import util.CSVHandler;

import java.io.IOException;

import java.util.InputMismatchException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static util.Globals.DEFAULT_NUMBER_OF_STAR;


/**
 * Manages the list of repositories and provides the next repository to check.
 * Keeps track of processed repositories. Gets new repositories, if the amount of unprocessed is under {@link #THRESHOLD}.
 */
public class RepoListManager {

    public RepoListManager(RuleCollection ruleCollection) {
        csvHandler = new CSVHandler("result.csv");
        csvHandler.createResultSchema(ruleCollection);
        csvHandler.createCsv();
    }

    public RepoListManager(RuleCollection ruleCollection, String searchTerm) {
        this(ruleCollection);
        this.searchTerm = searchTerm;
    }

    public RepoListManager(RuleCollection ruleCollection, String searchTerm, int starAmount) {
        this(ruleCollection, searchTerm);
        this.starAmount = starAmount;
    }

    public RepoListManager(RuleCollection ruleCollection, int starAmount) {
        this(ruleCollection);
        this.starAmount = starAmount;
    }

    /**
     * Handles the writing of the results to a CSV file.
     */
    private final CSVHandler csvHandler;

    /**
     * List of repositories this class handles.
     */
    private final RepoList repoList = RepoList.getInstace();

    /**
     * Starts getting new repositories, when the number of unprocessed repositories is below this threshold.
     */
    private final int THRESHOLD = 5;

    /**
     * Number of unprocessed, ready repositories.
     */
    private int unprocessedRepos = 0;



    private String searchTerm;



    private int starAmount = -1;

    /**
     * Gets the next repository to check.
     * @return next repository
     * @throws TimeoutException if no new repositories are available after 10 seconds
     */
    public synchronized Repository getNextRepo() {
        checkRepoAmount();


        // If no new repositories are available, wait for 10 seconds.
        // If still no new repositories are available, throw a TimeoutException
        for (int i = 0; i < 10; i++) {
            if (repoList.size() == 0) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    continue; // test again if the list is already filled
                }
            } else {
                break;
            }
            return null;
        }
        unprocessedRepos--;

        return repoList.getNext();
    }

    /**
     * Gets new repositories from the GitHub API.
     *
     */
    private void getNewRepos() {

        List<Repository> repos;
        try {
            if (searchTerm != null && starAmount >= 0) {
                repos = GithubCommunication.getInstance().getTenRepository(searchTerm, starAmount);
            } else if (searchTerm != null) {
                repos = GithubCommunication.getInstance().getTenRepository(searchTerm, DEFAULT_NUMBER_OF_STAR);
            } else if (starAmount >= 0) {
                repos = GithubCommunication.getInstance().getTenRepository(null, starAmount);
            } else {
                repos = GithubCommunication.getInstance().getTenRepository();
            }
             if (repos == null) {
                 checkRepoAmount();
                 return;
             } else if (repos.isEmpty()) {
                 return;
             }
        } catch (JsonProcessingException e) {
            throw new InputMismatchException("Error while getting new repositories.");
        }

        for (Repository repo : repos) {
            if (repoList.addRepo(repo)) {
                unprocessedRepos++;
            }
        }
    }

    /**
     * Writes the result of the search to files.
     *
     * @param repo repository to be finished
     */
    public void finishRepo(Repository repo, RuleCollection ruleCollection) {
        repo.finish();
        System.out.println("\u001B[32m" + "repo: " + repo.getRepositoryName() + " of " + repo.getOwner() + " got: " + repo.getOverallPoints() + " points" + "\u001B[0m");
        try {
            csvHandler.writeResult(repo, ruleCollection);
        } catch (IOException e) {
            System.out.println("\u001B[31m" + "Error while writing to CSV file. Please note: " + repo.getIdentifier() + " got: " + repo.getOverallPoints() + "\u001B[0m");
        }
    }

    /**
     * Checks if the amount of unprocessed repositories is below the threshold.
     * Triggers the refill if necessary
     */
    private void checkRepoAmount() {

        // In loop to get repositories, to get other repositories if repositories were found again.
        for (int i = 0; i < 2; i++) {
            if (repoList.size() < THRESHOLD){
                //getTestRepos();
                getNewRepos();
            }
        }
    }

    private void getTestRepos() {
        repoList.addRepo(new Repository("TeaStore", "DescartesResearch"));
        repoList.addRepo(new Repository("teammates", "TEAMMATES"));
        repoList.addRepo(new Repository("javaPS", "52North"));
        repoList.addRepo(new Repository("arctic-sea", "52North"));
        repoList.addRepo(new Repository("bigbluebutton", "bigbluebutton"));
        unprocessedRepos = 5;
    }

    public int getMaxResults() throws JsonProcessingException {
        if (searchTerm != null && starAmount >= 0) {
            return GithubCommunication.getInstance().getMaxResults(searchTerm, starAmount);
        } else if (searchTerm != null) {
            return GithubCommunication.getInstance().getMaxResults(searchTerm, -1);
        } else if (starAmount >= 0) {
            return GithubCommunication.getInstance().getMaxResults(null, starAmount);
        }
        return 1000;
    }

}
