package controller.rules;

import controller.Rule;
import controller.RuleType;
import model.Repository;
import model.RepositoryAspectEval;

/**
 * Checks the quality metric of the repository. For example, GitHub stars.
 */
public class QualityMetric extends Rule {

    private static int[] limits;

    public QualityMetric(Repository repository, int[] limits) {
        super(RuleType.QUALITY, repository);
        if (QualityMetric.limits == null) {
            QualityMetric.limits = limits;
        }
    }

    @Override
    public RepositoryAspectEval execute() {
        int metric = repository.getQualityMetrics();

        return new RepositoryAspectEval(calculatePointsWithLimits(limits, metric));

    }
}
