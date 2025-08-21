package org.fokinms.local_llm_rag;

import lombok.RequiredArgsConstructor;
import org.fokinms.local_llm_rag.advisors.expansion.ExpansionQueryAdvisor;
import org.fokinms.local_llm_rag.repository.ChatRepository;
import org.fokinms.local_llm_rag.service.PostgresChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class LocalLlmRagApplication {

    private static final PromptTemplate MY_PROMPT_TEMPLATE = new PromptTemplate(
            """
                    {query}
                    
                    Контекст:
                    ---------------------
                    {question_answer_context}
                    ---------------------
                    
                    Отвечай только на основе контекста выше. Если информации нет в контексте, сообщи, что не можешь ответить."""
    );

    private final ChatRepository chatRepository;
    private final VectorStore vectorStore;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultAdvisors(
                        ExpansionQueryAdvisor.builder().order(0).build(),
                        getHistoryAdvisor(10),
                        SimpleLoggerAdvisor.builder().order(20).build(),
                        getRagAdvisor(30),
                        SimpleLoggerAdvisor.builder().order(40).build())
                .defaultOptions(OllamaOptions.builder()
                        .temperature(0.3)
                        .topP(0.7)
                        .topK(20)
                        .repeatPenalty(1.1)
                        .build())
                .build();
    }

    private Advisor getRagAdvisor(int order) {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(MY_PROMPT_TEMPLATE)
                .searchRequest(
                        SearchRequest.builder()
                                .topK(4)
                                .similarityThreshold(0.7)
                                .build())
                .order(order)
                .build();
    }

    private Advisor getHistoryAdvisor(int order) {
        return MessageChatMemoryAdvisor.builder(getChatMemory()).order(order).build();
    }

    private ChatMemory getChatMemory() {
        return PostgresChatMemory.builder()
                .maxMessages(8)
                .chatMemoryRepository(chatRepository)
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(LocalLlmRagApplication.class, args);
    }
}
