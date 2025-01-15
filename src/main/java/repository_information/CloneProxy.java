package repository_information;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controller.RepoCache;
import org.eclipse.jgit.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static util.Globals.CLONED_REPOS_PATH;

public class CloneProxy extends AbstractProxy{

    private final String repoPath = CLONED_REPOS_PATH + "/" + owner + "/" + repositoryName;


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
        System.out.println("node: " + node);

        return node;
    }

    @Override
    public String getFile(String path, String url) {
        path = repoPath + "/" + path;
        try {
            return Files.readString(Path.of(path));

        } catch (IOException e) {
            System.err.println("Local file not found: " + path);
            return "";
        }
    }

    private List<JsonNode> getStructure(String path) {
        File folder = new File(path);
        //String path2 = path.replaceAll("\\\\", "/");
        List<JsonNode> files = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

        System.out.println("files in folder: " + folder.listFiles());
        if (folder.listFiles() == null) {
            System.err.println("Repository isn't cloned yet. Exptected at:" + path);
            return new ArrayList<>();
        }

        System.out.println("file number: " + folder.listFiles().length);
        for (File file : folder.listFiles()) {

            if (file.isDirectory()) {
                ObjectNode node = mapper.createObjectNode();
                int length = path.length();
                node.put("path", file.getAbsolutePath().substring(length + 1));
                node.put("type", "tree");
                List<JsonNode> newFiles = new ArrayList<>(getStructure(file.getAbsolutePath()));
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
        //deleteFolder(new File(path));
    }

    private void deleteFolder(File folder) {
        try {
            FileUtils.delete(folder, FileUtils.RECURSIVE);
        } catch (IOException e) {
            // Already deleted.
        }
    }

}
