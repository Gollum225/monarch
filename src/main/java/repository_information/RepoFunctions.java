package repository_information;

import com.fasterxml.jackson.databind.JsonNode;

public interface RepoFunctions {

    JsonNode getStructure();
    String getFile(String path);
    boolean changeToClone(String reason);
}
