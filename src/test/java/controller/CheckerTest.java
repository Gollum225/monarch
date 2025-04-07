package controller;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class CheckerTest {

    @Test
    void testResultFileIsCreatedAndNotEmpty() throws IOException {
        Checker checker = new Checker();
        checker.checkRepos(1);

        File resultFile = new File("src/main/output/result.csv");

        assertTrue(resultFile.exists(), "Result file was created.");

        String content = Files.readString(Paths.get(resultFile.toURI()));
        assertFalse(content.isBlank(), "Content is in the result file.");
    }
}
