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
        rules.add(new RuleFactory("KeyWord", repository -> new KeyWord(repository,new int[]{0, 1, 5, 20, 50, 200})));
        rules.add(new RuleFactory("LLMReadme", repository -> new LLMReadme(repository,5)));
        rules.add(new RuleFactory("DocFolder", repository -> new DocFolder(repository,new int[]{0, 0, 0, 1, 2, 5})));
        rules.add(new RuleFactory("ReadReadmeLinks", repository -> new ReadReadmeLinks(repository,new int[]{0, 0, 1, 5, 5, 9, 9, 11, 11, 16})));
        rules.add(new RuleFactory("SearchOwnerRepo", repository -> new SearchOwnerRepo(repository,new int[]{12, 18, 23})));
        rules.add(new RuleFactory("QualityMetric", repository -> new QualityMetric(repository,new int[]{0, 100, 5000, 10000, 25000, 100000})));
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
