package controller.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.Rule;
import controller.RuleType;
import exceptions.CloneProhibitedException;
import model.Repository;
import model.RuleReturn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class LLMReadme extends Rule {

    // https://community.sambanova.ai/t/rate-limits/321
    private static final int MAX_REQUESTS_PER_MINUTE = 20;

    private final int MAX_POINTS = 5;

    static Date lastRequest = new Date();


    public LLMReadme(Repository repository) {
        super(RuleType.MANDATORY, repository);
    }

    @Override
    public RuleReturn execute() {

        String readme;
        try {
            readme = repository.getReadme();
        } catch (CloneProhibitedException e) {
            return new RuleReturn(e.getMessage(), repository.getIdentifier(), this.getClass().getSimpleName());
        }

        if (readme == null) {
            return new RuleReturn("No readme found", repository.getIdentifier(), this.getClass().getSimpleName());
        }

        String llmAnswer;

        try {
            llmAnswer = sendGetRequest(readme);
        } catch (IOException e) {
            return new RuleReturn("Error while getting LLM response", repository.getIdentifier(), this.getClass().getSimpleName());
        }

        if (llmAnswer == null) {
            return new RuleReturn("No answer from LLM", repository.getIdentifier(), this.getClass().getSimpleName());
        }


        for (int i = 0; i <= MAX_POINTS; i++) {
            if (llmAnswer.contains(String.valueOf(i))) {
                System.out.println("LLM gave: " + i + " points to " + repository.getIdentifier());
                return new RuleReturn(i);
            }
        }

        return new RuleReturn( "LLM gave an unexpected answer: " + llmAnswer, repository.getIdentifier(), this.getClass().getSimpleName());
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
                = " \"You are a machine to evaluate README files of Git Repositories. The user is looking for Repositories with architecture documentation. Give 0 to " + MAX_POINTS + " Points according to the likelihood of the existence of documentation for the repository. Answer only with the one number.\"";



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
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
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
        int timeout = 1000 * 60 / MAX_REQUESTS_PER_MINUTE + 500;
        long diff = now.getTime() - lastRequest.getTime();
        if (diff < timeout) {
            try {
                Thread.sleep(diff);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void requestDone() {
        lastRequest = new Date();
    }

}
