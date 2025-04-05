package util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for general JSON related operations.
 */
public final class JsonUtil {

    private static final String JSON_FILE_PATH = "src/main/resources/keywords.json";

    private JsonUtil() {
        throw new UnsupportedOperationException("Utility-class shouldn't be instantiated.");
    }

    /**
     * Parses the JSON response from the GitHub API and returns a list of repositories.
     *
     * @param rootNode the root node of the JSON response
     * @return a list of repositories
     */
    public static List<Repository> parseGraphQLRepositories(JsonNode rootNode) {

        JsonNode edges = rootNode.at("/data/search/edges");

        List<Repository> repositories = new ArrayList<>();

        for (JsonNode edge : edges) {
            JsonNode node = edge.get("node");
            String name = node.get("name").asText(); // Repository-Name
            String owner = node.get("owner").get("login").asText(); // Owner-Login

            repositories.add(new Repository(name, owner));
        }

        return repositories;
    }

    public static List<Repository> parseRestRepositories(JsonNode rootNode) {
        JsonNode items = rootNode.get("items");
        List<Repository> repositories = new ArrayList<>();
        for (JsonNode item : items) {
            String name = item.get("name").asText();
            String owner = item.get("owner").get("login").asText();
            repositories.add(new Repository(name, owner));
        }
        return repositories;
    }

    public static Map<String, List<String>> getGroupedKeywords() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(new File(JSON_FILE_PATH), new TypeReference<>() {});
    }

}
