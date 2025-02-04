package controller;

import controller.rules.DocFolder;
import controller.rules.KeyWord;
import controller.rules.LLMReadme;
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

    public RuleCollection() {
        rules.add(KeyWord.class);
        rules.add(LLMReadme.class);
        rules.add(DocFolder.class);
    }


    //TODO: ist das eine factory?
    /**
     * Equips the {@link Repository} with the rules from the {@link controller.rules} package.
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
                e.getCause().printStackTrace();
            }
        }
        return ruleList;
    }

    public ArrayList<Class<? extends Rule>> getRules() {
        return rules;
    }
}
