package controller.rules;

import controller.Rule;
import controller.RuleType;
import model.Repository;
import model.RepositoryAspectEval;

import java.util.List;

public class searchOwnerRepo extends Rule {

    public searchOwnerRepo(Repository repository) {
        super(RuleType.MANDATORY, repository);
    }

    @Override
    public RepositoryAspectEval execute() {
        String[] repoNames = repository.getOwnersRepos();

        int maxScore = 0;
        for (String name : repoNames) {
            if (countMultipleKeywordsSingleOccurrence(name, List.of(new String[]{"documentations", "documentation", "doc", "docs"})) > 0) {
                if (name.toLowerCase().contains(repository.getRepositoryName().toLowerCase())) {
                    maxScore = 23;
                } else if (repository.getRepositoryName().substring(0, 3).equalsIgnoreCase(name.substring(0, 3))) {
                    maxScore = 18;
                } else {
                    maxScore = 12;
                }
            }
        }

        return new RepositoryAspectEval(maxScore);
    }
}
