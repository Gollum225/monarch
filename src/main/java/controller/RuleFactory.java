package controller;

import model.Repository;
import java.util.function.Function;

public class RuleFactory {
    private final String name;
    private final Function<Repository, Rule> factory;

    public RuleFactory(String name, Function<Repository, Rule> factory) {
        this.name = name;
        this.factory = factory;
    }

    public Rule create(Repository repository) {
        return factory.apply(repository);
    }

    public String getName() {
        return name;
    }
}
