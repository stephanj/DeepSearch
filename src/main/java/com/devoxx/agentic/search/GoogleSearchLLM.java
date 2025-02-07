package com.devoxx.agentic.search;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleSearchLLM extends AbstractSearchLLM {
    public GoogleSearchLLM() {
        super(SearchEngine.GOOGLE);
    }
}
