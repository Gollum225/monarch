package model;

public class TextFile extends File {

    private String content;

    public TextFile(String path, String content) {
        super(path);
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
