package model;

public class TextFile {
    private String name;
    private String path;
    private String content;

    public TextFile(String name, String path, String content) {
        this.name = name;
        this.path = path;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getPath() {
        return path;
    }
}
