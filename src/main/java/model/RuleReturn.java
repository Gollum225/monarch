package model;

import controller.Rule;

/**
 * Return value (of the {@link Rule#execute()} methode) of a rule.
 */
public class RuleReturn{

    /**
     * Points the repository received.
     */
    private final int points;

    /**
     * A reason why the rule couldn't be executed.
     */
    private final String failureMessage;

    /**
     * true, if the rule could be executed as expected and can return the received points.
     */
    private final boolean isApplicable;

    /**
     * Creates a new {@link RuleReturn} in case of successfully executing the rule.
     *
     * @param points the received points. Should be bigger than 0.
     */
    public RuleReturn(int points){
        this.points = Math.max(points, 0);
        this.failureMessage = null;
        this.isApplicable = true;
    }

    /**
     * Creates a new {@link RuleReturn} in case of not being able to execute the rule.
     *
     * @param failureMessage reason, why the rule couldn't be executed.
     * @param repoIdentifier identifies the repository with name and owner. {@link Repository#getIdentifier()} can be used.
     * @param ruleName of the executing rule. {@link Rule#getClass()#getSimpleName()} can be used.
     */
    public RuleReturn(String failureMessage, String repoIdentifier, String ruleName){
        points = 0;
        this.isApplicable = false;
        this.failureMessage = failureMessage;
        System.out.println("Rule " + ruleName + " not applicable for " + repoIdentifier + ". Reason: " + failureMessage);
    }

    /**
     * Received points of the repository.
     * @return amount, not negative.
     */
    public int getPoints(){
        return points;
    }

    /**
     * Returns the reason why the rule couldn't be executed.
     *
     * @return reason.
     */
    public String getFailureMessage(){
        return failureMessage;
    }

    /**
     * Returns if the rule was applicable on the repository.
     *
     * @return true, if an amount of points could be given.
     */
    public boolean isApplicable(){
        return isApplicable;
    }

    /**
     * Returns the points, if the rule could be executed, failure message else.
     *
     * @return points or failure reason as string.
     */
    public String getResultString() {
        if (isApplicable) {
            return String.valueOf(points);
        } else {
            return "Rule not applicable. Reason: " + failureMessage;
        }
    }
}
