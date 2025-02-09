package controller;

import controller.rules.RuleReturn;
import model.Repository;

import java.util.List;

/**
 * Represents a rule that can be applied to a repository. The rules need to be registered in {@link RuleCollection#RuleCollection()}.
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

    /**
     * Checks if the text contains the term. The search is case-insensitive.
     * Only returns true, if the term is not surrounded by other letters.
     *
     * @param text to search in
     * @param term to search for
     * @return true, if the term is found in the text
     */
    protected boolean contains(String text, String term) {
        return scanText(false, text, term) > 0;
    }

    /**
     * Counts the number of occurrences of the term in the text.
     * The search is case-insensitive.
     * Only returns true, if the term is not surrounded by other letters.
     *
     * @param text to search in
     * @param term to search for
     * @return amount of occurrences
     */
    protected int countKeywordsMultipleOccurrence(String text, String term) {
        return scanText(true, text, term);
    }

    /**
     * Counts all occurrences of the terms in the text.
     *
     * @param text to search in
     * @param terms to search for
     * @return amount of occurrences
     */
    protected int countMultipleKeywordsMultipleOccurrence(String text, List<String> terms) {
        int count = 0;
        for (String term : terms) {
            count += countKeywordsMultipleOccurrence(text, term);
        }
        return count;
    }

    /**
     * Counts one occurrence of every term in the text.
     * Only returns true, if the term is not surrounded by other letters.
     *
     * @param text to search in
     * @param terms to search for
     * @return amount of occurrences
     */
    protected int countMultipleKeywordsSingleOccurrence(String text, List<String> terms) {
        int count = 0;
        for (String term : terms) {
            count += scanText(false, text, term);
        }
        return count;
    }

    /**
     * Scans the text for the term.
     * Only returns true, if the term is not surrounded by other letters.
     * For performance reasons the method can stop after the first occurrence.
     * May not work in all regions, because of call to {@link String#toLowerCase()}.
     *
     * @param count if true, the method will count the occurrences
     * @param text to search in
     * @param term to search for
     * @return amount of occurrences, if count is true max. 1
     */
    private int scanText(boolean count, String text, String term) {
        if (term.isEmpty()) {
            return 0;
        }

        text = text.toLowerCase();
        term = term.toLowerCase();
        int index = text.indexOf(term);
        int counter = 0;
        while (index != -1) {
            boolean before = (index == 0) || !Character.isLetter(text.charAt(index - 1));
            boolean after = (index + term.length() == text.length()) || !Character.isLetter(text.charAt(index + term.length()));

            if (!count && before && after) {
                return 1;
            } else if (count && before && after) {
                counter++;
            }

            index = text.indexOf(term, index + 1);
        }
        return counter;
    }

}
