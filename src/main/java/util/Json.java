package util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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




}
