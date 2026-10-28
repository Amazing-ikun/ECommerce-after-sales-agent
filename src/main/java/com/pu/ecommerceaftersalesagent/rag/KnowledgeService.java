package com.pu.ecommerceaftersalesagent.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class KnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeService.class);
    private static final Pattern SECTION_SPLIT = Pattern.compile("(?m)^(?=## )");
    private static final int MIN_SECTION_LENGTH = 20;

    private final VectorStore vectorStore;
    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
    private final int topK;
    private volatile boolean initialized;

    public KnowledgeService(@Lazy VectorStore vectorStore, @Value("${app.rag.top-k:3}") int topK) {
        this.vectorStore = vectorStore;
        this.topK = topK;
    }

    public List<PolicySnippet> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        ensureInitialized();
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThresholdAll()
                .build());
        return documents.stream()
                .map(document -> new PolicySnippet(
                        String.valueOf(document.getMetadata().getOrDefault("source", "unknown")),
                        String.valueOf(document.getMetadata().getOrDefault("section", "")),
                        document.getText(),
                        document.getScore()))
                .toList();
    }

    private void ensureInitialized() {
        if (initialized) {
            return;
        }
        synchronized (this) {
            if (initialized) {
                return;
            }
            List<Document> documents = loadDocuments();
            if (!documents.isEmpty()) {
                vectorStore.add(documents);
            }
            initialized = true;
            log.info("售后政策知识库加载完成，共 {} 个片段", documents.size());
        }
    }

    private List<Document> loadDocuments() {
        List<Document> documents = new ArrayList<>();
        try {
            Resource[] resources = resourceResolver.getResources("classpath*:knowledge/*.md");
            Arrays.sort(resources, Comparator.comparing(resource -> String.valueOf(resource.getFilename())));
            for (Resource resource : resources) {
                String filename = resource.getFilename() == null ? "unknown.md" : resource.getFilename();
                String text = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                documents.addAll(splitSections(filename, text));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("读取政策知识库失败", exception);
        }
        return documents;
    }

    private List<Document> splitSections(String filename, String text) {
        String documentTitle = extractDocumentTitle(text, filename);
        List<Document> documents = new ArrayList<>();
        for (String section : SECTION_SPLIT.split(text)) {
            String trimmed = section.strip();
            if (trimmed.length() < MIN_SECTION_LENGTH) {
                continue;
            }
            documents.add(new Document(trimmed, Map.of(
                    "source", filename,
                    "section", extractHeading(trimmed, documentTitle))));
        }
        return documents;
    }

    private String extractDocumentTitle(String text, String fallback) {
        return text.lines()
                .filter(line -> line.startsWith("# "))
                .findFirst()
                .map(line -> line.substring(2).strip())
                .filter(title -> !title.isBlank())
                .orElse(fallback);
    }

    private String extractHeading(String section, String fallback) {
        String firstLine = section.lines().findFirst().orElse("").strip();
        if (firstLine.startsWith("## ")) {
            return firstLine.substring(3).strip();
        }
        if (firstLine.startsWith("# ")) {
            return firstLine.substring(2).strip();
        }
        return fallback;
    }
}
