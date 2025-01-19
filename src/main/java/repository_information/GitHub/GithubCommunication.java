package repository_information.GitHub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Repository;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.dircache.InvalidPathException;
import repository_information.GitMandatories;
import util.Json;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public final class GithubCommunication implements GitMandatories {

    private static final String GITHUB_GRAPHQL_URL = "https://api.github.com/graphql";
    private static final String GITHUB_REST_URL = "https://api.github.com";


    private static final String ACCESS_TOKEN = System.getenv("GitHub_API");
    private static String cursor = "";

    private static final GithubRateLimitCheck rateLimitCheck = GithubRateLimitCheck.getInstance();

    private GithubCommunication() {
        throw new UnsupportedOperationException("Utility-class shouldn't be instantiated.");
    }

    /**
     * Get the repositories from the GitHub API.
     *
     * @param amount the amount of repositories to get
     * @return a list of repositories as Json
     */
    public static List<Repository> getRepository(int amount) throws JsonProcessingException {
        String responseBody = "";

        System.out.println("token" + ACCESS_TOKEN);

        // GraphQL-Query
        //String query = "{ \"query\": \"{ search(query: \\\"stars:>100" + randomChar + "\\\", type: REPOSITORY, first: " + amount +") { edges { node { ... on Repository { name owner { login } } } } } }\" }";
        String query = "{ \"query\": \"{ search(query: \\\"stars:>100\\\", type: REPOSITORY, first: "+amount+", after: \\\"" + cursor + "\\\") { edges { node { ... on Repository { name owner { login } } } } pageInfo { hasNextPage endCursor } } }\" }";

        // HTTP-Client erstellen
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {            // HTTP-POST-Anfrage
            HttpPost httpPost = new HttpPost(GITHUB_GRAPHQL_URL);
            httpPost.setHeader("Authorization", "Bearer " + ACCESS_TOKEN);
            httpPost.setHeader("Accept", "application/json");

            // Query als Body hinzufügen
            StringEntity entity = new StringEntity(query, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);

            // Anfrage senden
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                // Antwort auslesen
                if (response.getCode() == 200) {
                    responseBody = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()))
                            .lines()
                            .collect(Collectors.joining("\n"));
                } else {
                    System.out.println("Error while getting Repository: " + response.getCode());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(Json.parseRepositories(responseBody));

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = objectMapper.readTree(responseBody);

        if (rootNode.get("data").
                get("search").
                get("pageInfo").
                get("hasNextPage").asBoolean()) {
            cursor = rootNode.get("data").get("search").get("pageInfo").get("endCursor").asText();

        }

        return Json.parseRepositories(rootNode);

    }


    public static JsonNode getStructure(String owner, String repo) {
        return getStructure(owner, repo, "HEAD");
    }

    public static JsonNode getStructure(String owner, String repo, String treeSha) {

        String response = null;

        String endpoint = String.format("/repos/%s/%s/git/trees/%s?recursive=1", owner, repo, treeSha);
        String apiUrl = GITHUB_REST_URL + endpoint;
        try {
            response = sendGetRequest(apiUrl);

        } catch (Exception e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();

        if (response == null) {
            return null;
        }

        JsonNode jsonResponse = null;
        try {
            jsonResponse = mapper.readTree(response);
        } catch (JsonProcessingException e) {
            return null;
        }

        return jsonResponse.get("tree");

    }

    private static String sendGetRequest(String apiUrl) throws IOException {
        // Verbindung öffnen
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // GET-Methode und Header setzen
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
        connection.setRequestProperty("Accept", "application/vnd.github+json");

        // Statuscode überprüfen
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            System.err.println("Error while getting GitHub response. Code: " + responseCode + " (" + apiUrl + ")");
            return null;
        }

        // Set the Rate Limit tracker.
        rateLimitCheck.setRateLimit(RateResource.valueOf(connection.getHeaderField("x-ratelimit-resource").toUpperCase()),
                Integer.parseInt(connection.getHeaderField("x-ratelimit-limit")),
                Integer.parseInt(connection.getHeaderField("x-ratelimit-remaining")),
                new Date(Integer.parseInt(connection.getHeaderField("x-ratelimit-reset")) * 1000L));

        // Antwort lesen
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public static String getFile(String urlString) {
        String response;
        try {
            response = sendGetRequest(urlString);
        } catch (IOException e) {
            System.out.println("Couldnt get file: " + urlString);
            return null;
        }
        if (response == null) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(response.toString());
        } catch (JsonProcessingException e) {
            System.out.println("Couldnt read answer: " + response.toString());
            return null;
        }
        return decodeBase64(jsonNode.get("content").asText());
    }

    private static String decodeBase64(String encodedContent) {
        byte[] decodedBytes = Base64.getMimeDecoder().decode(encodedContent);
        return new String(decodedBytes);
    }

    public static boolean cloneRepo(String owner, String repo, Path path) {

        String repoUrl = "https://github.com/" + owner + "/" + repo + ".git";

        if (path.toFile().exists()) {
            System.out.println("Repository already cloned to: " + path);
            return true;
        }

        try {
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider("Gollum225", ACCESS_TOKEN))
                    .setDirectory(path.toFile())
                    .call();

        } catch (GitAPIException | InvalidPathException e) {
            System.err.println("couldn't clone: " + e.getMessage());
            return false;
        }
        System.out.println("Repository cloned to: " + path);
        return true;
    }

}


