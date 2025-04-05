package exceptions;

/**
 * Exception is thrown when a rule leads to the repository to be cloned, but cloning is prohibited, e.g., due to rate limits or large size of the repository.
 */
public class CloneProhibitedException extends Exception {

    /**
     * Creates a new exception. Passes a default message to the super class.
     */
    public CloneProhibitedException() {
        super("Tried to clone, but cloning is prohibited.");
    }

    /**
     * Creates a new exception with a custom message.
     *
     * @param message the message to be passed to the super class {@link Exception}
     */
    public CloneProhibitedException(String message) {
        super(message);
    }
}
