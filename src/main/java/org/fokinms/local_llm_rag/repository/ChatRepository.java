package org.fokinms.local_llm_rag.repository;

import org.fokinms.local_llm_rag.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
}
