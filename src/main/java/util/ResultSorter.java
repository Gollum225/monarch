package util;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static util.Globals.RESOURCE_PATH;

/**
 * ChatGPT entirely generated this class. The functionality has been checked manually, take care anyway when using it.
 */
public class ResultSorter {

    /**
     * Reads in a CSV file, sorts the rows according to the "TotalScore" column and writes the result to a new file.
     *
     * @param inputFile path to input CSV-file
     * @param outputFile path to output CSV-file
     * @throws IOException if an error occurs when reading or writing the files
     */
    public static void sortCsvByTotalScore(String inputFile, String outputFile) throws IOException {
        // Create CsvMapper
        CsvMapper csvMapper = new CsvMapper();

        // Read schema with the header from the file
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        // Read data from CSV
        List<Map<String, String>> data = new ArrayList<>();
        try (MappingIterator<Map<String, String>> it = csvMapper
                .readerFor(Map.class)
                .with(schema)
                .readValues(new File(inputFile))) {

            while (it.hasNext()) {
                data.add(it.next());
            }
        }

        // Sort by TotalScore (numerical)
        data.sort((map1, map2) -> {
            try {
                double score1 = Double.parseDouble(map1.get("TotalScore"));
                double score2 = Double.parseDouble(map2.get("TotalScore"));
                return Double.compare(score2, score1); // sort descending (highest value first)
            } catch (NumberFormatException e) {
                // sort alphabetical, if TotalScore is not a number (shouldn't occur)
                return map1.get("TotalScore").compareTo(map2.get("TotalScore"));
            }
        });

        // Create a schema for the output (with all columns from the first data set)
        CsvSchema.Builder schemaBuilder = CsvSchema.builder();
        if (!data.isEmpty()) {
            for (String column : data.getFirst().keySet()) {
                schemaBuilder.addColumn(column);
            }
        }
        CsvSchema outputSchema = schemaBuilder.build().withHeader();

        // Write data to a new CSV file
        csvMapper.writer(outputSchema)
                .writeValue(new File(outputFile), data);

        System.out.println("CSV-file successfully order by TotalScore and stored in  " + outputFile);
    }

    // Example call of the method
    public static void main(String[] args) {
        Path path = RESOURCE_PATH.resolve("results.csv");
        Path outputPath = RESOURCE_PATH.resolve("sorted.csv");
        try {
            sortCsvByTotalScore(path.toString(), outputPath.toString());
        } catch (IOException e) {
            // No need to catch the exception.
            System.err.println("Error while processing CSV-file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}