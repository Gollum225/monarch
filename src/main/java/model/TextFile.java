package model;

public class TextFile extends File {

    private String content;

    public TextFile(String url, String path, String content) {
        super(url, path);
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
