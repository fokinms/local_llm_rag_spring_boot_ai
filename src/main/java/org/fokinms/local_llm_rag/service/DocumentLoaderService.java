package org.fokinms.local_llm_rag.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.fokinms.local_llm_rag.model.LoadedDocument;
import org.fokinms.local_llm_rag.repository.DocumentRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentLoaderService implements CommandLineRunner {

    private final DocumentRepository documentRepository;

    private final ResourcePatternResolver resourcePatternResolver;

    private final VectorStore vectorStore;

    @SneakyThrows
    public void loadDocuments() {
         List<Resource> resources = Arrays.stream(resourcePatternResolver.getResources("classpath:/knowledgebase/**/*.txt"))
                 .toList();

         resources.stream()
                 .map(resource -> Pair.of(resource, calcContentHash(resource)))
                 .filter(pair -> !documentRepository.existsByFilenameAndContentHash(pair.getFirst().getFilename(), pair.getSecond()))
                 .forEach(pair -> {
                     Resource resource = pair.getFirst();
                     List<Document> documents = new TextReader(resource).get();
                     TokenTextSplitter splitter = TokenTextSplitter.builder().withChunkSize(500).build();
                     List<Document> chunks = splitter.apply(documents);
                     vectorStore.accept(chunks);

                     LoadedDocument loadedDocument = LoadedDocument.builder()
                             .documentType("txt")
                             .chunkCount(chunks.size())
                             .filename(resource.getFilename())
                             .contentHash(pair.getSecond())
                             .build();
                    documentRepository.save(loadedDocument);
                 });
    }

    @SneakyThrows
    private String calcContentHash(Resource resource) {
        return DigestUtils.md5DigestAsHex(resource.getInputStream());
    }

    @Override
    public void run(String... args) {
        loadDocuments();
    }
}
