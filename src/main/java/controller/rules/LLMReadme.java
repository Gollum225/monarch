package controller.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.Rule;
import controller.RuleType;
import exceptions.CloneProhibitedException;
import model.Repository;
import model.RepositoryAspectEval;
import util.CLIOutput;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class LLMReadme extends Rule {

    // https://docs.sambanova.ai/cloud/docs/get-started/rate-limits
    private static final int MAX_REQUESTS_PER_MINUTE = 30;

    private static int maxPoints = -1;

    static Date lastRequest = new Date();

    /**
     * The access token for the API.
     * It is stored in the environment variables.
     * When set manually, it will be used instead.
     */
    private static String ACCESS_TOKEN;



    public LLMReadme(Repository repository, int maxPoints) {
        super(RuleType.MANDATORY, repository);
        if (LLMReadme.maxPoints < 0) {
            LLMReadme.maxPoints = maxPoints;
        }
        if (ACCESS_TOKEN == null) {
            ACCESS_TOKEN = System.getenv("SambaNova_API");
        }
    }

    @Override
    public RepositoryAspectEval execute() {

        String readme;
        try {
            readme = repository.getReadme();
        } catch (CloneProhibitedException cpe) {
            // The readme has caused the repository to be cloned, which it is not allowed to do.
            return new RepositoryAspectEval(cpe.getMessage());
        }

        String llmAnswer;

        if (readme == null) {
            return new RepositoryAspectEval("No readme found");
        }


        try {
            llmAnswer = sendGetRequest(readme);
        } catch (IOException e) {
            return new RepositoryAspectEval("Error while getting LLM response");
        }

        if (llmAnswer == null) {
            return new RepositoryAspectEval("No answer from LLM");
        }


        for (int i = 0; i <= maxPoints; i++) {
            if (llmAnswer.contains(String.valueOf(i))) {
                CLIOutput.ruleInfo(this.getClass().getSimpleName(), repository.getIdentifier(), "rewarded " + i + " points");
                return new RepositoryAspectEval(i);
            }
        }

        return new RepositoryAspectEval( "LLM gave an unexpected answer: " + llmAnswer);
    }

    private String sendGetRequest(String readme) throws IOException {
        // open connection
        URL url = new URL("https://api.sambanova.ai/v1/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // set GET-methode and Header
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        ObjectMapper objectMapper = new ObjectMapper();
        String formattedReadme = objectMapper.writeValueAsString(readme);

        String jsonInput = getJsonInput(formattedReadme);


        waitForAPI();

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        requestDone();
        // check status-code
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            System.err.println("Error while getting Samba Nova response. Code: " + responseCode +" "+ connection.getResponseMessage());
            return null;
        }

        // read answer
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

    private String getJsonInput(String formattedReadme) {
        String roleContent
                = " \"You are a machine to evaluate README files of Git Repositories. The user is looking for Repositories with architecture documentation. Give 0 to " + maxPoints + " points according to the likelihood of the existence of documentation for the repository. If no hints are given give 0 points. Answer only with the one number.\"";


        return """
                {
                    "stream": false,
                    "max_tokens": 1,
                    "model": "Meta-Llama-3.3-70B-Instruct",
                    "messages": [
                        {
                            "role": "system",
                            "content": """ + roleContent + """
                        },
                        {
                            "role": "user",
                            "content": """ + formattedReadme + """
                        }
                    ]
                }
            """;
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
