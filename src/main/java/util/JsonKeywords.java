package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Responsible for keywords from the JSON file.
 */
public final class JsonKeywords {

    private static Map<String, List<String>> list;

    private JsonKeywords() {
        throw new UnsupportedOperationException("Utility-class shouldn't be instantiated.");
    }

    /**
     * Read the JSON file.
     */
    private static void setup() throws IOException {
        list = JsonUtil.getGroupedKeywords();
    }

    /**
     * Reads the JSON file with the keywords.
     *
     * @return a single array with all the keywords.
     */
    public static String[] getAllKeywords() {

        checkJson();

        List<String> allKeywords = new ArrayList<>();
        for (List<String> keywords : list.values()) {
            allKeywords.addAll(keywords);
        }

        // Convert to a native array for faster iteration.
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

    /**
     * Loads the JSON file content, if not done before.
     */
    public static void checkJson() {
        if (list == null) {
            try {
                setup();
            } catch (IOException e) {
                throw new RuntimeException("Couldn't read the JSON file.");
            }
        }
        if (list == null) {
            throw new RuntimeException("Couldn't read the JSON file.");
        }
    }

}
