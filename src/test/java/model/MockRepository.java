package model;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.util.ArrayList;
import java.util.List;

public class MockRepository extends Repository {

    public MockRepository(String repositoryName, String owner) {
        super(repositoryName, owner);
    }

    public List<TextFile> getTextfiles() {
        TextFile file1 = new TextFile("file1", "architecture");
        List<TextFile> list = new ArrayList<>();
        list.add(file1);
        return list;
    }

    @Override
    public JsonNode getStructure() {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();

        array.add(createFolder("src"));
        array.add(createFolder("documentation"));
        array.add(createFolder("readme"));

        return array;
    }

    private JsonNode createFolder(String path) {
        return JsonNodeFactory.instance.objectNode()
                .put("type", "tree")
                .put("path", path);
    }

    @Override
    public int getQualityMetrics() {
        return 0;
    }
}
