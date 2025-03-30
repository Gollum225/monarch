package controller.rules;

import com.fasterxml.jackson.databind.JsonNode;
import controller.Rule;
import controller.RuleType;
import exceptions.CloneProhibitedException;
import model.Repository;
import model.RuleReturn;

/**
 * Checks if the repository contains a folder with documentation.
 */
public class DocFolder extends Rule {
    private final int MAX_POINTS = 5;

    public DocFolder(Repository repository) {
        super(RuleType.MANDATORY, repository);
    }

    @Override
    public RuleReturn execute() {

        String[] docPaths = {"doc", "docs", "documentation", "documentations", };
        JsonNode structure = null;
        try {
            structure = repository.getStructure();
        } catch (CloneProhibitedException e) {
            return new RuleReturn(e.getMessage(), repository.getIdentifier(), this.getClass().getSimpleName());
        }
        int counter = 0;
        String lastFoundPath = "";
        for (JsonNode node : structure) {
            if (!node.get("type").asText().equals("tree")) {
                continue;
            }
            String path = node.get("path").asText();
            for (String docPath : docPaths) {
                // Takes the String of each last path and checks, if it contains one of the docPaths.
                if (contains(getLastFolder(path), docPath)) {
                    counter++;
                    lastFoundPath = path;
                    break;

                }
            }
        }

        if (counter == 0) {
            return new RuleReturn(0);
        } else if (counter == 1) {
            System.out.println("Found 1 documentation folder in " + repository.getIdentifier() + " at: " + lastFoundPath);
            return new RuleReturn(3);
        } else if (counter <= 5){
            System.out.println("Found " + counter + " documentation folders in " + repository.getIdentifier() + " e.g.: " + lastFoundPath);
            return new RuleReturn(4);
        } else {
            System.out.println("Found " + counter + " documentation folders in " + repository.getIdentifier() + " e.g.: " + lastFoundPath);
            return new RuleReturn(MAX_POINTS);
        }
    }

    /**
     * Takes a path and returns the last folder (or file) in it.
     * Basically everything after the last '/'.
     *
     * @param input path
     * @return last folder (or file) in the path
     */
    private String getLastFolder(String input) {
        int index = input.lastIndexOf('/');
        if (index != -1 && index + 1 < input.length()) {
            return input.substring(index + 1);
        }
        // Path was top level directory
        return input;
    }

}
