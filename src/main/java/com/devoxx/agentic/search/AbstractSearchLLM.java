package com.devoxx.agentic.search;

import com.devoxx.agentic.llm.ContentSummarizer;
import com.devoxx.agentic.model.ContentStore;
import com.devoxx.agentic.web.WebScraper;
import com.devoxx.agentic.web.WebSearchException;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public abstract class AbstractSearchLLM implements AutoCloseable {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    protected final WebDriver driver;
    protected final WebScraper scraper;
    protected final ExecutorService executor;
    protected final SearchEngine searchEngine;

    protected AbstractSearchLLM(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--force-device-scale-factor=1");
        options.addArguments("--window-size=1000,1300");
        options.addArguments("--disable-pdf-viewer");

        this.driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(TIMEOUT);

        this.scraper = new WebScraper(driver);
        this.executor = Executors.newFixedThreadPool(5);
    }

    public ContentStore executeSearch(ContentStore contentStore, String subQuery, ContentSummarizer summarizer) {
        try {
            // Process each sub-query
            List<String> links = scraper.search(searchEngine, subQuery);
            contentStore.addLinks(subQuery, searchEngine, links);

            // Process content for each link concurrently
            List<CompletableFuture<Void>> contentFutures = links.stream()
                    .map(link -> contentStore.processContentAsync(subQuery, searchEngine, link, scraper))
                    .toList();

            // Wait for all content to be processed
            CompletableFuture.allOf(
                    contentFutures.toArray(new CompletableFuture[0])
            ).join();

            // Process summaries sequentially after all content is fetched
            if (summarizer != null) {
                contentStore.processSummaries(subQuery, searchEngine, summarizer);
            }

            return contentStore;
        } catch (Exception e) {
            log.error("Search failed for {}", searchEngine.name(), e);
            throw new WebSearchException("Failed to execute search with " + searchEngine.name(), e);
        }
    }

    @Override
    public void close() {
        executor.shutdown();
        driver.quit();
    }
}
