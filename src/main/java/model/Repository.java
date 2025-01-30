package model;

import com.fasterxml.jackson.databind.JsonNode;
import repository_information.RepoCache;
import controller.Rule;
import repository_information.RepoFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Implements the requests, the rules in {@link controller.rules} can ask.
 */
public class Repository implements RepoFunctions {


    // uniquely identifies a repository
    private final String repositoryName;
    private final String owner;

    /**
     * All request will be called on the cache.
     */
    private final RepoCache cache;

    private boolean isCloned = false;

    /**
     * Constructor for the Repository.
     * @param repositoryName name of the repository to uniquely identify it with the {@link #owner}
     * @param owner owner of the repository
     */
    public Repository(String repositoryName, String owner) {
        this.repositoryName = repositoryName;
        this.owner = owner;
        this.cache = new RepoCache(repositoryName, owner);
    }

    /**
     * Maps the rule to the points it has given.
     */
    private HashMap<Class<? extends Rule>, RuleReturn> results = new HashMap<>();

    private int overallPoints = 0;

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getOwner() {
        return owner;
    }

    public List<TextFile> getTextfiles() {
        String repoIdentifier ="repository: " + repositoryName + " of owner: " + owner;
        List <JsonNode> foundTextFiles = new ArrayList<>();

        List <TextFile> parsedTextFiles = new ArrayList<>();
        JsonNode tree = getStructure();
        if (tree == null || !tree.isArray()) {
            System.out.println("No files found in " + repoIdentifier);
            return new ArrayList<>();
        }
        System.out.println("Found " + tree.size() + " files in " + repoIdentifier);
        int textFileCount = 0;

        for (JsonNode entry : tree) {
            if (entry.get("type").asText().equals("blob")) {
                String path = entry.get("path").asText();
                if (path.endsWith(".txt") ||
                        path.endsWith(".md") ||
                        path.endsWith(".markdown") ||
                        path.endsWith(".rst") ||
                        path.endsWith(".adoc") ||
                        //path.endsWith(".pdf") ||
                        path.endsWith(".docx")) {
                    foundTextFiles.add(entry);
                    textFileCount++;


                }
            }
        }
        System.out.println("Found " + textFileCount + " textfiles in " + repoIdentifier);
        for (JsonNode file: foundTextFiles) {
            String path = file.get("path").asText();
            String content = getFile(path);
            if (content != null) {
                TextFile textFile = new TextFile(path, content);
                //System.out.println("Found text file: " + textFile.getPath());
                parsedTextFiles.add(textFile);
                //System.out.println("Remaining textfiles of " + repoIdentifier + ": " + textFileCount--);
            }
        }
        return parsedTextFiles;
    }

    @Override
    public JsonNode getStructure() {
        return cache.getStructure();
    }

    @Override
    public String getFile(String path) {
        return cache.getFile(path);
    }

    @Override
    public boolean changeToClone(String reason) {
        return cache.changeToClone(reason);
    }

    public void saveResult(Class<? extends Rule> rule, RuleReturn points) {
        results.put(rule, points);
    }

    public String customQuery(String GraphQLQuery) {
        return ""; //TODO: implement
    }

    public int finish() {

        for (RuleReturn rule : results.values()) {
            if (rule.isApplicable()) {
                overallPoints += rule.getPoints();
            }
        }
        return overallPoints;
    }

    public int getOverallPoints() {
        return overallPoints;
    }

    public HashMap<Class<? extends Rule>, RuleReturn> getResults() {
        return results;
    }

    public boolean checkFileExistence(String path) {
        JsonNode structure = getStructure();
        for (JsonNode file: structure) {
            if (file.get("path").asText().equals(path)) {
                return true;
            }
        }
        return false;
    }
}
