package model;

import com.fasterxml.jackson.databind.JsonNode;
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


    // uniquely identifies a repository:
    private final String repositoryName;
    private final String owner;
    private final String repoIdentifier;

    /**
     * All requests will be called on the cache.
     */
    private RepoCache cache;

    /**
     * Date of creation of the repository.
     */
    private final Date created;

    private long duration;

    /**
     * Content of the readme file. null if no readme file is found, or if the readme file is not yet requested.
     */
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
        this.repoIdentifier = "repository: " + repositoryName + " of owner: " + owner;
    }

    /**
     * Maps the rule to the points it has given.
     */
    private HashMap<Class<? extends Rule>, RuleReturn> results = new HashMap<>();

    /**
     * The overall points of the repository. Starting with 0.
     */
    private int overallPoints = 0;

    /**
     * Getter for the name of the repository.
     *
     * @return repository name.
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * Getter for the owner of the repository.
     *
     * @return repository owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Returns all textfiles of a {@link Repository}. The textfiles are identified by their file extension.
     * Searched extensions are: txt, md, markdown, rst, adoc, pdf, docx.
     * @return list of {@link TextFile}
     * @throws CloneProhibitedException if this call lead to cloning of the repository, where cloning is prohibited.
     */
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

    /**
     * Gets the quality metric of a repository. e.g., GitHub stars.
     *
     * @return number of points
     */
    public int getQualityMetrics() {
        JsonNode generalInfo = generalInfo();
        if (generalInfo == null) {
            return 0;
        }
        return generalInfo.get("stargazers_count").asInt();
    }

    /**
     * Saves the result of an executed rule in the repository.
     *
     * @param rule that was executed
     * @param points the rule gave
     */
    public void saveResult(Class<? extends Rule> rule, RuleReturn points) {
        results.put(rule, points);
    }

    @Override
    public void finish() {
        duration = (new Date().getTime() - created.getTime());
        calculateOverallPoints();
        cache.finish();
        cache = null;
    }

    /**
     * Getter for the overall points.
     *
     * @return sum of all points given by the rules.
     */
    public int getOverallPoints() {
        calculateOverallPoints();
        return overallPoints;
    }

    private void calculateOverallPoints() {
        overallPoints = 0;
        for (RuleReturn rule : results.values()) {
            if (rule.isApplicable()) {
                overallPoints += rule.getPoints();
            }
        }
    }

    /**
     * Getter for results.
     *
     * @return map of rules and their results.
     */
    public HashMap<Class<? extends Rule>, RuleReturn> getResults() {
        return results;
    }

    /**
     * Checks if a file exists in the repository.
     *
     * @param path the path of the file to check.
     * @return true, if the file exists.
     * @throws CloneProhibitedException if this methode lead to a clone of the repository, but the repository is prohibited to clone.
     */
    public boolean checkFileExistence(String path) throws CloneProhibitedException {
        JsonNode structure = getStructure();
        for (JsonNode file: structure) {
            if (file.get("path").asText().equals(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the date where the repository was discovered. This is not the date, where it was begun to be analyzed.
     *
     * @return date of discovery.
     */
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

    /**
     * Returns the identifier of the repository. The identifier is the name of the repository and the owner.
     * Identifier is used mainly for logging, because it is a nicely formatted string.
     * Use {@link #getRepositoryName()} and {@link #getOwner()} for other code-purposes.
     *
     * @return identifier of the repository.
     */
    public String getIdentifier() {
        return repoIdentifier;
    }

    public long getDuration() {
        return duration;
    }
}
