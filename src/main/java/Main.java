import controller.Checker;
import util.Json;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class Main {

    public static void main(String[] args) throws IOException, TimeoutException {
        Json.setup();

        Checker checker = new Checker();
        checker.checkRepos(1);

    }


}