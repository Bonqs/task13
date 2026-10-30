package com.accenture.springai_bootcamp_demo.agent;

import com.accenture.springai_bootcamp_demo.client.OpenRouterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClassifierAgent {
    private static final int MAX_ATTEMPTS = 3;
    private static final String SYSTEM_PROMPT = """
            You are a support ticket classifier for NordShop, an online shop.
            Classify the customer's ticket.

            category:
            - REFUND: returns, refunds, damaged or wrong items
            - SHIPPING: delivery times, tracking, lost parcels, delivery address
            - ACCOUNT: login, password, locked account, security, deleting the account
            - PAYMENT: charges, double charge, invoices, payment methods
            - OTHER: anything else

            priority:
            - URGENT: security issues, fraud, orders the customer did not place
            - HIGH: customer lost money, lost parcel, damaged or wrong item
            - MEDIUM: problem with an existing order
            - LOW: general question

            summary: one sentence in English, at most 25 words.
            """;

    private final ChatClient chatClient;

    public ClassifierAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
    }

    public TicketClassification classify(String ticket) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                TicketClassification classification = chatClient.prompt()
                        .user(ticket)
                        .call()
                        .entity(TicketClassification.class);
                if (isValid(classification)) {
                    return classification;
                }
                log.warn("Classifier returned invalid result (attempt {}): {}", attempt, classification);
            } catch (RuntimeException e) {
                log.warn("Classifier failed (attempt {}): {}", attempt, e.getMessage());
            }
        }
        throw new OpenRouterException("Classifier could not classify the ticket after " + MAX_ATTEMPTS + " attempts");
    }

    private boolean isValid(TicketClassification classification) {
        return classification != null
                && classification.category() != null
                && classification.priority() != null
                && classification.summary() != null
                && !classification.summary().isBlank();
    }
}
