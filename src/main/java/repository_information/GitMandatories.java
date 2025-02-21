package repository_information;

import com.fasterxml.jackson.databind.JsonNode;

public interface GitMandatories {
    JsonNode getStructure(String owner, String repositoryName);

    String getFile(String path, String owner, String repositoryName);
}
