package com.devoxx.agentic.search;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BingSearchLLM extends AbstractSearchLLM {
    public BingSearchLLM() {
        super(SearchEngine.BING);
    }
}
