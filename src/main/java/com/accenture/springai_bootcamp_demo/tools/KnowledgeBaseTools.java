package com.accenture.springai_bootcamp_demo.tools;

import com.accenture.springai_bootcamp_demo.knowledge.KnowledgeBase;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeBaseTools {
    private final KnowledgeBase knowledgeBase;

    public KnowledgeBaseTools(KnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    @Tool(description = "Searches NordShop policies about refunds, returns, shipping, delivery, "
            + "account security, passwords, payments and invoices")
    public String searchPolicies(@ToolParam(description = "Short search query in English") String query) {
        return knowledgeBase.searchAsText(query, 3);
    }
}
