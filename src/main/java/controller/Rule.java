package controller;

import controller.rules.RuleReturn;
import exceptions.CloneProhibitedException;
import model.Repository;

import java.util.concurrent.Callable;

/**
 * Represents a rule that can be applied to a repository. The rules needs to be registered in {@link RuleCollection#RuleCollection()}.
 */
public abstract class Rule {

    /**
     * The type of the rule.
     */
    protected final RuleType type;

    /**
     * The repository the rule is working on.
     */
    protected final Repository repository;

    /**
     * Creates a new rule.
     *
     * @param type type of the rule
     * @param repository the rule is working on
     */
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
