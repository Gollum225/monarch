package util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Globals {
    public static Path RESOURCE_PATH = Paths.get("").toAbsolutePath().resolve(Paths.get("src/main/resources"));
    public static Path CLONED_REPOS_PATH = (Paths.get("").toAbsolutePath()).resolve(Paths.get("src/main/resources/cloned_repos"));

    /**
     * If the repository has more than {@link #CLONE_THRESHOLD} elements, it will be cloned.
     */
    public static int CLONE_THRESHOLD = 2500;

    /**
     * If the request limit is at the {@link #RATE_LIMIT_THRESHOLD} * maximal requests, the program will try to switch to cloning repositories.
     */
    public static double RATE_LIMIT_THRESHOLD = 0.9;

    /**
     * The maximal size of a repository to still be cloned.
     * Unit: KB
     */
    public static int MAX_CLONE_SIZE = 100000;

    /**
     * The maximal number of files to be requested at once. If more files are requested, the repository will be cloned.
     */
    public static int MAX_FILE_AMOUNT = 40;

    public static int DEFAULT_NUMBER_OF_STAR = 100;
}
