package controller.rules;

import controller.Rule;
import controller.RuleType;
import model.Repository;
import model.RepositoryAspectEval;

import java.util.List;

public class SearchOwnerRepo extends Rule {

    private static int[] limits;

    public SearchOwnerRepo(Repository repository, int[] limits) {
        super(RuleType.MANDATORY, repository);
        if (SearchOwnerRepo.limits == null) {
            if (limits.length != 3) {
                throw new IllegalArgumentException("Limits of SearchOwnerRepo must be of length 3");
            }
            SearchOwnerRepo.limits = limits;
        }
    }

    @Override
    public RepositoryAspectEval execute() {
        String[] repoNames = repository.getOwnersRepos();

        int maxScore = 0;
        for (String name : repoNames) {
            if (countMultipleKeywordsSingleOccurrence(name, List.of(new String[]{"documentations", "documentation", "doc", "docs"})) > 0) {
                if (name.toLowerCase().contains(repository.getRepositoryName().toLowerCase())) {
                    maxScore = limits[2];
                } else if (repository.getRepositoryName().substring(0, 3).equalsIgnoreCase(name.substring(0, 3))) {
                    maxScore = limits[1];
                } else {
                    maxScore = limits[0];
                }
            }
        }

        return new RepositoryAspectEval(maxScore);
    }
}
