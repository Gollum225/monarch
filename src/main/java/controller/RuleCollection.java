package controller;

import controller.rules.DocFolder;
import controller.rules.KeyWord;
import controller.rules.LLMReadme;
import controller.rules.ReadReadmeLinks;
import model.Repository;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Collection of rules from the {@link controller.rules} package.
 */
public class RuleCollection {

    /**
     * The rules to be applied to the {@link Repository}.
     */
    private final ArrayList<Class <? extends Rule>> rules = new ArrayList<>();

    /**
     * Creates a new RuleColection. Adds the rules in the constructor to the list to be executed.
     */
    public RuleCollection() {
        rules.add(KeyWord.class);
        rules.add(LLMReadme.class);
        rules.add(DocFolder.class);
        rules.add(ReadReadmeLinks.class);

        rules.add(controller.rules.QualityMetric.class);
    }


    /**
     * Equips the {@link Repository} with the rules from the {@link controller.rules} package.
     * Factory pattern.
     *
     * @param repository the repository, which is given to the rules to work with.
     * @return a list of rules with the given repository.
     */
    public ArrayList<Rule> equipRules(Repository repository) {
        ArrayList<Rule> ruleList = new ArrayList<>();

        for (Class<? extends Rule> rule : rules) {
            try {
                ruleList.add(rule.getDeclaredConstructor(Repository.class).newInstance(repository));
            } catch (NoSuchMethodException e) {
                System.err.println("Constructor not found for rule: " + rule.getName());
            } catch (InstantiationException e) {
                System.err.println("Failed to instantiate rule: " + rule.getName());
            } catch (IllegalAccessException e) {
                System.err.println("Illegal access when creating rule instance: " + rule.getName());
            } catch (InvocationTargetException e) {
                System.err.println("Exception while invoking constructor for rule: " + rule.getName());
            }
        }
        return ruleList;
    }

    /**
     * Getter for the rules.
     *
     * @return the list of rules
     */
    public ArrayList<Class<? extends Rule>> getRules() {
        return rules;
    }

    public int getRuleAmount() {
        return rules.size();
    }
}
