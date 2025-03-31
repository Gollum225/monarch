package exceptions;

/**
 * Exception is thrown when the requested requests are too large for the API.
 */
public class APIOverloaded extends Exception {

    /**
     * Creates a new exception. Passes a default message to the super class.
     */
    public APIOverloaded() {
        super("Tried to clone, but cloning is prohibited.");
    }

    /**
     * Creates a new exception with a custom message.
     *
     * @param message the message to be passed to the super class {@link Exception}
     */
    public APIOverloaded(String message) {
        super(message);
    }
}
