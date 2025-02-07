package com.devoxx.agentic.util;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
public class ResultWriter {
    private final Path outputPath;
    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private final String mainQuery;

    public ResultWriter(String baseDirectory, @NotNull String query) {
        this.mainQuery = query;

        String cleanQuery = query.replace(" ", "_").toLowerCase().trim()
                                 .replaceAll("[^a-zA-Z0-9]", "");

        String timestamp = LocalDateTime.now().format(FILE_TIME_FORMAT);
        this.outputPath = Paths.get(baseDirectory, cleanQuery + "-" + timestamp + ".md");
    }

    public void writeResults(@NotNull Map<String, List<String>> linksByEngine,
                             String allContent,
                             String allSummaries,
                             String analysisResult,
                             boolean generateNewQueries,
                             boolean includeSummaries) {

        // Create directory if it doesn't exist
        outputPath.getParent().toFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath.toFile()))) {
            // Write search configuration
            writer.write("SEARCH CONFIGURATION:");
            writer.newLine();
            writer.write("Query Generation Strategy: " +
                    (generateNewQueries ? "Generating new queries for each engine" : "Reusing initial queries across engines"));
            writer.write("Summary Generation: " + (includeSummaries ? "Enabled" : "Disabled"));
            writer.newLine();
            writer.newLine();

            // Write the main query
            writer.write("MAIN QUERY:");
            writer.newLine();
            writer.write(mainQuery);
            writer.newLine();
            writer.newLine();

            // Write analysis results
            writer.write("============================================================");
            writer.newLine();
            writer.write("ANALYSIS RESULTS");
            writer.newLine();
            writer.write("============================================================");
            writer.newLine();
            writer.write(analysisResult);
            writer.newLine();

            // Write links by search engine
            writer.write("============================================================");
            writer.newLine();
            writer.write("LINKS BY SEARCH ENGINE");
            writer.newLine();
            writer.write("============================================================");
            writer.newLine();

            linksByEngine.forEach((engine, links) -> {
                try {
                    writer.write("Search Engine: " + engine);
                    writer.newLine();
                    for (String link : links) {
                        writer.write("  - " + link);
                        writer.newLine();
                    }
                    writer.newLine();
                } catch (IOException e) {
                    log.error("Error writing links for engine {}", engine, e);
                }
            });

            // Write summary content
            if (includeSummaries) {
                writer.write("============================================================");
                writer.newLine();
                writer.write("SUMMARY CONTENT");
                writer.newLine();
                writer.write("============================================================");
                writer.newLine();
                writer.write(allSummaries);
                writer.newLine();
                writer.newLine();
            }

            // Write raw content
            writer.write("============================================================");
            writer.newLine();
            writer.write("RAW CONTENT");
            writer.newLine();
            writer.write("============================================================");
            writer.newLine();
            writer.write(allContent);
            writer.newLine();
            writer.newLine();

            log.info("Results have been saved to: {}", outputPath);
        } catch (IOException e) {
            log.error("Failed to write results to file", e);
            throw new RuntimeException("Failed to write results", e);
        }
    }
}