package repository_information;

import com.fasterxml.jackson.databind.JsonNode;
import exceptions.CloneProhibitedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class RepoCache implements RepoFunctions {


    private AbstractProxy proxy;
    private JsonNode generalInfo;

    public RepoCache(String repositoryName, String owner) {
        this.proxy = new APIProxy(repositoryName, owner, this);
    }

    //Cached information:
    /**
     * The structure of the repository, as requested in {@link RepoCache#getStructure()}.
     */
    private JsonNode structure;

    /**
     * The path of the file based on the repository root mapped to the content.
     */
    private Map<String, String> files = new HashMap<>();

    @Override
    public JsonNode getStructure() throws CloneProhibitedException {
        if (structure == null) {
            structure = proxy.getStructure();
        }
        return structure;

    }

    @Override
    public Map<String, String> getFiles(List<String> paths) throws CloneProhibitedException {
        ArrayList<String> notCached = new ArrayList<>();
        Map<String, String> results = new HashMap<>();

        for (String path : paths) {
            if (files.containsKey(path)) {
                results.put(path, files.get(path));
            } else {
                notCached.add(path);
            }
        }
        Map<String, String> requestResults = proxy.getFiles(notCached);
        files.putAll(requestResults);
        results.putAll(requestResults);

        return results;
    }

    @Override
    public boolean changeToClone(String reason) throws CloneProhibitedException {
        return proxy.changeToClone(reason);
    }

    @Override
    public JsonNode generalInfo() {
        if (generalInfo == null) {
            generalInfo = proxy.generalInfo();
        }
        return generalInfo;
    }

    @Override
    public void finish() {
        structure = null;
        files.clear();
        proxy.finish();
    }

    @Override
    public String[] getOwnersRepos() {
        return proxy.getOwnersRepos();
    }

    /**
     * Sets the proxy to the given one. Useful, when the repo was cloned.
     * @param proxy new {@link AbstractProxy} to use.
     */
    public void setProxy(AbstractProxy proxy) {
        this.proxy = proxy;
    }
}
