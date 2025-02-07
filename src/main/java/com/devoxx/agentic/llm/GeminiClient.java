package com.devoxx.agentic.llm;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeminiClient {

    private final ChatLanguageModel chatLanguageModel;

    public GeminiClient(String apiKey, String modelName) {

        Dotenv dotenv = Dotenv.load();
        chatLanguageModel = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.3)
                .build();
    }

    interface DeepResearchAssistant {
        @UserMessage("""
          Generate a comprehensive, well-researched document based on the given query:
      
          <USER_QUERY>
          {{query}}
          </USER_QUERY>

          # Response Format
          Your response MUST follow this structured format:
          1. Title and Abstract – A concise title and brief summary of the research.
          2. Executive Summary – A concise overview of the research findings.
          3. Key Findings – The most important insights, including supporting data if available.
          4. Recommendations – Actionable advice or suggested next steps.
          5. Code Examples (if applicable) – Relevant implementations, best practices, and explanations.
          6. Resources – A list of references formatted as markdown links.

          # Guidelines
          - Strictly use only the provided content in `<CONTENT>`. Do NOT generate responses from external knowledge.
          - Prioritize accuracy and clarity over verbosity.
          - Cite sources properly by linking back to the original content using markdown links.
          - If technical, provide concise and well-documented code snippets in the appropriate programming language.
          - Ensure logical flow and maintain a professional research tone.

          <CONTENT>
          {{content}}
          </CONTENT>
          """)
        String research(@V("content") String content, @V("query") String query);
    }

    public String analyze(String content, String query) {
        try {
            DeepResearchAssistant deepResearchAssistant = AiServices.create(DeepResearchAssistant.class, chatLanguageModel);
            return deepResearchAssistant.research(content, query);
        } catch (Exception e) {
            log.error("Failed to generate summary with Gemini", e);
            throw new RuntimeException("Gemini API error", e);
        }
    }
}