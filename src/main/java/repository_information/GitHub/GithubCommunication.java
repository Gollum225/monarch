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



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;

public final class GithubCommunication implements GitMandatories {

    private static GithubCommunication instance;

    private GithubCommunication() {
    }

    public static GithubCommunication getInstance() {
        if (instance == null) {
            instance = new GithubCommunication();
        }
        return instance;
    }

    private static final String GITHUB_GRAPHQL_URL = "https://api.github.com/graphql";
    private static final String GITHUB_REST_URL = "https://api.github.com";


    private static final String ACCESS_TOKEN = System.getenv("GitHub_API");

    private static final RateLimitMandatories rateLimitCheck = GithubRateLimitCheck.getInstance();

    private final ObjectMapper objectMapper = new ObjectMapper();


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

    public JsonNode getStructure(String owner, String repo, String treeSha) {

        String response = null;

        String endpoint = String.format("/repos/%s/%s/git/trees/%s?recursive=1", owner, repo, treeSha);
        String apiUrl = GITHUB_REST_URL + endpoint;
        try {
            response = sendGetRequest(apiUrl);

        } catch (IOException e) {
            return null;
        }

        if (response == null) {
            return null;
        }

        JsonNode jsonResponse = null;
        try {
            jsonResponse = objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            return null;
        }

        return jsonResponse.get("tree");

    }

    private String sendGetRequest(String apiUrl) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader in = null;

        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();

            // set GET-Methode and header
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            connection.setRequestProperty("Accept", "application/vnd.github+json");

            // check status code
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

            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } finally {
            if (in != null) {
                in.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    public String getFile(String path, String owner, String reponame) {
        String urlString = GITHUB_REST_URL + "/repos/" + owner + "/" + reponame + "/contents/" + encode(path);
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

        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(response.toString());
        } catch (JsonProcessingException e) {
            System.out.println("Couldnt read answer: " + response.toString());
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

    @Override
    public JsonNode generalInfo(String owner, String repo) {
        String apiUrl = GITHUB_REST_URL + "/repos/" + owner + "/" + repo;
        String response = null;
        try {
            response = sendGetRequest(apiUrl);
        } catch (IOException e) {
            e.printStackTrace(); //TODO
        }

        try {
            return objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
