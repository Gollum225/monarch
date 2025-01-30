package controller.rules;

public class RuleReturn{
    private int points;
    private String failureMessage;
    private boolean isApplicable;

    public RuleReturn(int points){
        this.points = points;
        this.isApplicable = true;

    }

    public RuleReturn(String failureMessage){
        points = 0;
        this.isApplicable = false;
        this.failureMessage = failureMessage;
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
