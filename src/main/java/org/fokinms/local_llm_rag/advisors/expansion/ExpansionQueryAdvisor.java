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

    private static final PromptTemplate template = PromptTemplate.builder()
            .template("""
                    Instruction: Расширь поисковый запрос, добавив наиболее релевантные термины.
                    
                    Специализация по "":
                    -
                    -
                    -
                    -
                    
                    Правила:
                    1. Сохрани все слова из исходного вопроса
                    2. Добавь максимум пять наиболее важных терминов
                    3. Выбирай самые специфичные и релевантные слова
                    4. Результат - простой список слов через пробел
                    
                    Стратегия выбора:
                    - Приоритет: специализированные термины
                    - Избегай общих слов
                    - Фокусируйся на ключевых понятиях
                    
                    Question: {question}
                    Expanded query:
                    """)
            .build();

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

    private ChatClient chatClient;

    @Getter
    private final int order;

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String userQuestion = chatClientRequest.prompt().getUserMessage().getText();

        String enrichedQuestion = chatClient.prompt()
                .user(template.render(Map.of("question", userQuestion)))
                .call()
                .content();

        assert enrichedQuestion != null;
        double ratio = enrichedQuestion.length() / (double) userQuestion.length();

        return chatClientRequest.mutate()
                .context("ENRICHED_QUESTION", enrichedQuestion)
                .context("ORIGINAL_QUESTION", userQuestion)
                .context("EXPANSION_RATIO", ratio)
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }
}
