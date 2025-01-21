package controller;

import com.fasterxml.jackson.databind.JsonNode;
import repository_information.APIProxy;
import repository_information.AbstractProxy;
import repository_information.RepoFunctions;

import java.util.HashMap;
import java.util.Map;

public class RepoCache implements RepoFunctions {


    private final String repositoryName;
    private final String owner;
    private AbstractProxy proxy;

    public RepoCache(String repositoryName, String owner) {
        this.repositoryName = repositoryName;
        this.owner = owner;
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
    public JsonNode getStructure() {
        if (structure == null) {
            structure = proxy.getStructure();
        }
        return structure;

    }

    @Override
    public String getFile(String path) {
        if (files.containsKey(path)) {
            return files.get(path);
        }

        //TODO: überlegen, wann cachen sinnvoll ist
        String file = proxy.getFile(path);
        if (file == null || file.isEmpty()) {
            return null;
        }
        files.put(path, file);

        return file;
    }

    @Override
    public boolean changeToClone(String reason) {
        return proxy.changeToClone(reason);
    }

    /**
     * Sets the proxy to the given one. Useful, when the repo was cloned.
     * @param proxy new {@link AbstractProxy} to use.
     */
    public void setProxy(AbstractProxy proxy) {
        this.proxy = proxy;
    }
}
