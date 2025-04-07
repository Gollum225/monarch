package controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import model.RepositoryAspectEval;
import model.RepoList;
import model.Repository;
import org.eclipse.jgit.util.FileUtils;
import util.CLIOutput;
import util.JsonKeywords;
import view.Status;

import java.io.File;
import java.io.IOException;
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

    /**
     * Number of rules the repositories are checked with.
     */
    private final int numberOfRules;

    private final Status status;

    /**
     * Creates a new Checker with the default rules.
     */
    public Checker() {
        rules = new RuleCollection();
        listManager = new RepoListManager(rules);
        numberOfRules = rules.getNumberOfRules();
        status = new Status(numberOfRules);

    }

    public Checker(String searchTerm) {
        rules = new RuleCollection();
        listManager = new RepoListManager(rules, searchTerm);
        numberOfRules = rules.getNumberOfRules();
        status = new Status(numberOfRules);
    }

    public Checker(String searchTerm, int numberOfStars) {
        rules = new RuleCollection();
        listManager = new RepoListManager(rules, searchTerm, numberOfStars);
        numberOfRules = rules.getNumberOfRules();
        status = new Status(numberOfRules);
    }




    /**
     * Apply the list of rules to the given number of repositories.
     *
     * @param number of repositories to be checked
     */
    public void checkRepos(int number) {
        JsonKeywords.checkJson();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        int maxResults;
        try {
            maxResults = listManager.getMaxResults();
            if (maxResults >=1000) {
                CLIOutput.found("At least 1000", "repositories", "matching the search");
            } else {
                CLIOutput.found(String.valueOf(maxResults), "repositories", "matching the search");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot get number of available repositories.");
        }

        if (maxResults < number) {
            number = maxResults;
            CLIOutput.info("Can't find enough repositories with given search. Analyzing " + number + " repositories.");
        }


        CompletionService<Void> repoProcessingService = submitRepoAnalysation(number, executor);

        for (int i = 0; i < number; i++) {
            try {
                repoProcessingService.take().get();
            } catch (ExecutionException e) {
                CLIOutput.repositoryProcessingError(e.getCause().getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                CLIOutput.repositoryProcessingError(e.getCause().getMessage());
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
     * Creates a new CompletionService for the given number of repositories.
     *
     * @param number of tasks of repositories to be checked
     * @param executor to execute the tasks
     * @return the CompletionService
     */
    private CompletionService<Void> submitRepoAnalysation(int number, ExecutorService executor) {
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);

        for (int i = 0; i < number; i++) {
            completionService.submit(() -> {

                Repository currentRepo;
                    currentRepo = listManager.getNextRepo();

                if (currentRepo == null) {
                    return null;
                }

                List<Rule> equippedRules = rules.equipRules(currentRepo);
                status.addStatusBar(currentRepo);
                for (Rule rule : equippedRules) {
                    if (rule.getType() == RuleType.MANDATORY) {
                        status.updateStatusBar(currentRepo, rule.getClass().getSimpleName());

                        currentRepo.saveResult(rule, rule.execute());
                    }
                }
                for (Rule rule : equippedRules) {

                    // Have to check for each rule to save the 0-point result in the else-if.
                    if (currentRepo.getOverallPoints() > 0) {
                        if (rule.getType() == RuleType.QUALITY) {
                            status.updateStatusBar(currentRepo, rule.getClass().getSimpleName());
                            currentRepo.saveResult(rule, rule.execute());
                        }
                    } else if (rule.getType() == RuleType.QUALITY) {
                        currentRepo.saveResult(rule, new RepositoryAspectEval("Did not get mandatory points"));
                    }
                }

                listManager.finishRepo(currentRepo, rules);

                status.removeStatusBar(currentRepo);

                return null;
            });
        }
        return completionService;
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
            // Delete recursive, to partly delete folders, if OS prevents deletion due to open files.
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
            CLIOutput.warning("Couldn't delete all repos. Please delete path manually if necessary: " + CLONED_REPOS_PATH);
        }

    }
}
