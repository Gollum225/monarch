package controller.rules;

import controller.Rule;
import controller.RuleType;
import exceptions.CloneProhibitedException;
import model.Repository;
import model.RepositoryAspectEval;
import util.CLIOutput;
import util.JsonKeywords;
import model.TextFile;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * This mandatory rule scans the textfiles for a given list of keywords.
 */
public class KeyWord extends Rule {

    private static final String[] KEYWORDS = JsonKeywords.getAllKeywords();

    // The limits determine how many keywords need to be found for a point.
    private static int[] limits;

    public KeyWord(Repository repository, int[] limits) {
        super(RuleType.MANDATORY, repository);
        if (KeyWord.limits == null) {
            KeyWord.limits = limits;
        }
    }

    @Override
    public RepositoryAspectEval execute() {

        List<TextFile> textFiles;
        try {
            textFiles = repository.getTextfiles();
        } catch (CloneProhibitedException e) {
            return new RepositoryAspectEval(e.getMessage());
        }

        if (textFiles == null || textFiles.isEmpty()) {
            CLIOutput.ruleInfo(this.getClass().getSimpleName(), repository.getIdentifier(), "No files found");
            return new RepositoryAspectEval("No files found.");
        } else {
            CLIOutput.found(String.valueOf(textFiles.size()), "textfiles in", repository.getIdentifier());
        }


        if (KEYWORDS.length == 0) {
            return new RepositoryAspectEval("No keywords found in the JSON file.");
        }

        AtomicInteger keywordCount = new AtomicInteger();

        // Stops, the execution, if enough keywords are found.
        AtomicBoolean stopProcessing = new AtomicBoolean(false);

        // Look at the files parallel and count the keywords.
        textFiles.parallelStream()
                .forEach(textFile -> {
                    int count = 0;
                    if (textFile.getContent() == null || textFile.getContent().isEmpty()) {
                        return;
                    }
                    for (String keyword : KEYWORDS) {
                        if (stopProcessing.get()) {
                            return;
                        }
                        if (keyword == null || keyword.isEmpty()) {
                            continue;
                        }
                        // The following condition is taken from StackOverflow: https://stackoverflow.com/a/90780
                        // This is necessary because the method "contains" is case-sensitive.
                        if (Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE).matcher(textFile.getContent()).find()) {
                            count++;
                        }
                    }
                    if (keywordCount.addAndGet(count) > limits[limits.length -1]) {
                        stopProcessing.set(true);
                    }
                });
        return new RepositoryAspectEval(calculatePointsWithLimits(limits, keywordCount.get()));
    }
}