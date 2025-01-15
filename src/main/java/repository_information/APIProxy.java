package repository_information;

import com.fasterxml.jackson.databind.JsonNode;
import controller.RepoCache;

public class APIProxy extends AbstractProxy{


    public APIProxy(String repositoryName, String owner, RepoCache cache) {
        super(repositoryName, owner, cache);
    }

    @Override
    public JsonNode getStructure() {
        JsonNode structure = GithubCommunication.getStructure(owner, repositoryName);
        if (structure != null && structure.size() > 1500) {
            changeToClone();
        }
        System.out.println(structure.size());
        return GithubCommunication.getStructure(owner, repositoryName);
    }

    @Override
    public String getFile(String path, String url) {
        return GithubCommunication.getFile(url);
    }

    @Override
    public void finish() {
        //Nothing to do here
    }
}
