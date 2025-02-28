package model;
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
}
