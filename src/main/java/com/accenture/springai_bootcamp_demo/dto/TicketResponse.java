package com.accenture.springai_bootcamp_demo.dto;

import com.accenture.springai_bootcamp_demo.entity.TicketCategory;
import com.accenture.springai_bootcamp_demo.entity.TicketPriority;
import com.accenture.springai_bootcamp_demo.entity.TicketStatus;

import java.time.Instant;

public record TicketResponse(
        Long id,
        String customerEmail,
        String subject,
        String description,
        TicketCategory category,
        TicketPriority priority,
        String summary,
        String suggestedReply,
        String reviewerFeedback,
        int reviewRounds,
        TicketStatus status,
        Instant createdAt) {
}
