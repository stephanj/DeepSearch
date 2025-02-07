package com.devoxx.agentic.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class ResultWriterIT {

    @Test
    public void testWriteResult() {
        // Given
        ResultWriter resultWriter = new ResultWriter("results", "How to do RAG?");

        Map<String, List<String>> links = Map.of("google", List.of("link1", "link2"));

        resultWriter.writeResults(links, "test", "summary", "resukt", false, false);
    }
}
