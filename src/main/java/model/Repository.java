package model;

import com.fasterxml.jackson.databind.JsonNode;
import controller.rules.RuleReturn;
import exceptions.CloneProhibitedException;
import repository_information.RepoCache;
import controller.Rule;
import repository_information.RepoFunctions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

    /**
     * Date of creation of the repository.
     */
    private Date created;

    private String readme;

    /**
     * Constructor for the Repository.
     * @param repositoryName name of the repository to uniquely identify it with the {@link #owner}
     * @param owner owner of the repository
     */
    public Repository(String repositoryName, String owner) {
        this.repositoryName = repositoryName;
        this.owner = owner;
        this.cache = new RepoCache(repositoryName, owner);
        this.created = new Date();
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

    public List<TextFile> getTextfiles() throws CloneProhibitedException {
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
        List<String> paths = new ArrayList<>(textFileCount);
        for (JsonNode file: foundTextFiles) {
            paths.add(file.get("path").asText());
        }
        Map<String, String> files = getFiles(paths);
        for (Map.Entry<String, String> entry : files.entrySet()) {
            parsedTextFiles.add(new TextFile(entry.getKey(), entry.getValue()));
        }

        return parsedTextFiles;
    }

    @Override
    public JsonNode getStructure() throws CloneProhibitedException {
        return cache.getStructure();
    }

    @Override
    public Map<String, String> getFiles(List<String> paths) throws CloneProhibitedException {
        return cache.getFiles(paths);
    }

    @Override
    public boolean changeToClone(String reason) throws CloneProhibitedException {
        return cache.changeToClone(reason);
    }

    @Override
    public JsonNode generalInfo() {
        return cache.generalInfo();
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

    public boolean checkFileExistence(String path) throws CloneProhibitedException {
        JsonNode structure = getStructure();
        for (JsonNode file: structure) {
            if (file.get("path").asText().equals(path)) {
                return true;
            }
        }
        return false;
    }

    public Date getCreationDate() {
        return created;
    }

    /**
     * Returns the content of the readme file. If no readme file is found in the top level directory,
     * a readme file from a possible documentation folder is returned.
     *
     * @return readme file content or null, if no readme file is found.
     */
    public String getReadme() throws CloneProhibitedException {
        if (readme != null) {
            return readme;
        }

        String[] readmeNames = {"README.md", "readme.md", "Readme.md", "README", "readme", "Readme"};

        for (String name : readmeNames) {
            if (checkFileExistence(name)) {
                readme = getFiles(new ArrayList<>(List.of(name))).get(name);
                return readme;
            }
        }

        for (JsonNode file : getStructure()) {
            String path = file.get("path").asText();
            if (Pattern.compile(Pattern.quote("docs/readme"), Pattern.CASE_INSENSITIVE).matcher(path).find()||
                    Pattern.compile(Pattern.quote("documentation/readme"), Pattern.CASE_INSENSITIVE).matcher(path).find()||
                    Pattern.compile(Pattern.quote("readme"), Pattern.CASE_INSENSITIVE).matcher(path).find()) {
                readme = getFiles(new ArrayList<>(List.of(path))).get(path);
                return readme;
            }
        }
        return null;

    }
}
