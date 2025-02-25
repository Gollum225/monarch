package repository_information;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Path;

public interface GitMandatories {
    JsonNode getStructure(String owner, String repositoryName);

    String getFile(String path, String owner, String repositoryName);

    boolean cloneRepo(String owner, String repositoryName, Path repoPath);

    JsonNode generalInfo(String owner, String repositoryName);
}
