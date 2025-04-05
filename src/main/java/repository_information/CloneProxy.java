package repository_information;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.APIOverloaded;
import exceptions.CloneProhibitedException;
import org.eclipse.jgit.util.FileUtils;
import repository_information.GitHub.GithubCommunication;
import repository_information.GitHub.GithubRateLimitCheck;
import repository_information.GitHub.RateResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.Globals.CLONED_REPOS_PATH;
import static util.Globals.CLONE_THRESHOLD;
import static util.Globals.MAX_CLONE_SIZE;

public class CloneProxy implements RepoFunctions{


    public CloneProxy(String repositoryName, String owner) {
        this.apiProxy = new APIProxy(repositoryName, owner);
        this.repositoryName = repositoryName;
        this.owner = owner;
        repoPath = CLONED_REPOS_PATH.resolve(owner).resolve(repositoryName);
    }

    /**
     * If the repository is too large to clone. The limit is set in {@link util.Globals#MAX_CLONE_SIZE}.
     */
    boolean cloneProhibited = false;


    private APIProxy apiProxy;

    private boolean isCloned = false;

    final String repositoryName;
    final String owner;
    final RateLimitMandatories rateLimitMandatories = GithubRateLimitCheck.getInstance();
    final GitMandatories gitAPI = GithubCommunication.getInstance();

    final Path repoPath;

    private int generalInfoSize = -1;



    @Override
    public JsonNode getStructure() throws CloneProhibitedException {
        JsonNode structure = null;

        if (!isCloned) {
            try {
                structure = apiProxy.getStructure();
            } catch (APIOverloaded e) {
                changeToClone();
            }
        }
        if (structure == null) {
            changeToClone("couldn't get structure");
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            ArrayNode tree = mapper.createArrayNode();
            tree.addAll(getStructure(repoPath));
            node.set("tree", tree);
            structure = node.get("tree");
        } else if (structure.size() > CLONE_THRESHOLD) {
            // If the structure is too large: try to clone, but don't throw an exception, because it is not the
            // fault of the caller.
            try {
                changeToClone("large structure: " + structure.size() + " elements");
            } catch (CloneProhibitedException e) {
                //do nothing
            }
        }

        return structure;
    }

    @Override
    public Map<String, String> getFiles(List<String> paths) throws CloneProhibitedException {
        if (!isCloned) {
            try {
                return apiProxy.getFiles(paths);
            } catch (APIOverloaded e) {
                changeToClone();
            }
        }
        Map<String, String> results = new HashMap<>();

        for (String path : paths) {
            results.put(path, getSingleFile(path));
        }
        return results;
    }


    @Override
    public JsonNode generalInfo() {
        JsonNode generalInfo = apiProxy.generalInfo();
        generalInfoSize = generalInfo.get("size").asInt();
        return generalInfo;
    }

    private String getSingleFile(String path) {
        Path filePath = repoPath.resolve(path);
        try {
            return Files.readString(filePath);

        } catch (IOException e) {
            System.err.println("Local file not found: " + filePath);
            return "";
        }
    }

    private List<JsonNode> getStructure(Path repoPath) {
        String path = repoPath.toString();
        File folder = new File(path);
        List<JsonNode> files = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        if (folder.listFiles() == null) {
            System.err.println("Repository isn't cloned yet. Expected at:" + path);
            return new ArrayList<>();
        }

        File[] allFiles = folder.listFiles();
        if (allFiles == null) {
            return files;
        }

        for (File file : allFiles) {

            if (file.isDirectory()) {
                ObjectNode node = mapper.createObjectNode();
                int length = path.length();
                node.put("path", file.getAbsolutePath().substring(length + 1));
                node.put("type", "tree");
                List<JsonNode> newFiles = new ArrayList<>(getStructure(Path.of(file.getAbsolutePath())));
                if (!newFiles.isEmpty()) {
                    files.addAll(newFiles);
                }
                files.add(node);
            } else {
                ObjectNode node = mapper.createObjectNode();
                node.put("path", file.getAbsolutePath());
                node.put("type", "blob");

                files.add(node);
            }
        }
        return files;
    }

    @Override
    public void finish() {
        apiProxy = null;
        deleteFolder(new File(String.valueOf(repoPath)));
    }

    @Override
    public String[] getOwnersRepos() {
        return apiProxy.getOwnersRepos();
    }

    private void deleteFolder(File folder) {
        try {
            FileUtils.delete(folder, FileUtils.RECURSIVE);
        } catch (IOException e) {
            // Deletion is not possible in some cases due to the operating system. It will be removed later again.
        }
    }

    /**
     * Checks the rate limit if the call is connected to a specific resource.
     *
     * @param resource the resource to check the rate limit for.
     * @param critical if the request has a higher priority to be sent per API.
     * @return if the request can be made per API.
     */
    private boolean checkRateLimit(RateResource resource, boolean critical) {
        if (rateLimitMandatories.checkMildRateLimit(resource)) {
            return true;
        } else if (critical) {
            return rateLimitMandatories.checkHardRateLimit(resource);
        }
        return false;
    }

    private boolean cloneRepo() throws CloneProhibitedException {

        // Wait if the rate limit is reached.
        if (!checkRateLimit(RateResource.CORE, false)) {
            while (!checkRateLimit(RateResource.CORE, true)) {
                try {
                    Thread.sleep(rateLimitMandatories.getTimeTillReset(RateResource.GRAPHQL));
                } catch (InterruptedException e) {
                    //Do nothing, wait for the next loop.
                }
            }
        }


        if (cloneProhibited) {
            System.out.println("\u001B[31m" + "Couldn't clone " + repositoryName + "of: " + owner + "\u001B[0m");
            throw new CloneProhibitedException();
        } else if(getRepoSize() < 0) {
            System.out.println("\u001B[31m" + "Couldn't clone " + repositoryName + "of: " + owner + " due to the unknown size" + "\u001B[0m");
            throw new CloneProhibitedException();
        } else if (getRepoSize() > MAX_CLONE_SIZE) {
            cloneProhibited = true;
            System.out.println("\u001B[31m" + "Couldn't clone " + repositoryName + "of: " + owner + " due to the large size" + "\u001B[0m");
            throw new CloneProhibitedException();
        }
        isCloned = gitAPI.cloneRepo(owner, repositoryName, repoPath);
        return isCloned;
    }

    boolean changeToClone() throws CloneProhibitedException {
        System.out.println("\u001B[34m" + "Trying to clone: " + repositoryName + " of owner: " + owner + "\u001B[0m");

        return cloneRepo();
    }

    public boolean changeToClone(String reason) throws CloneProhibitedException {
        System.out.println("\u001B[34m" + "Trying to clone: " + repositoryName + " of owner: " + owner + " because of " + reason + "\u001B[0m");
        return cloneRepo();
    }

    public int getRepoSize() {
        if (generalInfoSize < 0) {
            generalInfo();
        }

        return generalInfoSize;
    }

}
