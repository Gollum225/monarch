package controller.rules;

import controller.Rule;
import controller.RuleType;
import exceptions.CloneProhibitedException;
import model.Repository;
import model.RepositoryAspectEval;
import util.CLIOutput;
import util.JsonKeywords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Checks the links in the readme file if they refer to a website with potential documentation.
 */
public class ReadReadmeLinks extends Rule {

    /**
     * Maximum number of sites to check per repository. More sides
     */
    private static final int MAX_SITES = 200;

    /**
     * Keywords to search for in the website content. These keywords are more important ones.
     */
    private static final List<String> MAIN_KEYWORDS = new ArrayList<>();

    /**
     * Keywords to search for in the website content. These keywords are less important ones.
     */
    private static final List<String> SIDE_KEYWORDS = new ArrayList<>();

    /**
     * List of concrete sites to exclude from the search.
     * E.g., <a href="https://github.com/owner/repo">github.com/owner/repo</a>
     */
    private final List<String> excludedConcreteSites = new ArrayList<>();

    /**
     * List of general sites to exclude from the search. E.g., YouTube.com
     */
    private final List<String> excludedGeneralSites = new ArrayList<>();

    private static int[] limits;

    // Regex from https://stackoverflow.com/questions/163360/regular-expression-to-match-urls-in-java
    private static final Pattern WEBSITE_PATTERN = Pattern.compile("\\b(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", Pattern.CASE_INSENSITIVE);


    public ReadReadmeLinks(Repository repository, int[] limits) {
        super(RuleType.MANDATORY, repository);
        if (ReadReadmeLinks.MAIN_KEYWORDS.isEmpty()) {
            MAIN_KEYWORDS.addAll(JsonKeywords.getSpecificKeywords("general-architecture"));
            MAIN_KEYWORDS.addAll(JsonKeywords.getSpecificKeywords("UML"));
        }
        addExcludedSites();
        if (ReadReadmeLinks.limits == null) {
            ReadReadmeLinks.limits = limits;
        }
    }

    @Override
    public RepositoryAspectEval execute() {
        String readme;

        try {
            readme = repository.getReadme();
        } catch (CloneProhibitedException e) {
            return new RepositoryAspectEval(e.getMessage());
        }
        if (readme == null) {
            return new RepositoryAspectEval("No readme found");
        }

        ArrayList<String> links = extractLinks(readme);
        links = filterImages(links);

        Set<String> set = links.stream().limit(MAX_SITES).collect(Collectors.toSet());


        if (set.isEmpty()) {
            return new RepositoryAspectEval("No links found in readme");
        }

        ConcurrentMap<String, Integer> concurrentMap = set.parallelStream().collect(Collectors.toConcurrentMap(
                key -> key,
                this::calculateWebsiteScore)
        );

        String maxKey = Collections.max(concurrentMap.entrySet(), Map.Entry.comparingByValue()).getKey();

        int maxScore = concurrentMap.get(maxKey);

        CLIOutput.ruleInfo(this.getClass().getSimpleName(), repository.getIdentifier(), "Best documentation page: " + maxKey + " with score: " + maxScore);

        maxScore = maxScore / Math.min(links.size(), MAX_SITES);

        return new RepositoryAspectEval(calculatePointsWithLimits(limits, maxScore));
    }

    /**
     * Gets content of a website.
     *
     * @param websiteUrl url of the website
     * @return content of the website
     * @throws IOException if an error occurs while fetching the website content
     */
    private static String fetchWebsiteContent(String websiteUrl) throws IOException {
        StringBuilder content = new StringBuilder();
        URL url = new URL(websiteUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        // Skip if the webpage is too large or not an HTML text.
        String contentType = connection.getContentType();
        if (contentType == null) {
            contentType = "text/html";
        }
        if ((connection.getContentLength() > 1000000 ) || !contentType.startsWith("text/html")) {
            if (!contentType.startsWith("application/pdf")) {
                return "";
            }
        }

        long startTime = System.currentTimeMillis();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (System.currentTimeMillis() - startTime > 10000) {
                    return "";
                }
                content.append(line).append("\n");
            }
        } catch (SocketTimeoutException e) {
            return "";
        } finally {
            connection.disconnect();
        }

        return content.toString();
    }

    /**
     * Extracts all links from a file.
     *
     * @param readme file to extract links from
     * @return list of links
     */
    private ArrayList<String> extractLinks(String readme) {
        ArrayList<String> links = new ArrayList<>();

        Matcher matcher = WEBSITE_PATTERN.matcher(readme);

        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            links.add(readme.substring(matchStart, matchEnd));
        }
        while (matcher.find()) {
            links.add(matcher.group());
        }
        CLIOutput.found(String.valueOf(links.size()), "links in readme of", repository.getIdentifier());
        return links;
    }

    /**
     * Counts the number out of a website's keywords and calculates a score based on that.
     *
     * @param websiteContent content of the website
     * @return score of the website
     */
    private int calculateWebsiteScore(String websiteContent) {

        if (excludedConcreteSites.contains(websiteContent) || excludedGeneralSites.stream().anyMatch(websiteContent::contains)) {
            return 0;
        }
        String content;
        try {
            content = fetchWebsiteContent(websiteContent);
        } catch (IOException e) {
            return 0;
        }

        if (content.isEmpty()) {
            return 0;
        }
        int score = 2 * countMultipleKeywordsSingleOccurrence(content, MAIN_KEYWORDS);
        if (score > 0) {
            score += countMultipleKeywordsSingleOccurrence(content, SIDE_KEYWORDS);

            // Triple the score of the website when the repository name is in the content, because the website
            // is more likely to contain documentation for this specific repository.
            if (contains(content, repository.getRepositoryName())) {
                score *= 3;
            }

            if (contains(content, repository.getOwner())) {
                score = score * 2;
            }

        }
        return score;
    }

    /**
     * Filter the list of links to exclude image links.
     * Save resources by not checking images.
     *
     * @param links list of links to filter
     * @return same list of links without image links
     */
    private ArrayList<String> filterImages(ArrayList<String> links) {

        return links.stream().filter(link ->
                !link.contains(".png") &&
                        !link.contains(".jpg") &&
                        !link.contains(".jpeg") &&
                        !link.contains(".gif") &&
                        !link.contains(".zip") &&
                        !link.contains(".svg")).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Adds site to exclude to the list of excluded sites.
     */
    private void addExcludedSites() {
        String repositoryUrl = "https://github.com/" + repository.getOwner() + "/" + repository.getRepositoryName();
        excludedConcreteSites.add(repositoryUrl);
        excludedConcreteSites.add(repositoryUrl + "/issues");
        excludedConcreteSites.add(repositoryUrl + "/pulls");
        excludedConcreteSites.add(repositoryUrl + "#readme");
        excludedConcreteSites.add(repositoryUrl + ".git");

        excludedGeneralSites.add("youtube.com");
        excludedGeneralSites.add("youtu.be");
        excludedGeneralSites.add(repositoryUrl + "/issues");

    }

}
