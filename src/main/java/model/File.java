package model;

/**
 * Represents a file in a repository.
 */
public class File {

    /**
     * The path to the file.
     */
    private final String path;

    /**
     * Creates a new file.
     *
     * @param path to the file
     */
    public File(String path) {
        this.path = path;
    }

    /**
     * Returns the path to the file.
     *
     * @return path to the file
     */
    public String getPath() {
        return path;
    }


}
