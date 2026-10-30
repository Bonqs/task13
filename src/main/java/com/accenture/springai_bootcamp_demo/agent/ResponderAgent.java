package com.accenture.springai_bootcamp_demo.agent;

import com.accenture.springai_bootcamp_demo.client.OpenRouterException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class ResponderAgent {
    private static final String SYSTEM_PROMPT = """
            You are a customer support agent at NordShop, an online shop.
            Write the reply email to the customer.
            Rules:
            - Use only facts from the KNOWLEDGE BASE. Never invent policies, prices or deadlines.
            - If the knowledge base does not answer the question, say that a colleague will follow up.
            - Never ask for passwords or full card details.
            - Be polite and concise, at most 150 words.
            - Sign the email as "NordShop Support".
            Return only the email text.
            """;

    private final ChatClient chatClient;

    public ResponderAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
    }

    public String writeReply(String ticket, String knowledge) {
        return call(ticket + "\n\nKNOWLEDGE BASE:\n" + knowledge);
    }

    public String improveReply(String ticket, String knowledge, String previousReply, String feedback) {
        return call(ticket
                + "\n\nKNOWLEDGE BASE:\n" + knowledge
                + "\n\nYOUR PREVIOUS REPLY:\n" + previousReply
                + "\n\nREVIEWER FEEDBACK:\n" + feedback
                + "\n\nWrite an improved reply that fixes the feedback.");
    }

    private String call(String prompt) {
        String reply;
        try {
            reply = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (RuntimeException e) {
            throw new OpenRouterException("Responder failed: " + e.getMessage(), e);
        }
        if (reply == null || reply.isBlank()) {
            throw new OpenRouterException("Responder returned an empty reply");
        }
        return reply.trim();
    }
}
