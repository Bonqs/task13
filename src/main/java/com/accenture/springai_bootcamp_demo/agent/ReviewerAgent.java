package com.accenture.springai_bootcamp_demo.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReviewerAgent {
    private static final String SYSTEM_PROMPT = """
            You are a strict reviewer of NordShop customer support emails.
            Approve the reply only if ALL of these are true:
            - every fact (deadlines, prices, steps) is supported by the KNOWLEDGE BASE
            - it answers the customer's question
            - it does not ask for passwords or full card details
            - it is polite and signed as "NordShop Support"
            Otherwise set approved to false and write short feedback on what must be fixed.
            When approved, feedback must be an empty string.
            """;

    private final ChatClient chatClient;

    public ReviewerAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
    }

    public ReviewVerdict review(String ticket, String knowledge, String reply) {
        try {
            ReviewVerdict verdict = chatClient.prompt()
                    .user(ticket + "\n\nKNOWLEDGE BASE:\n" + knowledge + "\n\nREPLY:\n" + reply)
                    .call()
                    .entity(ReviewVerdict.class);
            if (verdict == null) {
                return new ReviewVerdict(false, "Reviewer returned no result");
            }
            return verdict;
        } catch (RuntimeException e) {
            log.warn("Reviewer failed: {}", e.getMessage());
            return new ReviewVerdict(false, "Reviewer could not check the reply");
        }
    }
}
