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
    private JsonNode structure;
    private Map<String, String> files = new HashMap<>();

    @Override
    public JsonNode getStructure() {
        if (structure == null) {
            structure = proxy.getStructure();
        }
        return structure;

    }

    @Override
    public String getFile(String path, String url) {
        if (files.containsKey(url)) {
            return files.get(url);
        }

        //TODO: überlegen, wann cachen sinnvoll ist
        String file = proxy.getFile(path, url);
        files.put(url, file);

        return file;
    }

    public void setProxy(AbstractProxy proxy) {
        this.proxy = proxy;
    }
}
