package com.devoxx.agentic.llm;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContentSummarizer {
    private final OllamaChatModel chatLanguageModel;

    public ContentSummarizer() {
        this.chatLanguageModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434/")
                .modelName("llama3.1:latest")
                .build();
    }

    interface SummaryAssistant {
        @UserMessage("""
            Summarize the following webpage content in 10 - 15 concise sentences, focusing on the main points:
            
            {{content}}
            """)
        String summarize(@V("content") String content);
    }

    public String generateSummary(String content) {
        try {
            log.info("Generating summary for content: {}", content);
            SummaryAssistant assistant = AiServices.create(SummaryAssistant.class, chatLanguageModel);
            String summarize = assistant.summarize(content);
            log.info("Generated summary: {}", summarize);
            return summarize;
        } catch (Exception e) {
            log.error("Failed to generate summary", e);
            return "Summary generation failed";
        }
    }
}