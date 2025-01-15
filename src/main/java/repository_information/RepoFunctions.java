package repository_information;

import com.fasterxml.jackson.databind.JsonNode;

public interface RepoFunctions {

    public JsonNode getStructure();
    public String getFile(String path, String url);
}
