package util;

import com.fasterxml.jackson.core.JsonProcessingException;
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
 * Responsible for the communication with the JSON files.
 */
public final class Json {

    private static Map<String, List<String>> list;
    private static final String jsonFilePath = "src/main/resources/keywords.json";


    private Json() {
        throw new UnsupportedOperationException("Utility-class shouldn't be instantiated.");
    }

    /**
     * Read the JSON file.
     */
    public static void setup() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        list = objectMapper.readValue(new File(jsonFilePath), new TypeReference<Map<String, List<String>>>() {});

    }

    /**
     * Reads the JSON file with the keywords.
     *
     * @return a single array with all the keywords.
     */
    public static String[] getAllKeywords() {

        if (list == null) {
            try {
                setup();
            } catch (IOException e) {
                System.out.println("Couldn't read the JSON file.");
                return null;
            }
            if (list == null) {
                return null;
            }
        }

        List<String> allKeywords = new ArrayList<>();
        for (List<String> keywords : list.values()) {
            allKeywords.addAll(keywords);
        }

        return allKeywords.toArray(new String[0]);
    }

    /**
     * Get specific keywords from the JSON file.
     *
     * @param key of the keywords to get
     * @return list of words under the index
     */
    public static List<String> getSpecificKeywords(String key) {

        checkJson();
        return list.get(key);
    }

    private static void checkJson() {
        if (list == null) {
            try {
                setup();
            } catch (IOException e) {
                System.out.println("Couldn't read the JSON file.");
                throw new RuntimeException("Couldn't read the JSON file.");
            }
        }
    }


    /**
     * Parses the JSON response from the GitHub API and returns a list of repositories.
     *
     * @param rootNode the root node of the JSON response
     * @return a list of repositories
     */
    public static List<Repository> parseRepositories(JsonNode rootNode) throws JsonProcessingException {

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





}
