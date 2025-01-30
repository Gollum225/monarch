package controller;

import controller.rules.RuleReturn;
import model.Repository;

public abstract class Rule {

    protected final RuleType type;
    protected final Repository repository;

    protected Rule(RuleType type, Repository repository) {
        this.type = type;
        this.repository = repository;
    }


    /**
     * Executes the rule.
     *
     * @return an object representing the outcome of the rule. May be points or a failure message.
     */
    public abstract RuleReturn execute();


}
