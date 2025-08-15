package org.fokinms.local_llm_rag.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.fokinms.local_llm_rag.model.Chat;
import org.fokinms.local_llm_rag.repository.ChatRepository;
import org.fokinms.local_llm_rag.utils.ChatUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.fokinms.local_llm_rag.model.Role.ASSISTANT;
import static org.fokinms.local_llm_rag.model.Role.USER;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;

    private final ChatClient chatClient;

    private final ChatUtils chatUtils;

    public List<Chat> getAllChats() {
        return chatRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Chat getChat(Long chatId) {
        return chatRepository.findById(chatId).orElseThrow();
    }

    public Chat createNewChat(String title) {
        Chat chat = Chat.builder().title(title).build();
        return chatRepository.save(chat);
    }

    public void deleteChat(Long chatId) {
        chatRepository.deleteById(chatId);
    }

//    @Transactional
//    public void proceedInteraction(Long chatId, String prompt) {
//        chatUtils.addChatEntry(chatId, prompt, USER);
//        String answer = chatClient.prompt().user(prompt).call().content();
//        chatUtils.addChatEntry(chatId, answer, ASSISTANT);
//    }

    public SseEmitter proceedInteractionWithStreaming(Long chatId, String prompt) {
        chatUtils.addChatEntry(chatId, prompt, USER);
        final StringBuilder answer = new StringBuilder();
        SseEmitter sseEmitter = new SseEmitter(0L);
        chatClient.prompt().user(prompt).stream()
                .chatResponse()
                .subscribe(response -> processToken(response, sseEmitter, answer),
                        sseEmitter::completeWithError,
                        () -> chatUtils.addChatEntry(chatId, answer.toString(), ASSISTANT));
        return sseEmitter;

    }

    @SneakyThrows
    private static void processToken(ChatResponse response, SseEmitter sseEmitter, StringBuilder answer) {
        AssistantMessage token = response.getResult().getOutput();
        sseEmitter.send(token);
        answer.append(token.getText());

    }
}
