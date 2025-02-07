package com.devoxx.agentic.llm;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OpenAIClientIT {

    @Test
    void shouldCreateSubQueries() {
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("OPENAI_API_KEY");
        String modelName = dotenv.get("OPENAI_MODEL_NAME");

        OpenAIClient openAIClient = new OpenAIClient(apiKey, modelName);
        List<String> subQueries = openAIClient.createSubQueries("How to implement RAG for codebases");

        assertNotNull(subQueries);
        assertEquals(9, subQueries.size());
    }
}