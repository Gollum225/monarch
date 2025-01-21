package controller;

import model.Repository;

public abstract class Rule {

    final RuleType type;
    final Repository repository;

    protected Rule(RuleType type, Repository repository) {
        this.type = type;
        this.repository = repository;
    }


    /**
     * Executes the rule.
     *
     * @return amount of points the rule has given.
     */
    public abstract int execute();


}
