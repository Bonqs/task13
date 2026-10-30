package com.accenture.springai_bootcamp_demo.service;

import com.accenture.springai_bootcamp_demo.agent.ClassifierAgent;
import com.accenture.springai_bootcamp_demo.agent.ResponderAgent;
import com.accenture.springai_bootcamp_demo.agent.ReviewVerdict;
import com.accenture.springai_bootcamp_demo.agent.ReviewerAgent;
import com.accenture.springai_bootcamp_demo.agent.TicketClassification;
import com.accenture.springai_bootcamp_demo.dto.CreateTicketRequest;
import com.accenture.springai_bootcamp_demo.dto.TicketResponse;
import com.accenture.springai_bootcamp_demo.entity.Ticket;
import com.accenture.springai_bootcamp_demo.entity.TicketPriority;
import com.accenture.springai_bootcamp_demo.entity.TicketStatus;
import com.accenture.springai_bootcamp_demo.exception.TicketNotFoundException;
import com.accenture.springai_bootcamp_demo.knowledge.KnowledgeBase;
import com.accenture.springai_bootcamp_demo.repository.TicketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TicketService {
    private static final int MAX_REVIEW_ROUNDS = 2;
    private static final int KNOWLEDGE_RESULTS = 3;

    private final TicketRepository ticketRepository;
    private final ClassifierAgent classifierAgent;
    private final ResponderAgent responderAgent;
    private final ReviewerAgent reviewerAgent;
    private final KnowledgeBase knowledgeBase;

    public TicketService(TicketRepository ticketRepository, ClassifierAgent classifierAgent,
                         ResponderAgent responderAgent, ReviewerAgent reviewerAgent, KnowledgeBase knowledgeBase) {
        this.ticketRepository = ticketRepository;
        this.classifierAgent = classifierAgent;
        this.responderAgent = responderAgent;
        this.reviewerAgent = reviewerAgent;
        this.knowledgeBase = knowledgeBase;
    }

    public TicketResponse createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket(request.customerEmail(), request.subject(), request.description());
        String ticketText = "Subject: " + request.subject() + "\nMessage: " + request.description();

        TicketClassification classification = classifierAgent.classify(ticketText);
        ticket.setCategory(classification.category());
        ticket.setPriority(classification.priority());
        ticket.setSummary(classification.summary());

        String knowledge = knowledgeBase.searchAsText(
                request.subject() + " " + request.description() + " " + classification.category(), KNOWLEDGE_RESULTS);

        String reply = responderAgent.writeReply(ticketText, knowledge);
        ReviewVerdict verdict = reviewerAgent.review(ticketText, knowledge, reply);
        int reviewRounds = 1;

        while (!verdict.approved() && reviewRounds < MAX_REVIEW_ROUNDS) {
            reply = responderAgent.improveReply(ticketText, knowledge, reply, verdict.feedback());
            verdict = reviewerAgent.review(ticketText, knowledge, reply);
            reviewRounds++;
        }

        ticket.setSuggestedReply(reply);
        ticket.setReviewerFeedback(verdict.feedback());
        ticket.setReviewRounds(reviewRounds);
        if (verdict.approved() && classification.priority() != TicketPriority.URGENT) {
            ticket.setStatus(TicketStatus.REPLY_APPROVED);
        } else {
            ticket.setStatus(TicketStatus.NEEDS_HUMAN_REVIEW);
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Ticket {} created with status {}", savedTicket.getId(), savedTicket.getStatus());
        return mapToResponse(savedTicket);
    }

    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public TicketResponse getTicketById(Long id) {
        return ticketRepository.findById(id).map(this::mapToResponse).orElseThrow(
                () -> new TicketNotFoundException(id)
        );
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getCustomerEmail(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getSummary(),
                ticket.getSuggestedReply(),
                ticket.getReviewerFeedback(),
                ticket.getReviewRounds(),
                ticket.getStatus(),
                ticket.getCreatedAt()
        );
    }
}
