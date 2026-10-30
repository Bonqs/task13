package com.accenture.springai_bootcamp_demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "tickets")
@NoArgsConstructor
@Data
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerEmail;
    private String subject;

    @Column(length = 8000)
    private String description;

    @Enumerated(EnumType.STRING)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    private TicketPriority priority;

    @Column(length = 1000)
    private String summary;

    @Column(length = 8000)
    private String suggestedReply;

    @Column(length = 2000)
    private String reviewerFeedback;

    private int reviewRounds;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    private Instant createdAt;

    public Ticket(String customerEmail, String subject, String description) {
        this.customerEmail = customerEmail;
        this.subject = subject;
        this.description = description;
        this.createdAt = Instant.now();
    }
}
