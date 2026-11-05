package ru.tushkov.helpdesk.report;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.tushkov.helpdesk.ticket.TicketRepository;

@Component
public class TicketReportAdapter implements TicketReportSource {

    private final TicketRepository tickets;

    public TicketReportAdapter(TicketRepository tickets) {
        this.tickets = tickets;
    }

    @Override
    public List<TicketSnapshot> findCreatedBetween(Instant from, Instant to) {
        return tickets.findCreatedBetween(from, to).stream()
                .map(ticket -> new TicketSnapshot(
                        ticket.getId(),
                        ticket.getStatus(),
                        ticket.getPriority(),
                        ticket.getCreatedAt(),
                        ticket.getDueAt(),
                        ticket.getClosedAt()))
                .toList();
    }
}
