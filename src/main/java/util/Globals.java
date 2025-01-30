package util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Globals {
    public static Path RESOURCE_PATH = Paths.get("").toAbsolutePath().resolve(Paths.get("src/main/resources"));
    public static Path CLONED_REPOS_PATH = (Paths.get("").toAbsolutePath()).resolve(Paths.get("src/main/resources/cloned_repos"));
            //"C:/Users/colin/Documents/Programmierprojekt/monarch/src/main/resources/cloned_repos/";

    /**
     * If the request limit is at the {@link #RATE_LIMIT_THRESHOLD} * maximal requests, the program will try to switch to cloning repositories.
     */
    public static double RATE_LIMIT_THRESHOLD = 0.9;
}
