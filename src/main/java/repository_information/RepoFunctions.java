package repository_information;

import com.fasterxml.jackson.databind.JsonNode;
import exceptions.CloneProhibitedException;

import java.util.List;
import java.util.Map;

public interface RepoFunctions {

    JsonNode getStructure() throws CloneProhibitedException;

    /**
     * Get the files from the repository.
     * @param paths the paths of the files to get.
     * @return a map with the path as key and the content as value.
     * @throws CloneProhibitedException if the repository is too large to clone.
     */
    Map<String, String> getFiles(List<String> paths) throws CloneProhibitedException;
    boolean changeToClone(String reason) throws CloneProhibitedException;
    JsonNode generalInfo();
}
