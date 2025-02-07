package com.devoxx.agentic.web;

import com.devoxx.agentic.search.SearchEngine;
import com.devoxx.agentic.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.*;

@Slf4j
public class WebScraper implements AutoCloseable {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_DELAY = Duration.ofSeconds(2);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";

    private final WebDriver driver;
    private final Random random = new Random();
    private final Map<SearchEngine, SearchStrategy> searchStrategies;

    public WebScraper(WebDriver driver) {
        this.driver = driver;
        this.searchStrategies = initializeSearchStrategies();
    }

    private @NotNull Map<SearchEngine, SearchStrategy> initializeSearchStrategies() {
        Map<SearchEngine, SearchStrategy> strategies = new EnumMap<>(SearchEngine.class);

        strategies.put(SearchEngine.GOOGLE, new SearchStrategy(
                "https://www.google.com/search?q=",
                "div.yuRUbf a, div.g a, a[jsname=UWckNb]"
        ));

        strategies.put(SearchEngine.BING, new SearchStrategy(
                "https://www.bing.com/search?q=",
                "h2 a"
        ));

        strategies.put(SearchEngine.YAHOO, new SearchStrategy(
                "https://search.yahoo.com/search?p=",
                "h3.title a"
        ));

        return strategies;
    }

    public List<String> search(SearchEngine engine, String query) {
        SearchStrategy strategy = searchStrategies.get(engine);
        if (strategy == null) {
            log.error("No strategy found for search engine: {}", engine);
            return Collections.emptyList();
        }

        try {
            String searchUrl = strategy.buildSearchUrl(query);
            driver.get(searchUrl);

            List<String> links = driver.findElements(By.cssSelector(strategy.getLinkSelector()))
                    .stream()
                    .map(this::extractUrl)
                    .filter(Objects::nonNull)
                    .toList();

            introduceDelay();
            return links;
        } catch (Exception e) {
            log.error("{} search failed for query: {}", engine, query, e);
            return Collections.emptyList();
        }
    }

    public String fetchContentWithRetry(String url) {
        return RetryUtil.withRetry(3, () -> {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout((int) TIMEOUT.toMillis())
                    .get();
            return doc.text();
        }, "Failed to fetch content from " + url);
    }

    private @Nullable String extractUrl(WebElement element) {
        try {
            return element.getDomProperty("href");
        } catch (Exception e) {
            log.error("Error extracting URL from element", e);
            return null;
        }
    }

    private void introduceDelay() {
        try {
            Thread.sleep(REQUEST_DELAY.toMillis() + random.nextInt(3000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}