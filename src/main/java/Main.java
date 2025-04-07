import controller.Checker;
import util.CLIOutput;


public class Main {

    public static void main(String[] args) {

        // first argument args [0]: number of repositories to check
        // second argument args[1]: search term
        // third argument args[2]: quality points

        Checker checker;
        int repositoriesToCheck;

        if (args.length == 0) {
            CLIOutput.info("Please insert number of repositories to check.");
            return;
        } else {
            try {
                repositoriesToCheck = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                repositoriesToCheck = -1;
            }
            if (repositoriesToCheck < 0) {
                CLIOutput.info("The first argument must be a positive number of repositories to analyze. " + args[0] + " is invalid.");
                return;
            } else {
                CLIOutput.info("Analyze " + args[0] + " repositories.");
            }
        }

        if (args.length > 1) {
            CLIOutput.info("Looking only for repositories with search term: " + args[1]);
            int numberOfStars;
            try {
                numberOfStars = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                numberOfStars = -1;
            }
            if (numberOfStars < 0) {
                CLIOutput.info("The third argument must be a positive number of repositories to analyze.");
                checker = new Checker(args[1]);
            } else {
                CLIOutput.info("Looking for repositories with more than " + args[2] + " quality points");
                checker = new Checker(args[1], numberOfStars);
            }
        } else {
            checker = new Checker();
        }

        checker.checkRepos(repositoriesToCheck);

    }


}