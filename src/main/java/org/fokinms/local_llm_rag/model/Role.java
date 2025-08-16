package org.fokinms.local_llm_rag.model;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("user") {
        @Override
        public Message getMessage(String prompt) {
            return new UserMessage(prompt);
        }
    },
    ASSISTANT("assistant") {
        @Override
        public Message getMessage(String prompt) {
            return new AssistantMessage(prompt);
        }
    },
    SYSTEM("system") {
        @Override
        public Message getMessage(String prompt) {
            return new SystemMessage(prompt);
        }
    };

    private final String role;

    public static Role getRole(String value) {
        return Arrays.stream(Role.values())
                .filter(r -> r.role.equals(value))
                .findFirst()
                .orElseThrow();
    }

    abstract public Message getMessage(String prompt);
}
