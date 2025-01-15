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

    public static List<Repository> parseRepositories(String jsonResponse) throws JsonProcessingException {
        // Jackson ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // Die JSON-Response als JsonNode parsen
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Den Pfad zur "edges"-Liste finden
        JsonNode edges = rootNode.at("/data/search/edges");

        // Liste für die Ergebnisse
        List<Repository> repositories = new ArrayList<>();

        // Über die Knoten in der "edges"-Liste iterieren
        for (JsonNode edge : edges) {
            JsonNode node = edge.get("node");
            String name = node.get("name").asText(); // Repository-Name
            String owner = node.get("owner").get("login").asText(); // Owner-Login

            // Repository-Objekt erstellen und zur Liste hinzufügen
            repositories.add(new Repository(name, owner));
        }

        return repositories;
    }





}
