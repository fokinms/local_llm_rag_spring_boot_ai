package org.fokinms.local_llm_rag;

import lombok.RequiredArgsConstructor;
import org.fokinms.local_llm_rag.advisors.expansion.ExpansionQueryAdvisor;
import org.fokinms.local_llm_rag.advisors.rag.RagAdvisor;
import org.fokinms.local_llm_rag.repository.ChatRepository;
import org.fokinms.local_llm_rag.service.PostgresChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class LocalLlmRagApplication {

    private static final PromptTemplate SYSTEM_PROMPT = new PromptTemplate(
            """
                    Ты - fms, Java-разработчик, отвечай от первого лица, кратко
                    
                    Вопрос может быть о следствии факта их Context.
                    Всегда связывай: факт Context -> вопрос.
                    
                    Нет связи, даже косвенной = "я не знаю ответ на этот вопрос".
                    Есть связь = отвечай.
                    """
    );

    private final ChatRepository chatRepository;
    private final VectorStore vectorStore;
    private final ChatModel chatModel;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultAdvisors(
                        ExpansionQueryAdvisor.builder(chatModel).order(0).build(),
                        getHistoryAdvisor(10),
                        SimpleLoggerAdvisor.builder().order(20).build(),
                        RagAdvisor.build(vectorStore).order(30).build(),
                        SimpleLoggerAdvisor.builder().order(40).build())
                .defaultOptions(OllamaOptions.builder()
                        .temperature(0.3)
                        .topP(0.7)
                        .topK(20)
                        .repeatPenalty(1.1)
                        .build())
                .defaultSystem(SYSTEM_PROMPT.render())
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
