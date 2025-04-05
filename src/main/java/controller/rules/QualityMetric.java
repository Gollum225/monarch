package controller.rules;

import controller.Rule;
import controller.RuleType;
import model.Repository;
import model.RepositoryAspectEval;

/**
 * Checks the quality metric of the repository. E.g., GitHub stars.
 */
public class QualityMetric extends Rule {

    public QualityMetric(Repository repository) {
        super(RuleType.QUALITY, repository);
    }

    @Override
    public RepositoryAspectEval execute() {
        int metric = repository.getQualityMetrics();

        if (metric < 100) {
            return new RepositoryAspectEval(0);
        } else if (metric < 5000) {
            return new RepositoryAspectEval(1);
        } else if (metric < 10000) {
            return new RepositoryAspectEval(2);
        } else if (metric < 25000) {
            return new RepositoryAspectEval(3);
        } else if (metric < 100000) {
            return new RepositoryAspectEval(4);
        } else {
            return new RepositoryAspectEval(5);
        }
    }
}
