package model;

/**
 * Represents a text file in a repository.
 */
public class TextFile extends File {

    /**
     * The content of the file.
     */
    private String content;

    /**
     * Creates a new text file.
     *
     * @param path to the file
     * @param content of the file
     */
    public TextFile(String path, String content) {
        super(path);
        this.content = content;
    }

    /**
     * Returns the content of the file.
     * @return content of the file
     */
    public String getContent() {
        return content;
    }
}
