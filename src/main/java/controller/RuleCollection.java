package controller;

import controller.rules.DocFolder;
import controller.rules.QualityMetric;
import controller.rules.SearchOwnerRepo;
import controller.rules.KeyWord;
import controller.rules.LLMReadme;
import controller.rules.ReadReadmeLinks;
import model.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection of rules from the {@link controller.rules} package.
 */
public class RuleCollection {

    /**
     * The rules to be applied to the {@link Repository}.
     */
    private final List<RuleFactory> rules = new ArrayList<>();

    /**
     * Creates a new RuleCollection. Adds the rules in the constructor to the list to be executed.
     */
    public RuleCollection() {
        rules.add(new RuleFactory("Keyword", KeyWord::new));
        rules.add(new RuleFactory("LLMReadme", LLMReadme::new));
        rules.add(new RuleFactory("DocFolder", DocFolder::new));
        rules.add(new RuleFactory("ReadReadmeLinks", ReadReadmeLinks::new));
        rules.add(new RuleFactory("SearchOwnerRepo", SearchOwnerRepo::new));
        rules.add(new RuleFactory("QualityMetric", QualityMetric::new));
    }


    /**
     * Equips the {@link Repository} with the rules from the {@link controller.rules} package.
     * Factory pattern.
     *
     * @param repository the repository, which is given to the rules to work with.
     * @return a list of rules with the given repository.
     */
    public List<RuleFactory> equipRules(Repository repository) {
        ArrayList<Rule> ruleList = new ArrayList<>();

        for (RuleFactory factory : rules) {
            ruleList.add(factory.create(repository));
        }
        return rules;
    }

    /**
     * Getter for the rules.
     *
     * @return the list of rules
     */
    public List<RuleFactory> getRules() {
        return rules;
    }

    /**
     * Getter for the number of rules.
     *
     * @return number of rules
     */
    public int getNumberOfRules() {
        return rules.size();
    }
}
