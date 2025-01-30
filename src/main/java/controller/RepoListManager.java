package controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import model.RepoList;
import model.Repository;
import repository_information.GitHub.GithubCommunication;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static util.Globals.RESOURCE_PATH;

/**
 * Manages the list of repositories and provides the next repository to check.
 * Keeps track of processed repositories. Gets new repositories, if the amount of unprocessed is under {@link #THRESHOLD}.
 */
public class RepoListManager {

    public RepoListManager(RuleCollection ruleCollection) {
        createSchema(ruleCollection);
        createCsv();
    }

    private CsvSchema schema;

    private final RepoList repoList = RepoList.getInstace();

    /**
     * Starts getting new repositories, when the amount of unprocessed repositories is below this threshold.
     */
    private final int THRESHOLD = 2;

    private final int REFILL_AMOUNT = 3;

    /**
     * Amount of unprocessed, ready repositories.
     */
    private int unprocessedRepos = 0;



    public synchronized Repository getNextRepo() throws TimeoutException {
        checkRepoAmount();


        // if no new repositories are available, wait for 10 seconds.
        // If still no new repositories are available, throw a TimeoutException
        for (int i = 0; i < 10; i++) {
            if (repoList.size() == 0) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    continue; // test again, if list is already filled
                }
            } else {
                break;
            }
            throw new TimeoutException("No new repositories available.");
        }
        unprocessedRepos--;

        return repoList.getNext();
    }

    private void getNewRepos(int amount) {

        List<Repository> repos;
        try {
             repos = GithubCommunication.getRepository(amount);
        } catch (JsonProcessingException e) {
            throw new InputMismatchException("Error while getting new repositories.");
        }

        for (Repository repo : repos) {
            if (repoList.addRepo(repo)) {
                unprocessedRepos++;
            }
        }

        checkRepoAmount();
    }

    /**
     * Writes the result of the search to files.
     * @param repo
     */
    public void finishRepo(Repository repo, RuleCollection ruleCollection) {
        repo.finish();
        System.out.println("\u001B[32m" + "repo: " + repo.getRepositoryName() + " of " + repo.getOwner() + " got: " + repo.getOverallPoints() + " points" + "\u001B[0m");
        //TODO
        try {
            writeResult(repo, ruleCollection);//TODO
        } catch (IOException e) {
            throw new RuntimeException(e); //TODO
        }
    }

    /**
     * Checks if the amount of unprocessed repositories is below the threshold.
     * Triggers the refill if necessary
     */
    private void checkRepoAmount() {
        if (repoList.size() < THRESHOLD) {
            getNewRepos(REFILL_AMOUNT);
        }
    }

    private void getTestRepos() {
        repoList.addRepo(new Repository("TeaStore", "DescartesResearch"));
        repoList.addRepo(new Repository("teammates", "TEAMMATES"));
        repoList.addRepo(new Repository("javaPS", "52North"));
        repoList.addRepo(new Repository("arctic-sea", "52North"));
        repoList.addRepo(new Repository("bigbluebutton", "bigbluebutton"));
        unprocessedRepos = 5;
    }

    private void createCsv() {
        Path path = RESOURCE_PATH.resolve("result.csv");
        File file = new File(path.toString());

        if (path.toFile().exists()) {
            if (handleExistingCsv(path)) {
                return;
            } else if (file.renameTo(new File(RESOURCE_PATH.resolve("resultCopied.csv").toString()))) {
                System.out.println("\u001B[33m" + "Old result file was found and renamed. Delete before next run!" + "\u001B[0m");
            } else {
                System.err.println("Old result file was found and deleted.");
                file.delete();
            }
        }
        CsvMapper csvMapper = new CsvMapper();

        try {
            csvMapper.writer(schema)
                    .writeValue(new File(path.toString()), new ArrayList<>());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create CSV file.");
        }
        System.out.println("\\u001B[32m" + "CSV file created: " + path + "\u001B[0m");

    }

    /**
     * Checks if the schema of the existing CSV file matches the schema of the current run.
     * @param path to the existing CSV file
     * @return true, if the schema matches
     */
    private boolean handleExistingCsv(Path path) {
        CsvMapper csvMapper = new CsvMapper();

        CsvSchema existingSchema = CsvSchema.emptySchema().withHeader();

        try {
            MappingIterator<Map<String, String>> it = csvMapper
                    .readerFor(Map.class)
                    .with(schema)
                    .readValues(new File(path.toString()));

            if (it.hasNext()) {
                Map<String, String> firstRow = it.next();

                CsvSchema.Builder schemaBuilder = CsvSchema.builder();
                firstRow.keySet().forEach(schemaBuilder::addColumn);
                schema = schemaBuilder.setUseHeader(true).build();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!existingSchema.equals(schema)) {
            System.err.println("Schema doesn't match the existing schema in the file. Overwriting the file.");
            return false;
        }
        return true;
    }

    private void createSchema(RuleCollection ruleCollection) {
        CsvSchema.Builder schemaBuilder = CsvSchema.builder()
                .addColumn("RepoName")
                .addColumn("repoOwner");

        for (Class<? extends Rule> rule : ruleCollection.getRules()) {
            schemaBuilder.addColumn(rule.getSimpleName());
        }

        schema = schemaBuilder
                .addColumn("TotalScore")
                .addColumn("reachablePoints")
                .setUseHeader(true)
                .build();
    }

    private void writeResult(Repository repo, RuleCollection ruleCollection) throws IOException {

        File csvFile = new File(String.valueOf(RESOURCE_PATH.resolve("result.csv")));

        CsvMapper csvMapper = new CsvMapper();
        List<Map<String, String>> rows = new ArrayList<>();


        Map<String, String> row = new HashMap<>();
        row.put("RepoName", repo.getRepositoryName());
        row.put("repoOwner", repo.getOwner());
        for (Class<? extends Rule> rule : ruleCollection.getRules()) {
            if (rule.getSimpleName().equals("DocFolder")) {
                continue;
            }
            row.put(rule.getSimpleName(), String.valueOf(repo.getResults().get(rule).getResultString()));
        }
        row.put("TotalScore", String.valueOf(repo.getOverallPoints()));

        rows.add(row);


        // write csv file
            csvMapper.writer(schema.withHeader()).writeValue(csvFile, rows);


    }

}
