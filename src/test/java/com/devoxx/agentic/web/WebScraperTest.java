package com.devoxx.agentic.web;

import com.devoxx.agentic.search.SearchEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebScraperTest {

    @Mock
    private WebDriver driver;

    @Test
    void shouldSearchGoogle() {

        WebElement mockElement = mock(WebElement.class);
        when(mockElement.getDomProperty("href")).thenReturn("http://example.com");
        when(driver.findElements(any(By.class)))
                .thenReturn(Arrays.asList(mockElement));

        WebScraper scraper = new WebScraper(driver);
        List<String> results = scraper.search(SearchEngine.GOOGLE, "test query");

        assertEquals(1, results.size());
        assertEquals("http://example.com", results.get(0));
    }

    @Test
    void shouldHandleSearchFailure() {
        when(driver.findElements(any(By.class))).thenThrow(new RuntimeException("Search failed"));

        WebScraper scraper = new WebScraper(driver);
        List<String> results = scraper.search(SearchEngine.GOOGLE, "test query");

        assertTrue(results.isEmpty());
    }
}
