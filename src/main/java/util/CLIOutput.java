package util;

/**
 * Class responsible for output to the command line.
 * It is used to print the status of the program to deliver intermediate results, success and failures.
 */
public final class CLIOutput{

    private CLIOutput() {
        throw new UnsupportedOperationException("Utility-class shouldn't be instantiated.");
    }

    // ANSI escape codes for colored output:
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";


    /**
     * For important warning.
     *
     * @param message to be messaged to the user.
     */
    public static void warning(String message) {
        System.out.println(ANSI_YELLOW + message + ANSI_RESET);
    }

    /**
     * For a successful result.
     *
     * @param message to be messaged to the user.
     */
    public static void success(String message) {
        System.out.println(ANSI_GREEN + message + ANSI_RESET);
    }

    /**
     * For an error or unsuccessful result.
     *
     * @param message to be messaged to the user.
     */
    public static void error(String message) {
        System.out.println(ANSI_RED + message + ANSI_RESET);
    }

    /**
     * For normal information to the user.
     *
     * @param message to be messaged to the user.
     */
    public static void info(String message) {
        System.out.println(message);
    }

    public static void cannotClone(String repoName, String owner, String reason) {
        System.out.print(ANSI_RED + "Couldn't clone " + repoName + " of: " + owner + ANSI_RESET);
        if (reason != null && !reason.isEmpty()) {
            System.out.println(ANSI_RED + " due to: " + reason + ANSI_RESET);
        } else {
            System.out.println(" ");
        }
    }

    public static void tryToClone(String repoName, String owner, String reason) {
        System.out.print(ANSI_BLUE + "Trying to clone " + repoName + " of: " + owner + ANSI_RESET);
        if (reason != null && !reason.isEmpty()) {
            System.out.println(ANSI_BLUE + " due to: " + reason + ANSI_RESET);
        } else {
            System.out.println(" ");
        }
    }

    public static void found(String number, String what, String where) {
        info("Found " + number + " " + what + " " + where);
    }

    public static void repositoryProcessingError(String cause) {
        System.err.println("Error processing a repository: " + cause);
    }

    public static void ruleInfo(String ruleName, String repoIdentifier, String message) {
        info(ruleName + ": " + repoIdentifier + ": "+ message);
    }
}
