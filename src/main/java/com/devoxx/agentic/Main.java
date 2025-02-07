package com.devoxx.agentic;

import com.devoxx.agentic.llm.ContentSummarizer;
import com.devoxx.agentic.llm.GeminiClient;
import com.devoxx.agentic.llm.OpenAIClient;
import com.devoxx.agentic.model.ContentStore;
import com.devoxx.agentic.search.BingSearchLLM;
import com.devoxx.agentic.search.GoogleSearchLLM;
import com.devoxx.agentic.search.YahooSearchLLM;
import com.devoxx.agentic.util.ResultWriter;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Slf4j
public class Main {

    static String openaiApiKey;
    static String openaiModelName;

    static String googleApiKey;
    static String geminiModelName;

    public static void checkEnvSettings(@NotNull Dotenv dotenv) {

        openaiApiKey = dotenv.get("OPENAI_API_KEY");
        openaiModelName = dotenv.get("OPENAI_MODEL_NAME");

        if (openaiApiKey == null) {
            log.error("OpenAI API key not found in environment variables");
            System.exit(1);
        }

        if (openaiModelName == null || openaiModelName.isEmpty()) {
            openaiModelName = "gpt-3.5-turbo";
        }

        googleApiKey = dotenv.get("GOOGLE_API_KEY");
        geminiModelName = dotenv.get("GEMINI_MODEL_NAME");
        if (googleApiKey == null) {
            log.error("Google Gemini API key not found in environment variables");
            System.exit(1);
        }

        if (geminiModelName == null || geminiModelName.isEmpty()) {
            geminiModelName = "gemini-2.0-pro-exp-02-05";
        }
    }

    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.load();

        checkEnvSettings(dotenv);

        String outputDirectory = dotenv.get("OUTPUT_DIRECTORY", "search_results");
        boolean generateNewQueries = Boolean.parseBoolean(dotenv.get("GENERATE_NEW_QUERIES", "false"));
        boolean generateSummaries = Boolean.parseBoolean(dotenv.get("GENERATE_SUMMARIES", "false"));

        log.info("Using model {} with{} new query generation", openaiModelName, generateNewQueries ? " " : "out ");

        // Get the DeepSearch query from the user input
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the deep search query: ");
        String query = scanner.nextLine();

        ContentStore contentStore = new ContentStore();

        OpenAIClient openAIClient = new OpenAIClient(openaiApiKey, openaiModelName);

        List<String> allQueries = openAIClient.createSubQueries(query);

        if (allQueries.size() < 9) {
            log.error("Failed to generate enough queries. Required: 9, Generated: {}", allQueries.size());
            System.exit(1);
        }

        // Initialize summarizer if enabled
        ContentSummarizer summarizer = null;
        if (generateSummaries) {
            summarizer = new ContentSummarizer();
        }

        // Split queries for each search engine
        List<String> googleQueries = allQueries.subList(0, 3);
        List<String> yahooQueries = allQueries.subList(3, 6);
        List<String> bingQueries = allQueries.subList(6, 9);

        // GOOGLE SEARCH - first 3 queries
        try (GoogleSearchLLM googleSearchLLM = new GoogleSearchLLM()) {
            log.info("Processing Google sub-queries...");
            for (String subQuery : googleQueries) {
                contentStore = googleSearchLLM.executeSearch(contentStore, subQuery, summarizer);
            }
        }

        // YAHOO SEARCH - next 3 queries
        try (YahooSearchLLM yahooSearchLLM = new YahooSearchLLM()) {
            log.info("Processing Yahoo sub-queries...");
            for (String subQuery : yahooQueries) {
                contentStore = yahooSearchLLM.executeSearch(contentStore, subQuery, summarizer);
            }
        }

        // BING SEARCH - last 3 queries
        try (BingSearchLLM bingSearchLLM = new BingSearchLLM()) {
            log.info("Processing Bing sub-queries...");
            for (String subQuery : bingQueries) {
                contentStore = bingSearchLLM.executeSearch(contentStore, subQuery, summarizer);
            }
        }

        // Get all the results
        Map<String, List<String>> linksByEngine = contentStore.getLinksByEngine();
        String allContent = contentStore.getAllContent();

        String allSummaries = contentStore.getAllSummaries();

        log.info("All content has been retrieved, now lets analyze it...");

        // Generate analysis
        GeminiClient geminiClient = new GeminiClient(googleApiKey, geminiModelName);
        String analysisResult = geminiClient.analyze(allContent, query);

        // Write all results to file
        ResultWriter resultWriter = new ResultWriter(outputDirectory, query);
        resultWriter.writeResults(
                linksByEngine,
                allContent,
                allSummaries,
                analysisResult,
                generateNewQueries,
                generateNewQueries
        );
    }
}