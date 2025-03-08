package repository_information.GitHub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Repository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.dircache.InvalidPathException;
import repository_information.GitMandatories;
import repository_information.RateLimitMandatories;
import util.Json;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class GithubCommunication implements GitMandatories {

    /**
     * The access token for the GitHub API. It is stored in the environment variables.
     * When set manually, it will be used instead.
     */
    private static String ACCESS_TOKEN;


    /**
     * Singleton instance.
     */
    private static GithubCommunication instance;

    private GithubCommunication() {

        if (ACCESS_TOKEN == null) {
            ACCESS_TOKEN = System.getenv("GitHub_API");
        }
    }

    /**
     * Singleton instance.
     * @return the instance
     */
    public static GithubCommunication getInstance() {
        if (instance == null) {
            instance = new GithubCommunication();
        }
        return instance;
    }

    /**
     * The URL for the GitHub GraphQL API.
     */
    private static final String GITHUB_GRAPHQL_URL = "https://api.github.com/graphql";

    /**
     * The URL for the GitHub REST API.
     */
    private static final String GITHUB_REST_URL = "https://api.github.com";


    private static final RateLimitMandatories rateLimitCheck = GithubRateLimitCheck.getInstance();

    private final ObjectMapper objectMapper = new ObjectMapper();

    HttpClient client = HttpClient.newHttpClient();

    /**
     * Gets 10 random repositories from the GitHub API.
     * @return List of repositories
     * @throws JsonProcessingException if the response couldn't be parsed
     */
    public List<Repository> getTenRepository() throws JsonProcessingException {
        int starAmount = 100;

        int randomPage = (int) (Math.random() * 100);
        String randomSearchString = createRandomSearchString();

        String responseBody = "";
        String apiUrl = GITHUB_REST_URL + "/search/repositories?q=" + randomSearchString + "+stars:>" + starAmount + "&per_page=10" + "&page=" + randomPage;

        try {
            responseBody = sendGetRequest(apiUrl);
        } catch (IOException e) {
            responseBody = sendGetRequest(URI.create(apiUrl));
        } catch (IOException | InterruptedException e) {
            return null;
        }
        if (responseBody == null) {
            return null;
        }
        JsonNode rootNode = objectMapper.readTree(responseBody);
        return Json.parseRestRepositories(rootNode);

    }

    private String createRandomSearchString() {
        Random random = new Random();
        String randomSearchString = String.valueOf((char) (Math.random() * 26 + 'a'));
        if (random.nextBoolean()) {
            randomSearchString += (char) (Math.random() * 26 + 'a');
            if (random.nextBoolean()) {
                randomSearchString += (char) (Math.random() * 26 + 'a');
            }
        }
        return randomSearchString;
    }


    @Override
    public JsonNode getStructure(String owner, String repo) {
        return getStructure(owner, repo, "HEAD");
    }

    /**
     * Gets the structure of the repository, with a custom treeSha.
     *
     * @param owner of the repository
     * @param repo of the repository
     * @param treeSha to get the structure from
     * @return the structure of the repository
     */
    public JsonNode getStructure(String owner, String repo, String treeSha) {

        String response;

        String endpoint = String.format("/repos/%s/%s/git/trees/%s?recursive=1", owner, repo, treeSha);
        String apiUrl = GITHUB_REST_URL + endpoint;
        try {
            response = sendGetRequest(URI.create(apiUrl));

        } catch (IOException | InterruptedException e) {
            return null;
        }

        if (response == null) {
            return null;
        }

        JsonNode jsonResponse;
        try {
            jsonResponse = objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            return null;
        }

        return jsonResponse.get("tree");

    }

    /**
     * Send a GET request to the GitHub API.
     * Updates the rate limit tracker.
     *
     * @param apiUrl to send the request to
     * @return the response
     * @throws IOException if the request couldn't be sent
     */
    private String sendGetRequest(URI apiUrl) throws IOException, InterruptedException {

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(apiUrl)
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "Java-HttpClient");

            if (ACCESS_TOKEN != null) {
                //connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
                requestBuilder.header("Authorization", "Bearer " + ACCESS_TOKEN);
            }

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Error while getting GitHub response. Code: " + response.statusCode() + " (" + apiUrl + ")");
                return null;
            }

            // Set the Rate Limit tracker.
            Optional<String> rateResource = response.headers().firstValue("x-ratelimit-resource");
            Optional<String> rateLimit = response.headers().firstValue("x-ratelimit-limit");
            Optional<String> rateRemaining = response.headers().firstValue("x-ratelimit-remaining");
            Optional<String> rateReset = response.headers().firstValue("x-ratelimit-reset");

            if (rateResource.isPresent() && rateLimit.isPresent() && rateRemaining.isPresent() && rateReset.isPresent()) {
                rateLimitCheck.setRateLimit(
                        RateResource.valueOf(rateResource.get().toUpperCase()),
                        Integer.parseInt(rateLimit.get()),
                        Integer.parseInt(rateRemaining.get()),
                        new Date(Long.parseLong(rateReset.get()) * 1000L)  // Convert reset time to milliseconds
                );
            }
           return response.body();

    }

    @Override
    public String getFile(String path, String owner, String repoName) {
        String urlString = GITHUB_REST_URL + "/repos/" + owner + "/" + repoName + "/contents/" + encode(path);
        String response;
        try {
            response = sendGetRequest(URI.create(urlString));
        } catch (IOException | InterruptedException e) {
            System.out.println("Couldn't get file: " + urlString);
            return null;
        }
        if (response == null) {
            return null;
        }

        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            System.out.println("Couldn't read answer: " + response);
            return null;
        }
        return decodeBase64(jsonNode.get("content").asText());
    }

    private String decodeBase64(String encodedContent) {
        byte[] decodedBytes = Base64.getMimeDecoder().decode(encodedContent);
        return new String(decodedBytes);
    }

    private String encode(String content) {
        return URLEncoder.encode(content, StandardCharsets.UTF_8).replace("+", "%20");
    }

    @Override
    public boolean cloneRepo(String owner, String repo, Path path) {

        System.out.println("Cloning: " + owner + "/" + repo);
        String repoUrl = "https://github.com/" + owner + "/" + repo + ".git";

        if (path.toFile().exists()) {
            System.out.println("Repository already cloned to: " + path);
            return true;
        }

        try {
            if (ACCESS_TOKEN == null) {
                Git.cloneRepository()
                        .setURI(repoUrl)
                        .setDirectory(path.toFile())
                        .call();
            } else {
                Git.cloneRepository()
                        .setURI(repoUrl)
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider("git", ACCESS_TOKEN))
                        .setDirectory(path.toFile())
                        .call();
            }


        } catch (GitAPIException | InvalidPathException e) {
            System.err.println("couldn't clone: " + e.getMessage());
            return false;
        }
        System.out.println("Repository cloned to: " + path);
        return true;
    }

    @Override
    public JsonNode generalInfo(String owner, String repo) {
        String apiUrl = GITHUB_REST_URL + "/repos/" + owner + "/" + repo;
        String response;
        try {
            response = sendGetRequest(URI.create(apiUrl));
        } catch (IOException | InterruptedException e) {
            return null;
        }
        if (response == null) {
            return null;
        }
        try {
            return objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
