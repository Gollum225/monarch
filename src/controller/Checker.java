package controller;

import model.Repository;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Applies the rules from the {@link controller.rules} package to the {@link model.Repository} from {@link model.RepoList}.
 */
public class Checker {

    private RuleCollection rules;
    private final RepoListManager listManager = new RepoListManager();

    private Checker() {
        rules = new RuleCollection();
    }


    /**
     * Apply the list of rules to the given amount of repositories.
     *
     * @param amount of repositories to be checked
     * @throws TimeoutException, if no new repositories are available after 10 seconds
     */
    public void checkRepos(int amount) throws TimeoutException {
        AtomicInteger unprocessedRepos = new AtomicInteger(amount);
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);

        for (int i = 0; i < amount; i++) {
            completionService.submit(() -> {
                Repository currentRepo = listManager.getNextRepo();

                ArrayList<RuleMandatories> equippedRules = rules.equipRules(currentRepo);
                for (RuleMandatories rule : equippedRules) {
                    currentRepo.saveResult(rule.getClass(), rule.execute());
                }

                listManager.finishRepo(currentRepo);

                return null;
            });
        }

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

}
