package repository_information;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.jgit.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CloneProxy extends AbstractProxy{



    public CloneProxy(String repositoryName, String owner, RepoCache cache) {
        super(repositoryName, owner, cache);
    }

    @Override
    public JsonNode getStructure() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        ArrayNode tree = mapper.createArrayNode();
        tree.addAll(getStructure(repoPath));
        node.set("tree", tree);

        return node.get("tree");
    }

    @Override
    String getSingleFile(String path) {
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
            System.err.println("Repository isn't cloned yet. Exptected at:" + path);
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
        deleteFolder(new File(String.valueOf(repoPath)));
    }

    @Override
    public String[] getOwnersRepos() {
        return gitAPI.getOwnersRepositories(owner);
    }

    private void deleteFolder(File folder) {
        try {
            FileUtils.delete(folder, FileUtils.RECURSIVE);
        } catch (IOException e) {
            // Deletion is not possible in some cases due to the operating system. It will be removed later again.
        }
    }

}
