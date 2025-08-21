package org.fokinms.local_llm_rag.advisors.expansion;

import lombok.Builder;
import lombok.Getter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaOptions;

import java.util.Map;

@Builder
public class ExpansionQueryAdvisor implements BaseAdvisor {

    public static ExpansionQueryAdvisorBuilder builder(ChatModel chatModel) {
        return new ExpansionQueryAdvisorBuilder().chatClient(ChatClient.builder(chatModel)
                .defaultOptions(OllamaOptions.builder()
                        .temperature(0.0)
                        .topK(1)
                        .topP(0.1)
                        .repeatPenalty(1.0)
                        .build())
                .build());
    }

    private static final PromptTemplate template = PromptTemplate.builder()
            .template("""
                    Если в вопросе есть какая-либо ссылка во втором лице (например "ты", "тебе", "у тебя", "тобой" и т.д.), то отвечай от имени Олег
                    Если ссылок не будет, верни вопрос без изменений.
                    
                    Question: {question}
                    Reformulated:
                    """)
            .build();

    private ChatClient chatClient;

    @Getter
    private int order;

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String userQuestion = chatClientRequest.prompt().getUserMessage().getText();

        String enrichedQuestion = chatClient.prompt()
                .user(template.render(Map.of("question", userQuestion)))
                .call()
                .content();

        double ratio = enrichedQuestion.length() / (double) userQuestion.length();

        return chatClientRequest.mutate()
                .context("ENRICHED_QUESTION", enrichedQuestion)
                .context("ORIGINAL_QUESTION", userQuestion)
                .context("EXPENSION_RATIO", ratio)
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }
}
