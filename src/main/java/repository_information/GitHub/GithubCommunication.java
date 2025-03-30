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

import static util.Globals.DEFAULT_NUMBER_OF_STAR;

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

    private static int page = 0;

    private int maxNumberOfRepos = -1;

    HttpClient client = HttpClient.newHttpClient();


    /**
     * Gets 10 random repositories from the GitHub API.
     * @return List of repositories
     * @throws JsonProcessingException if the response couldn't be parsed
     */
    public List<Repository> getTenRepository() throws JsonProcessingException {
        return getTenRepository(null, DEFAULT_NUMBER_OF_STAR);

    }

    public List<Repository> getTenRepository(String searchTerm, int starAmount) throws JsonProcessingException {
        if (!GithubRateLimitCheck.getInstance().checkHardRateLimit(RateResource.SEARCH)) {
            System.out.println("Couldn't get new repositories. Rate limit reached.");
            return null;
        }

        if (searchTerm == null) {
            page = (int) (Math.random() * 101);
            searchTerm = createRandomSearchString();
        } else {
            page ++;
            if (maxNumberOfRepos >= 0 && page * 10 - 10 > maxNumberOfRepos) {
                System.out.println("Max number of repositories reached");
                return new ArrayList<>();
            }
        }

        String responseBody;
        String apiUrl = GITHUB_REST_URL + "/search/repositories?q=" + searchTerm + "+stars" + encode(":>") + starAmount + "&per_page=10" + "&page=" + page;

        try {
            responseBody = sendGetRequest(URI.create(apiUrl));
        } catch (IOException | InterruptedException e) {
            return null;
        }
        if (responseBody == null) {
            return null;
        }
        JsonNode rootNode = objectMapper.readTree(responseBody);

        if (maxNumberOfRepos < 0 ) {
            maxNumberOfRepos = rootNode.get("total_count").asInt();
        }

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

    private String sendGetRequest(URI apiUrl) throws IOException, InterruptedException {
        return sendGetRequest(apiUrl, false);
    }


        /**
         * Send a GET request to the GitHub API.
         * Updates the rate limit tracker.
         *
         * @param apiUrl to send the request to
         * @return the response
         * @throws IOException if the request couldn't be sent
         */
    private String sendGetRequest(URI apiUrl, boolean suppressMessages) throws IOException, InterruptedException {

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
                if (!suppressMessages) {
                    System.err.println("Error while getting GitHub response. Code: " + response.statusCode() + " (" + apiUrl + ")");
                }
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

    @Override
    public boolean checkRepositoryExistence(String owner, String repo) {
        String apiUrl = "https://github.com" + "/" + owner + "/" + repo;
        String response;
        try {
            response = sendGetRequest(URI.create(apiUrl), true);
        } catch (IOException | InterruptedException e) {
            return false;
        }
        return response != null;
    }

    @Override
    public String[] getOwnersRepositories(String owner) {
        String[] result = getOwnersRepositories(owner, true);
        if (result.length == 0) {
            result = getOwnersRepositories(owner, false);
        }
        return result;
    }

    public String[] getOwnersRepositories(String owner, boolean isOrganization) {
        String query = """
            {
                organization(login: "%s") {
                    repositories(first: 100, privacy: PUBLIC) {
                        nodes {
                            name
                        }
                    }
                }
            }
        """.formatted(owner);

        String response;
        try {
            response = sendGraphQLRequest(query);
        } catch (IOException | InterruptedException e) {
            return new String[0];
        }

        if (response != null) {
            try {
                return parseRepositoryNames(response);
            } catch (JsonProcessingException e) {
                return new String[0];
            }
        } else {
            return new String[0];
        }
    }

    public int getMaxResults(String searchTerm, int numberOfStars) throws JsonProcessingException {
        String responseBody;

        String apiUrl;
        String path  = GITHUB_REST_URL + "/search/repositories?q=";
        if (searchTerm != null && numberOfStars < 0) {
             apiUrl = path + encode( searchTerm) + "&per_page=1";
        } else if (searchTerm != null && numberOfStars > 0) {
            apiUrl = path + encode(searchTerm) + "+stars" + encode(":>") + numberOfStars +"&per_page=1";
        } else if (searchTerm == null && numberOfStars > 0) {
            apiUrl = path + "+stars" + encode(":>") + numberOfStars + "&per_page=1";
        } else {
            return 0;
        }

        try {
            responseBody = sendGetRequest(URI.create(apiUrl));
        } catch (IOException | InterruptedException e) {
            return 0;
        }
        if (responseBody == null) {
            return 0;
        }
        JsonNode rootNode = objectMapper.readTree(responseBody);

        return rootNode.get("total_count").asInt();
    }

    private String sendGraphQLRequest(String query) throws IOException, InterruptedException {
        String jsonQuery = String.format("{\"query\": \"%s\"}", query.replace("\"", "\\\"").replace("\n", " "));

        URI uri = URI.create(GITHUB_GRAPHQL_URL);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "Java-HttpClient")
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonQuery, StandardCharsets.UTF_8));

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Error while getting GitHub GraphQL response. Code: " + response.statusCode());
            return null;
        }

        return response.body();
    }

    private String[] parseRepositoryNames(String jsonResponse) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        JsonNode repositories = rootNode.path("data").path("organization").path("repositories").path("nodes");

        if (!repositories.isArray()) {
            return new String[0];
        }
        String[] repoNames = new String[repositories.size()];

        for (int i = 0; i < repositories.size(); i++) {
            repoNames[i] = repositories.get(i).get("name").asText();
        }
        return repoNames;
    }

}
