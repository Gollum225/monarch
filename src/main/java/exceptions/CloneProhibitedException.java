package exceptions;

/**
 * Exception is thrown, when a rule leads to the repository to be cloned, but cloning is prohibited, e.g., due to rate limits or large size of the repository.
 */
public class CloneProhibitedException extends Exception {
    public CloneProhibitedException() {
        super("Tried to clone, but cloning is prohibited.");
    }
}
