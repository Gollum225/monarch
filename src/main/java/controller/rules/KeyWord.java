package controller.rules;

import controller.Rule;
import controller.RuleType;
import model.Repository;
import util.Json;
import model.TextFile;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * This mandatory rule scans the textfiles for a given list of keywords.
 */
public class KeyWord extends Rule {

    private final String[] KEYWORDS = Json.getAllKeywords();
    private final List<TextFile> textFiles;

    // The limits determine, how many keywords need to be found for a point.
    private final int FIRST_LIMIT = 1;
    private final int SECOND_LIMIT = 5;
    private final int THIRD_LIMIT = 20;
    private final int FOURTH_LIMIT = 50;
    private final int LAST_LIMIT = 200;


    public KeyWord(Repository repository) {
        super(RuleType.MANDATORY, repository);
        textFiles = repository.getTextfiles();
    }

    @Override
    public int execute() {

        if (KEYWORDS == null || KEYWORDS.length == 0) {
            return 0;
        }

        AtomicInteger keywordCount = new AtomicInteger();

        // Stops, the execution, if enough keywords are found.
        AtomicBoolean stopProcessing = new AtomicBoolean(false);

        // Look at the files parallel and count the keywords.
        textFiles.parallelStream()
                .forEach(textFile -> {
                    int count = 0;
                    for (String keyword : KEYWORDS) {
                        if (stopProcessing.get()) {
                            return;
                        }
                        // Following condition is taken from StackOverflow: https://stackoverflow.com/a/90780
                        // This is necessary, because the method "contains" is case-sensitive.
                        if (Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE).matcher(textFile.getContent()).find()) {
                            count++;
                        }
                    }
                    if (keywordCount.addAndGet(count) > LAST_LIMIT) {
                        stopProcessing.set(true);
                    }
                });
        if (keywordCount.get() >= LAST_LIMIT) {
            return 5;
        } else if (keywordCount.get() >= FOURTH_LIMIT) {
            return 4;
        } else if (keywordCount.get() >= THIRD_LIMIT) {
            return 3;
        } else if (keywordCount.get() >= SECOND_LIMIT) {
            return 2;
        } else if (keywordCount.get() >= FIRST_LIMIT) {
            return 1;
        } else {
            return 0;
        }
    }
}