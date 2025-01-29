package controller.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.Rule;
import controller.RuleType;
import model.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.regex.Pattern;

public class LLMReadme extends Rule {

    // https://community.sambanova.ai/t/rate-limits/321
    private static final int MAX_REQUESTS_PER_MINUTE = 20;

    private final int MAX_POINTS = 5;

    static Date lastRequest = new Date();


    public LLMReadme(Repository repository) {
        super(RuleType.MANDATORY, repository);
    }

    @Override
    public int execute() {

        String readme = getReadme();

        if (readme == null) {
            System.out.println("No readme found in repository: " + repository.getRepositoryName() + " of owner: " + repository.getOwner());
            return 0;
        }

        String llmAnswer = null;

        try {
            llmAnswer = sendGetRequest(readme);
        } catch (IOException e) {
            System.out.println("GAB ERROR"); //TODO
            e.printStackTrace();
        }

        if (llmAnswer == null) {
            //TODO
            return 0;
        }


        for (int i = 0; i <= MAX_POINTS; i++) {
            if (llmAnswer.contains(String.valueOf(i))) {
                System.out.println("LLM gave: " + i + " points to " + repository.getRepositoryName() + " of owner: " + repository.getOwner());
                return i;
            }
        }

        return 0;
    }

    /**
     * Returns the content of the readme file. If no readme file is found in the top level directory,
     * a readme file from a possible documentation folder is returned.
     *
     * @return readme file content or null, if no readme file is found.
     */
    private String getReadme() {
        String[] readmeNames = {"README.md", "readme.md", "Readme.md", "README", "readme", "Readme"};

        for (String name : readmeNames) {
            if (repository.checkFileExistence(name)) {
                return repository.getFile(name);
            }
        }

        for (JsonNode file : repository.getStructure()) {
            String path = file.get("path").asText();
            if (Pattern.compile(Pattern.quote("docs/readme"), Pattern.CASE_INSENSITIVE).matcher(path).find()||
                    Pattern.compile(Pattern.quote("documentation/readme"), Pattern.CASE_INSENSITIVE).matcher(path).find()||
                    Pattern.compile(Pattern.quote("readme"), Pattern.CASE_INSENSITIVE).matcher(path).find()) {
                return repository.getFile(file.get("path").asText());
            }
        }
        return null;

    }

    private String sendGetRequest(String readme) throws IOException {
        // Verbindung öffnen
        URL url = new URL("https://api.sambanova.ai/v1/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // GET-Methode und Header setzen
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + System.getenv("SambaNova_API"));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        //readme = "This is the readme of the Monarch GitHub page. This Project is a well documented Repository miner. All architecural documents are in the \"docs\" Folder. There are UML Files for class diagrams as well as sequence Diagramms and components diagram and others. Every diagram is well described in words as well. A big thanks to Niklas Weber for supporting this Project. Feel free to support this Project with Money or your support. ";

        ObjectMapper objectMapper = new ObjectMapper();
        String formattetReadme = objectMapper.writeValueAsString(readme);

        String roleContent
                = " \"You are a machine to assist in evaluating a readme file of a GitHub repository according to the likelihood of the repository containing architectural information. Formulate your response as one point in the range of 0 to " + MAX_POINTS + " points. Answer only with the one number.\"";



        String jsonInput = """
                {
                    "stream": false,
                    "max_tokens": 1,
                    "model": "Meta-Llama-3.1-70B-Instruct",
                    "messages": [
                        {
                            "role": "system",
                            "content": """ + roleContent + """
                        },
                        {
                            "role": "user",
                            "content": """ + formattetReadme + """
                        }
                    ]
                }
            """;


        waitForAPI();

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        requestDone();
        // Statuscode überprüfen
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            System.err.println("Error while getting Samba Nova response. Code: " + responseCode +" "+ connection.getResponseMessage());
            return null;
        }

        // Antwort lesen
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        connection.disconnect();

        JsonNode rootNode = objectMapper.readTree(String.valueOf(response));

        JsonNode contentNode = rootNode.path("choices").get(0).path("message").path("content");

        return contentNode.asText().replace("\"", "");
    }

    private static void waitForAPI() {
        Date now = new Date();
        int timeout = 1000 * 60 / MAX_REQUESTS_PER_MINUTE;
        long diff = now.getTime() - lastRequest.getTime();
        if (diff < timeout) {
            try {
                Thread.sleep(diff);
            } catch (InterruptedException e) {
                //TODO mach was
                e.printStackTrace();
            }
        }
    }

    private static void requestDone() {
        lastRequest = new Date();
    }

}
