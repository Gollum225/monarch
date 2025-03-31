package controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import model.RepositoryAspectEval;
import model.RepoList;
import model.Repository;
import org.eclipse.jgit.util.FileUtils;
import util.Json;
import view.Status;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static util.Globals.CLONED_REPOS_PATH;

/**
 * Applies the rules from the {@link controller.rules} package to the {@link Repository} from {@link RepoList}.
 */
public class Checker {

    /**
     * The rules that will be applied to the repositories.
     */
    private final RuleCollection rules;

    /**
     * Manages repositories.
     */
    private final RepoListManager listManager;

    private final int ruleAmount;

    private final Status status;

    /**
     * Creates a new Checker with the default rules.
     */
    public Checker() {
        rules = new RuleCollection();
        listManager = new RepoListManager(rules);
        ruleAmount = rules.getRuleAmount();
        status = new Status(ruleAmount);

    }

    public Checker(String searchTerm) {
        rules = new RuleCollection();
        listManager = new RepoListManager(rules, searchTerm);
        ruleAmount = rules.getRuleAmount();
        status = new Status(ruleAmount);
    }

    public Checker(String searchTerm, int starAmount) {
        rules = new RuleCollection();
        listManager = new RepoListManager(rules, searchTerm, starAmount);
        ruleAmount = rules.getRuleAmount();
        status = new Status(ruleAmount);
    }

    public Checker(int starAmount) {
        rules = new RuleCollection();
        listManager = new RepoListManager(rules, starAmount);
        ruleAmount = rules.getRuleAmount();
        status = new Status(ruleAmount);
    }



    /**
     * Apply the list of rules to the given number of repositories.
     *
     * @param amount of repositories to be checked
     */
    public void checkRepos(int amount) {
        start();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        int maxResults;
        try {
            maxResults = listManager.getMaxResults();
            if (maxResults >=1000) {
                System.out.println("Found at least 1000 repositories matching the search.");
            } else {
                System.out.println("Found " + maxResults + " repositories");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot get number of available repositories.");
        }

        if (maxResults < amount) {
            amount = maxResults;
            System.out.println("Can't find enough repositories with given search. Analyzing " + amount + " repositories.");
        }


        CompletionService<Void> repoProcessingService = submitRepoAnalyzation(amount, executor);

        for (int i = 0; i < amount; i++) {
            try {
                repoProcessingService.take().get();
            } catch (ExecutionException e) {
                System.err.println("Error processing a repository: " + e.getCause().getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Error processing a repository: " + e.getCause().getMessage());
            }
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Executor did not terminate in the specified time.");
                List<Runnable> droppedTasks = executor.shutdownNow();
                System.err.println("Executor was abruptly shut down. " + droppedTasks.size() + " tasks will not be executed.");
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for executor to terminate: " + e.getMessage());
            executor.shutdownNow();
        }

        finish();

    }

    /**
     * Creates a new CompletionService for the given amount of repositories.
     *
     * @param amount of tasks of repositories to be checked
     * @param executor to execute the tasks
     * @return the CompletionService
     */
    private CompletionService<Void> submitRepoAnalyzation(int amount, ExecutorService executor) {
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);

        for (int i = 0; i < amount; i++) {
            completionService.submit(() -> {

                Repository currentRepo;
                    currentRepo = listManager.getNextRepo();

                if (currentRepo == null) {
                    return null;
                }

                ArrayList<Rule> equippedRules = rules.equipRules(currentRepo);
                status.addStatusBar(currentRepo);
                for (Rule rule : equippedRules) {
                    if (rule.getType() == RuleType.MANDATORY) {
                        status.updateStatusBar(currentRepo, rule.getClass().getSimpleName());

                        currentRepo.saveResult(rule.getClass(), rule.execute());
                    }
                }
                for (Rule rule : equippedRules) {
                    if (currentRepo.getOverallPoints() > 0) {
                        if (rule.getType() == RuleType.QUALITY) {
                            status.updateStatusBar(currentRepo, rule.getClass().getSimpleName());
                            currentRepo.saveResult(rule.getClass(), rule.execute());
                        }
                    } else if (rule.getType() == RuleType.QUALITY) {
                        currentRepo.saveResult(rule.getClass(), new RepositoryAspectEval("Did not get mandatory points", currentRepo.getIdentifier(), rule.getClass().getSimpleName()));
                    }
                }

                listManager.finishRepo(currentRepo, rules);

                status.removeStatusBar(currentRepo);

                return null;
            });
        }
        return completionService;
    }

    private void start() {
        try {
            Json.setup();
        } catch (IOException e) {
            // No need to handle this exception, as the program cannot continue without the keywords.
            throw new RuntimeException("Cant proceed, Error with setting up Json " + e);
        }
    }

    private void finish() {
        status.finish();
        deleteClonedRepos();
    }

    /**
     * Deletes the cloned repositories.
     * {@link repository_information.CloneProxy#finish()} should delete the repositories right after finishing the repo evaluation,
     * but in some cases the deletion fails due to the operating system.
     */
    private void deleteClonedRepos() {
        boolean failure = false;
        File folder = new File(String.valueOf(CLONED_REPOS_PATH));

        File[] files = folder.listFiles();
        if (files == null) {
            failure = true;
        } else {
            for (File file : files) {

                if (file.isDirectory()) {
                    try {
                        FileUtils.delete(file, FileUtils.RECURSIVE);
                    } catch (IOException e) {
                        failure = true;
                    }
                } else {
                    if (!file.delete()) {
                        failure = true;
                    }
                }
            }
        }
        if (failure) {
            System.out.println("\u001B[33m" + "Couldnt delete all repos. Please delete path manually if necessary: " + CLONED_REPOS_PATH + "\u001B[0m");
        }

    }
}
