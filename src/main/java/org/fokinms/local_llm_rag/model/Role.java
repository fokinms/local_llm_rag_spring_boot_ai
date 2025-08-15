package org.fokinms.local_llm_rag.model;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system");

    private final String role;
}
