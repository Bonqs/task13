package com.accenture.springai_bootcamp_demo.tools;

import com.accenture.springai_bootcamp_demo.entity.Ticket;
import com.accenture.springai_bootcamp_demo.repository.TicketRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class TicketTools {
    private final TicketRepository ticketRepository;

    public TicketTools(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Tool(description = "Returns the status, category, priority and summary of a NordShop support ticket by its id")
    public String getTicketStatus(@ToolParam(description = "Ticket id") Long id) {
        return ticketRepository.findById(id)
                .map(this::describe)
                .orElse("Ticket with id " + id + " not found");
    }

    private String describe(Ticket ticket) {
        return String.format("Ticket %d: subject '%s', status %s, category %s, priority %s, summary: %s",
                ticket.getId(), ticket.getSubject(), ticket.getStatus(), ticket.getCategory(),
                ticket.getPriority(), ticket.getSummary());
    }
}
