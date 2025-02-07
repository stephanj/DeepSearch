package com.devoxx.agentic.llm;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public class OpenAIClient {

    private final OpenAiChatModel chatLanguageModel;

    public OpenAIClient(String apiKey, String modelName) {

        this.chatLanguageModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    /**
     * Create subqueries for the main query
     * @param mainQuery The main query
     * @return List of subqueries
     */
    public List<String> createSubQueries(String mainQuery) {
//        SubQueryAssistant subQueryAssistant = AiServices.create(SubQueryAssistant.class, chatLanguageModel);

        SubQueryAssistant subQueryAssistant = AiServices.builder(SubQueryAssistant.class).chatLanguageModel(chatLanguageModel).build();

        String subQueries = subQueryAssistant.getSubQueries(mainQuery);

        JsonArray jsonQueries = (JsonArray) JsonParser.parseString(subQueries);

        return jsonQueries.asList()
                .stream()
                .map(JsonElement::getAsString)
                .toList();
    }

    interface SubQueryAssistant {
        @SystemMessage("""
                You are an expert in web search optimization and query refinement.
                Given a broad or ambiguous user query, generate a set of well-structured sub-queries that enhance web search accuracy.

                # Instructions:
                - Understand the user query and break it into key components (e.g., topic, intent, focus area).
                - Identify possible search intents, such as:
                  - Definition & Concepts – Understanding the topic.
                  - Implementation & How-To – Practical steps.
                  - Tools & Frameworks – Technologies related to the query.
                  - Best Practices & Optimization – Improving efficiency.
                  - Case Studies & Real-World Applications – Industry examples.
                
                YOU MUST RETURN 9 SUB-QUERIES, this is very important for the user to get the best results.
                
                - Generate 9 refined sub-queries** that:
                  - Are specific and clear
                  - Use SEO-friendly language for better web search ranking.
                  - Cover different aspects of the original query.
                - Ensure variety to capture different interpretations.
                """)
        @UserMessage("""
                <USER_QUERY>>
                {{query}}
                </USER_QUERY>>
                
                # Optimized Sub-Queries:
                Return a JSON Array containing three relevant subqueries for the above user query.
                Only return the json array, do not include markdown or any other text.
                Example : ["subquery1", "subquery2", "subquery3", "subquery4", "subquery5", "subquery6", "subquery7", "subquery8", "subquery9"]
                """)
        String getSubQueries(@V("query") String query);
    }
}
