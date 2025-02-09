package controller.rules;

import controller.Rule;
import controller.RuleType;
import model.Repository;

/**
 * Checks the quality metric of the repository. e.g. GitHub stars.
 */
public class QualityMetric extends Rule {

    public QualityMetric(Repository repository) {
        super(RuleType.QUALITY, repository);
    }

    @Override
    public RuleReturn execute() {
        int metric = repository.getQualityMetrics();

        if (metric < 100) {
            return new RuleReturn(0);
        } else if (metric < 5000) {
            return new RuleReturn(1);
        } else if (metric < 10000) {
            return new RuleReturn(2);
        } else if (metric < 25000) {
            return new RuleReturn(3);
        } else if (metric < 100000) {
            return new RuleReturn(4);
        } else {
            return new RuleReturn(5);
        }
    }
}
