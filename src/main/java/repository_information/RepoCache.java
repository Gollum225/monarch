package repository_information;

import com.fasterxml.jackson.databind.JsonNode;
import exceptions.CloneProhibitedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class RepoCache implements RepoFunctions {


    private final CloneProxy cloneProxy;
    private JsonNode generalInfo;

    public RepoCache(String repositoryName, String owner) {
        this.cloneProxy = new CloneProxy(repositoryName, owner);
    }

    //Cached information:
    /**
     * The structure of the repository, as requested in {@link RepoCache#getStructure()}.
     */
    private JsonNode structure;

    /**
     * The path of the file based on the repository root as key mapped to the content of the file as value.
     */
    private final Map<String, String> filesAtPath = new HashMap<>();

    @Override
    public JsonNode getStructure() throws CloneProhibitedException {
        if (structure == null) {
            structure = cloneProxy.getStructure();
        }
        return structure;

    }

    @Override
    public Map<String, String> getFiles(List<String> paths) throws CloneProhibitedException {
        ArrayList<String> notCached = new ArrayList<>();
        Map<String, String> results = new HashMap<>();

        for (String path : paths) {
            if (filesAtPath.containsKey(path)) {
                results.put(path, filesAtPath.get(path));
            } else {
                notCached.add(path);
            }
        }
        Map<String, String> requestResults = cloneProxy.getFiles(notCached);
        filesAtPath.putAll(requestResults);
        results.putAll(requestResults);

        return results;
    }

    @Override
    public boolean changeToClone(String reason) throws CloneProhibitedException {
        return cloneProxy.changeToClone(reason);
    }

    @Override
    public JsonNode generalInfo() {
        if (generalInfo == null) {
            generalInfo = cloneProxy.generalInfo();
        }
        return generalInfo;
    }

    @Override
    public void finish() {
        structure = null;
        filesAtPath.clear();
        cloneProxy.finish();
    }

    @Override
    public String[] getOwnersRepos() {
        return cloneProxy.getOwnersRepos();
    }

}
