package repository_information;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Path;

public interface GitMandatories {

    /**
     * Get the structure of the repository. Files and directories.
     *
     * @param owner of the repository
     * @param repositoryName of the repository
     * @return JsonTree with the structure.
     */
    JsonNode getStructure(String owner, String repositoryName);

    /**
     * Get one specific file from the repository.
     *
     * @param path of the file
     * @param owner of the associated repository
     * @param repositoryName of the associated repository
     * @return the content of the file.
     */
    String getFile(String path, String owner, String repositoryName);

    /**
     * Clones the reopsitory.
     *
     * @param owner of the repository
     * @param repositoryName of the repository
     * @param repoPath the path to clone the repository to
     * @return true if the repository was cloned successfully.
     */
    boolean cloneRepo(String owner, String repositoryName, Path repoPath);

    /**
     * Gets general information about the repository.
     *
     * @param owner of the repository
     * @param repositoryName of the repository
     * @return a JsonNode with the general information.
     */
    JsonNode generalInfo(String owner, String repositoryName);
}
