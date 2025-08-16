package org.fokinms.local_llm_rag.service;

import lombok.RequiredArgsConstructor;
import org.fokinms.local_llm_rag.model.Chat;
import org.fokinms.local_llm_rag.model.ChatEntry;
import org.fokinms.local_llm_rag.repository.ChatRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PostgresChatMemory implements ChatMemory {

    private final ChatRepository chatRepository;

    @Override
    @Transactional
    public void add(String conversationId, List<Message> messages) {
        for (Message message : messages) {
            Chat chat = chatRepository.findById(Long.valueOf(conversationId)).orElseThrow();
            chat.addChatEntry(ChatEntry.toChatEntry(message));
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        Chat chat = chatRepository.findById(Long.valueOf(conversationId)).orElseThrow();
        return chat.getHistory().stream()
                .map(ChatEntry::toMessage)
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        //not implemented (never)
    }
}
