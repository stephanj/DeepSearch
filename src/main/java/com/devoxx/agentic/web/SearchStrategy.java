package com.devoxx.agentic.web;

import lombok.Getter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

class SearchStrategy {
    private final String baseUrl;
    @Getter
    private final String linkSelector;

    public SearchStrategy(String baseUrl, String linkSelector) {
        this.baseUrl = baseUrl;
        this.linkSelector = linkSelector;
    }

    public String buildSearchUrl(String query) {
        return baseUrl + URLEncoder.encode(query, StandardCharsets.UTF_8);
    }
}