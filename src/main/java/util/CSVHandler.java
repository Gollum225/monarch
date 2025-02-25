package util;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import controller.Rule;
import controller.RuleCollection;
import model.Repository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.Globals.RESOURCE_PATH;

/**
 * Responsible for the writing to and from the result file.
 */
public final class CSVHandler {

    /**
     *
     * @param filePath path or name of the file, from the root of the resources folder
     */
    public CSVHandler(String filePath) {

        this.path = RESOURCE_PATH.resolve(filePath);
        this.filePath = filePath.replace(" ", "");

    }

    private CsvSchema schema;
    private Path path;
    private final String filePath;

    /**
     * Creates a new CSV file with the given schema. Create a schema before calling this method. e.g. with {@link #createResultSchema}
     */
    public void createCsv() {
        if (schema == null) {
            throw new RuntimeException("Schema not set.");
        }
        File file = new File(path.toString());

        if (path.toFile().exists()) {
            if (handleExistingCsv(path)) {
                System.out.println(filePath + " file with matching schema found. Continue with this file.");
                return;
            } else {
                // The File exists and has no matching scheme.
                int counter = 2;
                while (file.exists()) {
                    file = new File(RESOURCE_PATH.resolve(filePath.replace(".csv", "") + counter + ".csv").toString());
                    path = file.toPath();
                    counter++;
                }
            }}
        CsvMapper csvMapper = new CsvMapper();

        try {
            csvMapper.writer(schema)
                    .writeValue(new File(path.toString()), new ArrayList<>());
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create CSV file at: " + filePath);
        }
        System.out.println("\u001B[33m" + "New CSV file created at:  " + path + "\u001B[0m");

    }

    /**
     * Checks if the schema of the existing CSV file matches the schema of the current run.
     * @param path to the existing CSV file
     * @return true, if the schema matches
     */
    private boolean handleExistingCsv(Path path) {
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema existingSchema = csvMapper.typedSchemaFor(Map.class).withHeader();

        try {
            MappingIterator<Map<String, String>> it = csvMapper
                    .readerFor(Map.class)
                    .with(existingSchema)
                    .readValues(new File(path.toString()));

            existingSchema = (CsvSchema) it.getParserSchema();

        } catch (IOException e) {
            return false;
        }

        if (!existingSchema.toString().equals(schema.toString())) {
            System.err.println("Schema doesn't match the existing schema in the file.");
            return false;
        }
        return true;
    }

    /**
     * Creates the schema for csv files for the results.
     *
     * @param ruleCollection a column for each rule in the collection will be added
     */
    public void createResultSchema(RuleCollection ruleCollection) {
        CsvSchema.Builder schemaBuilder = CsvSchema.builder()
                .addColumn("RepoName")
                .addColumn("repoOwner");

        for (Class<? extends Rule> rule : ruleCollection.getRules()) {
            schemaBuilder.addColumn(rule.getSimpleName());
        }

        schema = schemaBuilder
                .addColumn("Duration")
                .addColumn("TotalScore")
                .addColumn("reachablePoints")
                .setUseHeader(true)
                .build();
    }

    /**
     * Writes the results of one repository to the files.
     *
     * @param repo the repository to write the results of
     * @param ruleCollection the collection of rules to write the results of
     * @throws IOException if the file can't be written
     */
    public synchronized void writeResult(Repository repo, RuleCollection ruleCollection) throws IOException {

        File csvFile = new File(String.valueOf(path));

        CsvMapper csvMapper = new CsvMapper();
        List<Map<String, String>> rows = new ArrayList<>();


        Map<String, String> row = new HashMap<>();
        row.put("RepoName", repo.getRepositoryName());
        row.put("repoOwner", repo.getOwner());
        for (Class<? extends Rule> rule : ruleCollection.getRules()) {

            row.put(rule.getSimpleName(), String.valueOf(repo.getResults().get(rule).getResultString()));
        }
        row.put("Duration", String.valueOf(repo.getDuration()));
        row.put("TotalScore", String.valueOf(repo.getOverallPoints()));

        //TODO reachable points
        rows.add(row);


        // write csv file
        FileWriter fileWriter = new FileWriter(csvFile, true);
        SequenceWriter seqWriter = csvMapper.writer(schema.withoutHeader()).writeValues(fileWriter);
        seqWriter.writeAll(rows);



    }
}
