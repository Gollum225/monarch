package repository_information.GitHub;

/**
 * Enum to represent the different rate limits of the GitHub API.
 * For more information see: <a href="https://docs.github.com/en/rest/rate-limit/rate-limit?apiVersion=2022-11-28#get-rate-limit-status-for-the-authenticated-user">GitHub rate limit status</a>.
 * Javadoc comments are taken from the GitHub documentation.
 */
public enum RateResource {

    /**
     * "The core object provides your rate limit status for all non-search-related resources in the REST API."
     */
    CORE,

    /**
     * "The search object provides your rate limit status for the REST API for searching (excluding code searches)."
     */
    SEARCH,

    /**
     * "The code_search object provides your rate limit status for the REST API for searching code."
     */
    CODE_SEARCH,

    /**
     * "The graphql object provides your rate limit status for the GraphQL API."
     */
    GRAPHQL


}
