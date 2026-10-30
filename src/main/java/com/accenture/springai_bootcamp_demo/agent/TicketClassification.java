package com.accenture.springai_bootcamp_demo.agent;

import com.accenture.springai_bootcamp_demo.entity.TicketCategory;
import com.accenture.springai_bootcamp_demo.entity.TicketPriority;

public record TicketClassification(TicketCategory category, TicketPriority priority, String summary) {
}
