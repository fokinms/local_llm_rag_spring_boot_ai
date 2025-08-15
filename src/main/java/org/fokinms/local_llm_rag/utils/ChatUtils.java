package org.fokinms.local_llm_rag.utils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.fokinms.local_llm_rag.model.Chat;
import org.fokinms.local_llm_rag.model.ChatEntry;
import org.fokinms.local_llm_rag.model.Role;
import org.fokinms.local_llm_rag.repository.ChatRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatUtils {

    private final ChatRepository chatRepository;

    @Transactional
    public void addChatEntry(Long chatId, String prompt, Role role) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        chat.addChatEntry(ChatEntry.builder().content(prompt).role(role).build());
    }
}
