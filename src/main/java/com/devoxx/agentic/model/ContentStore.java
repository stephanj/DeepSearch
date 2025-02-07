package com.devoxx.agentic.model;

import com.devoxx.agentic.llm.ContentSummarizer;
import com.devoxx.agentic.search.SearchEngine;
import com.devoxx.agentic.web.WebScraper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class ContentStore {

    @Data
    public static class SearchResult {
        private final String subQuery;
        private final String searchEngine;
        private final List<String> links;
        private final Map<String, String> contents;
        private final Map<String, String> summaries;

        public SearchResult(String subQuery, String searchEngine) {
            this.subQuery = subQuery;
            this.searchEngine = searchEngine;
            this.links = Collections.synchronizedList(new ArrayList<>());
            this.contents = new ConcurrentHashMap<>();
            this.summaries = new ConcurrentHashMap<>();
        }
    }

    private final Map<String, Map<String, SearchResult>> results;
    private final ExecutorService executor;

    public ContentStore() {
        this.results = new ConcurrentHashMap<>();
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void addSummary(String subQuery, SearchEngine searchEngine, String link, String summary) {
        Map<String, SearchResult> engineResults = results.get(subQuery);
        if (engineResults != null) {
            SearchResult result = engineResults.get(searchEngine.name());
            if (result != null) {
                result.getSummaries().put(link, summary);
            }
        }
    }

    public void addLinks(String subQuery, SearchEngine searchEngine, List<String> links) {
        Map<String, SearchResult> engineResults = results.computeIfAbsent(
                subQuery,
                k -> new ConcurrentHashMap<>()
        );

        SearchResult result = engineResults.computeIfAbsent(
                searchEngine.name(),
                engine -> new SearchResult(subQuery, engine)
        );

        result.getLinks().addAll(links);
    }

    public void addContent(String subQuery, SearchEngine searchEngine, String link, String content) {
        Map<String, SearchResult> engineResults = results.get(subQuery);
        if (engineResults != null) {
            SearchResult result = engineResults.get(searchEngine.name());
            if (result != null) {
                result.getContents().put(link, content);
            }
        }
    }

    public CompletableFuture<Void> processContentAsync(String subQuery,
                                                       SearchEngine searchEngine,
                                                       String link,
                                                       WebScraper scraper) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Fetch content concurrently
                String content = scraper.fetchContentWithRetry(link);
                addContent(subQuery, searchEngine, link, content);
            } catch (Exception e) {
                log.error("Failed to fetch content for {}: {}", link, e.getMessage());
            }
        }, executor);
    }

    // New method for sequential summary processing
    public void processSummaries(String subQuery, SearchEngine searchEngine, ContentSummarizer summarizer) {
        if (summarizer == null) {
            return;
        }

        Map<String, SearchResult> engineResults = results.get(subQuery);
        if (engineResults == null || !engineResults.containsKey(searchEngine.name())) {
            return;
        }

        SearchResult result = engineResults.get(searchEngine.name());
        result.getContents().forEach((link, content) -> {
            try {
                log.info("Generating summary for link: {}", link);
                String summary = summarizer.generateSummary(content);
                addSummary(subQuery, searchEngine, link, summary);
            } catch (Exception e) {
                log.error("Failed to generate summary for {}: {}", link, e.getMessage());
            }
        });
    }

    // Rest of the methods remain the same
    public List<SearchResult> getAllResults() {
        List<SearchResult> allResults = new ArrayList<>();
        results.values().forEach(engineResults ->
                allResults.addAll(engineResults.values())
        );
        return allResults;
    }

    public Map<String, List<String>> getLinksByEngine() {
        Map<String, List<String>> linksByEngine = new HashMap<>();
        results.values().forEach(engineResults ->
                engineResults.forEach((engine, result) -> {
                    linksByEngine.computeIfAbsent(engine, k -> new ArrayList<>())
                            .addAll(result.getLinks());
                })
        );
        return linksByEngine;
    }

    public String getAllContent() {
        StringBuilder content = new StringBuilder();
        results.forEach((subQuery, engineResults) -> {
            content.append("Results for query: ").append(subQuery).append("\n\n");
            engineResults.forEach((engine, result) -> {
                content.append("Search Engine: ").append(engine).append("\n");
                if (result.summaries.isEmpty()) {
                    result.getContents().forEach((link, text) -> {
                        content.append("Source: ").append(link).append("\n");
                        content.append(text).append("\n\n");
                    });
                } else {
                    result.getContents().forEach((link, text) -> {
                        content.append("Source: ").append(link).append("\n");
                        content.append("Summary: ").append(result.getSummaries().get(link)).append("\n");
                        content.append(text).append("\n\n");
                    });
                }
            });
        });
        return content.toString();
    }

    public String getAllSummaries() {
        StringBuilder content = new StringBuilder();
        results.forEach((subQuery, engineResults) -> {
            content.append("Results for query: ").append(subQuery).append("\n\n");
            engineResults.forEach((engine, result) -> {
                content.append("Search Engine: ").append(engine).append("\n");
                if (!result.summaries.isEmpty()) {
                    result.getContents().forEach((link, text) -> {
                        content.append("Source: ").append(link).append("\n");
                        content.append("Summary: ").append(result.getSummaries().get(link)).append("\n");
                        content.append(text).append("\n\n");
                    });
                }
            });
        });
        return content.toString();
    }
}