package controller.rules;

import com.fasterxml.jackson.databind.JsonNode;
import controller.Rule;
import controller.RuleType;
import exceptions.CloneProhibitedException;
import model.Repository;
import model.RepositoryAspectEval;

/**
 * Checks if the repository contains a folder with documentation.
 */
public class DocFolder extends Rule {

    private static int[] limits;

    private static final String[] DOC_PATHS = {"doc", "docs", "documentation", "documentations", };


    public DocFolder(Repository repository, int[] limits) {
        super(RuleType.MANDATORY, repository);
        if (DocFolder.limits == null) {
            DocFolder.limits = limits;
        }
    }

    @Override
    public RepositoryAspectEval execute() {

        JsonNode structure;
        try {
            structure = repository.getStructure();
        } catch (CloneProhibitedException e) {
            return new RepositoryAspectEval(e.getMessage(), repository.getIdentifier(), this.getClass().getSimpleName());
        }
        int counter = 0;
        String lastFoundPath = "";
        for (JsonNode node : structure) {
            if (!node.get("type").asText().equals("tree")) {
                continue;
            }
            String path = node.get("path").asText();
            for (String docPath : DOC_PATHS) {
                // Takes the String of each last path and checks if it contains one of the docPaths.
                if (contains(getLastFolder(path), docPath)) {
                    counter++;
                    lastFoundPath = path;
                    break;

                }
            }
        }
        if (counter > 0) {
            System.out.println("Found " + counter + " documentation folders in " + repository.getIdentifier() + " e.g.: " + lastFoundPath);
        }
        return new RepositoryAspectEval(calculatePointsWithLimits(limits, counter));
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
