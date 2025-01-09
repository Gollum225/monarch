package model;

public class TextFile {
    private String path;
    private String content;

    public TextFile(String path, String content) {
        this.path = path;
        this.content = content;
    }


    public String getContent() {
        return content;
    }

    public String getPath() {
        return path;
    }
}
