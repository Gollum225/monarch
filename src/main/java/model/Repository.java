package model;

import controller.RuleMandatories;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implements the requests, the rules in {@link java.java.controller.rules} can ask.
 */
public class Repository {

    // uniquely identifies a repository
    private final String repositoryName;
    private final String owner;

    public Repository(String repositoryName, String owner) {
        this.repositoryName = repositoryName;
        this.owner = owner;
    }

    private HashMap<Class<? extends RuleMandatories>, Integer> results;

    public ArrayList<TextFile> getTextfiles() {
        return new ArrayList<TextFile>();
    }

    public void saveResult(Class<? extends RuleMandatories> rule, int points) {
        results.put(rule, points);
    }
}
