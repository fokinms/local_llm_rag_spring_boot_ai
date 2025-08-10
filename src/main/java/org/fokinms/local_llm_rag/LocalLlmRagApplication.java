package org.fokinms.local_llm_rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LocalLlmRagApplication {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    public static void main(String[] args) {
        ChatClient chatClient = SpringApplication.run(LocalLlmRagApplication.class, args).getBean(ChatClient.class);
        System.out.println(chatClient.prompt().user("2 + 2").call().content());
    }
}
