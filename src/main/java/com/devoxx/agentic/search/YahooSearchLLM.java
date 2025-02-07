package com.devoxx.agentic.search;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YahooSearchLLM extends AbstractSearchLLM {
    public YahooSearchLLM() {
        super(SearchEngine.YAHOO);
    }
}