package com.accenture.springai_bootcamp_demo.knowledge;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class KnowledgeBase {
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "you", "your", "are", "was", "but", "not", "with", "this", "that",
            "have", "has", "how", "what", "when", "can", "could", "would", "please", "from", "about",
            "they", "our", "any", "all", "get", "got", "its", "why", "will", "just", "still", "did", "does");

    private final List<KnowledgeChunk> chunks = new ArrayList<>();

    @PostConstruct
    public void load() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath:knowledge/*.md");
            for (Resource resource : resources) {
                chunks.addAll(split(resource.getContentAsString(StandardCharsets.UTF_8)));
            }
            log.info("Knowledge base loaded: {} sections", chunks.size());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load knowledge base", e);
        }
    }

    public List<KnowledgeChunk> search(String query, int limit) {
        Set<String> queryWords = new HashSet<>();
        for (String word : toWords(query)) {
            if (word.length() > 2 && !STOP_WORDS.contains(word)) {
                queryWords.add(word);
            }
        }

        Map<KnowledgeChunk, Integer> scores = new HashMap<>();
        for (KnowledgeChunk chunk : chunks) {
            int score = 3 * countMatches(toWords(chunk.title()), queryWords) + countMatches(toWords(chunk.content()), queryWords);
            if (score > 0) {
                scores.put(chunk, score);
            }
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<KnowledgeChunk, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    public String searchAsText(String query, int limit) {
        List<KnowledgeChunk> found = search(query, limit);
        if (found.isEmpty()) {
            return "No relevant sections found.";
        }
        StringBuilder text = new StringBuilder();
        for (KnowledgeChunk chunk : found) {
            text.append("[").append(chunk.title()).append("]\n").append(chunk.content()).append("\n\n");
        }
        return text.toString().trim();
    }

    private List<KnowledgeChunk> split(String markdown) {
        List<KnowledgeChunk> result = new ArrayList<>();
        String documentTitle = "";
        String sectionTitle = null;
        StringBuilder body = new StringBuilder();

        for (String line : markdown.split("\\R")) {
            if (line.startsWith("# ")) {
                documentTitle = line.substring(2).trim();
            } else if (line.startsWith("## ")) {
                if (sectionTitle != null) {
                    result.add(new KnowledgeChunk(documentTitle + " / " + sectionTitle, body.toString().trim()));
                }
                sectionTitle = line.substring(3).trim();
                body.setLength(0);
            } else {
                body.append(line).append("\n");
            }
        }
        if (sectionTitle != null) {
            result.add(new KnowledgeChunk(documentTitle + " / " + sectionTitle, body.toString().trim()));
        }
        return result;
    }

    private int countMatches(List<String> words, Set<String> queryWords) {
        int count = 0;
        for (String word : words) {
            for (String queryWord : queryWords) {
                if (word.startsWith(queryWord) || (word.length() > 3 && queryWord.startsWith(word))) {
                    count++;
                }
            }
        }
        return count;
    }

    private List<String> toWords(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return Arrays.asList(text.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim().split(" "));
    }
}
