package model;

import controller.RuleMandatories;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implements the requests, the rules in {@link controller.rules} can ask.
 */
public class Repository {

    private HashMap<Class<? extends RuleMandatories>, Integer> results;

    public ArrayList<TextFile> getTextfiles() {
        return new ArrayList<TextFile>();
    }

    public void saveResult(Class<? extends RuleMandatories> rule, int points) {
        results.put(rule, points);
    }
}
