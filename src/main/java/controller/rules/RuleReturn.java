package controller.rules;

public class RuleReturn{
    private int points;
    private String failureMessage;
    private boolean isApplicable;

    public RuleReturn(int points){
        this.points = points;
        this.isApplicable = true;

    }

    public RuleReturn(String failureMessage, String repoIdentifier, String ruleName){
        points = 0;
        this.isApplicable = false;
        this.failureMessage = failureMessage;
        System.out.println("Rule " + ruleName + " not applicable for " + repoIdentifier + ". Reason: " + failureMessage);
    }

    public int getPoints(){
        return points;
    }

    public String getFailureMessage(){
        return failureMessage;
    }

    public boolean isApplicable(){
        return isApplicable;
    }

    public String getResultString() {
        if (isApplicable) {
            return String.valueOf(points);
        } else {
            return "Rule not applicable. Reason: " + failureMessage;
        }
    }
}
