package controller;

import model.RepoList;
import model.Repository;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Applies the rules from the {@link controller.rules} package to the {@link Repository} from {@link RepoList}.
 */
public class Checker {

    /**
     * The rules that will be applied to the repositories.
     */
    private RuleCollection rules;

    /**
     * Manages repositories.
     */
    private final RepoListManager listManager;

    /**
     * Creates a new Checker with the default rules.
     */
    public Checker() {
        rules = new RuleCollection();
        listManager = new RepoListManager(rules);
    }


    /**
     * Apply the list of rules to the given amount of repositories.
     *
     * @param amount of repositories to be checked
     */
    public void checkRepos(int amount) {
        AtomicInteger unprocessedRepos = new AtomicInteger(amount);
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<Void> completionService = getVoidCompletionService(amount, executor);

        try {
            for (int i = 0; i < amount; i++) {
                completionService.take().get();
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Error while processing repos", e);
            //TODO: handle exception
        } finally {
            executor.shutdown();
        }

    }

    /**
     * Creates a new CompletionService for the given amount of repositories.
     *
     * @param amount of repositories to be checked
     * @param executor to execute the tasks
     * @return the CompletionService
     */
    private CompletionService<Void> getVoidCompletionService(int amount, ExecutorService executor) {
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);

        for (int i = 0; i < amount; i++) {
            completionService.submit(() -> {
                Repository currentRepo = listManager.getNextRepo();

                ArrayList<Rule> equippedRules = rules.equipRules(currentRepo);
                for (Rule rule : equippedRules) {
                    if (rule.getType() == RuleType.MANDATORY) {
                        currentRepo.saveResult(rule.getClass(), rule.execute());
                    }
                }
                for (Rule rule : equippedRules) {
                    if (currentRepo.getOverallPoints() > 0) {
                        if (rule.getType() == RuleType.QUALITY) {
                            currentRepo.saveResult(rule.getClass(), rule.execute());
                        }
                    } else if (rule.getType() == RuleType.QUALITY) {
                        currentRepo.saveResult(rule.getClass(), new RuleReturn("Did not get mandatory points", currentRepo.getIdentifier(), rule.getClass().getSimpleName()));
                    }
                }

                listManager.finishRepo(currentRepo, rules);

                return null;
            });
        }
        return completionService;
    }

}
