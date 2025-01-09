package model;

import com.fasterxml.jackson.databind.JsonNode;
import controller.RepoCache;
import controller.RuleMandatories;
import repository_information.RepoFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the requests, the rules in {@link controller.rules} can ask.
 */
public class Repository implements RepoFunctions {

    public String getRepositoryName() {
        return repositoryName;
    }

    // uniquely identifies a repository
    private final String repositoryName;

    public String getOwner() {
        return owner;
    }

    private final String owner;
    private final RepoCache cache;

    public Repository(String repositoryName, String owner) {
        this.repositoryName = repositoryName;
        this.owner = owner;
        this.cache = new RepoCache(repositoryName, owner);
    }

    private HashMap<Class<? extends RuleMandatories>, Integer> results = new HashMap<>();

    private int overallPoints = 0;

    public List<TextFile> getTextfiles() {
        String repoIdentifier ="repository: " + repositoryName + " of owner: " + owner;
        Map<String, String> textFileUrl = new HashMap<>();
        List <TextFile> textFiles = new ArrayList<>();
        JsonNode structure = getStructure();
        JsonNode tree = structure.get("tree");
        if (tree == null || !tree.isArray()) {
            System.out.println("No files found in " + repoIdentifier);
            return new ArrayList<TextFile>();
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
                        path.endsWith(".pdf") ||
                        path.endsWith(".docx")) {
                    textFileUrl.put(entry.get("url").asText(), path);
                    textFileCount++;


                }
            }
        }
        System.out.println("Found " + textFileCount + " textfiles in " + repoIdentifier);
        for (Map.Entry<String, String> url: textFileUrl.entrySet()) {
            String content = getFile(url.getKey());
            if (content != null) {
                TextFile textFile = new TextFile(url.getKey(), url.getValue(), content);
                //System.out.println("Found text file: " + textFile.getPath());
                textFiles.add(textFile);
                //System.out.println("Remaining textfiles of " + repoIdentifier + ": " + textFileCount--);
            }
        }
        return textFiles;
    }

    @Override
    public JsonNode getStructure() {
        return cache.getStructure();
    }

    @Override
    public String getFile(String path) {
        return cache.getFile(path);
    }

    public int saveResult(Class<? extends RuleMandatories> rule, int points) {
        results.put(rule, points);
        return points;
    }

    public String customQuery(String GraphQLQuery) {
        return ""; //TODO: implement
    }

    public int finish() {

        for (Integer value : results.values()) {
            overallPoints += value;
        }
        return overallPoints;
    }

    public int getOverallPoints() {
        return overallPoints;
    }
}
