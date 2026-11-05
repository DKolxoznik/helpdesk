package ru.tushkov.helpdesk.ticket;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tushkov.helpdesk.category.Category;
import ru.tushkov.helpdesk.category.CategoryRepository;
import ru.tushkov.helpdesk.category.CategoryResponse;
import ru.tushkov.helpdesk.web.CategoryNotFoundException;
import ru.tushkov.helpdesk.web.InvalidPageRequestException;
import ru.tushkov.helpdesk.web.InvalidStatusTransitionException;
import ru.tushkov.helpdesk.web.PageResponse;
import ru.tushkov.helpdesk.web.TicketNotEditableException;
import ru.tushkov.helpdesk.web.TicketNotFoundException;

@Service
public class TicketService {

    private final TicketRepository tickets;
    private final CategoryRepository categories;
    private final StatusHistoryRepository histories;
    private final StatusTransitionPolicy transitions;
    private final OverduePolicy overduePolicy;
    private final Clock clock;

    public TicketService(
            TicketRepository tickets,
            CategoryRepository categories,
            StatusHistoryRepository histories,
            StatusTransitionPolicy transitions,
            OverduePolicy overduePolicy,
            Clock clock) {
        this.tickets = tickets;
        this.categories = categories;
        this.histories = histories;
        this.transitions = transitions;
        this.overduePolicy = overduePolicy;
        this.clock = clock;
    }

    @Transactional
    public TicketDetails create(CreateTicketRequest request) {
        Category category = categories
                .findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
        Instant now = clock.instant();
        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setTitle(request.title().trim());
        ticket.setDescription(normalizeDescription(request.description()));
        ticket.setCategory(category);
        ticket.setPriority(request.priority());
        ticket.setStatus(TicketStatus.NEW);
        ticket.setCreatedAt(now);
        ticket.setDueAt(request.dueAt());
        tickets.saveAndFlush(ticket);
        histories.save(StatusHistory.opened(ticket, now));
        return toDetails(ticket);
    }

    @Transactional(readOnly = true)
    public TicketDetails get(UUID id) {
        return toDetails(required(id));
    }

    @Transactional
    public TicketDetails update(UUID id, UpdateTicketRequest request) {
        Ticket ticket = required(id);
        if (ticket.getStatus().isTerminal()) {
            throw new TicketNotEditableException(id);
        }
        if (request.description() != null) {
            ticket.setDescription(normalizeDescription(request.description()));
        }
        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }
        if (request.clearDueAt()) {
            ticket.setDueAt(null);
        } else if (request.dueAt() != null) {
            ticket.setDueAt(request.dueAt());
        }
        return toDetails(ticket);
    }

    @Transactional
    public TicketDetails changeStatus(UUID id, TicketStatus target) {
        Ticket ticket = required(id);
        TicketStatus current = ticket.getStatus();
        if (!transitions.canTransit(current, target)) {
            throw new InvalidStatusTransitionException(current, target);
        }
        Instant now = clock.instant();
        ticket.setStatus(target);
        if (target.isTerminal()) {
            ticket.setClosedAt(now);
        }
        histories.save(StatusHistory.transition(ticket, current, target, now));
        return toDetails(ticket);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketSummary> list(TicketFilter filter, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidPageRequestException();
        }
        Page<Ticket> result = tickets.findAll(
                TicketSpecifications.matching(filter, clock.instant()),
                PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id"))));
        return PageResponse.from(result.map(this::toSummary));
    }

    private Ticket required(UUID id) {
        return tickets.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
    }

    private TicketDetails toDetails(Ticket ticket) {
        List<StatusHistoryItem> history = histories.findByTicketIdOrderByChangedAtAsc(ticket.getId()).stream()
                .map(item -> new StatusHistoryItem(item.getFromStatus(), item.getToStatus(), item.getChangedAt()))
                .toList();
        return new TicketDetails(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                toCategory(ticket.getCategory()),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getDueAt(),
                ticket.getClosedAt(),
                overduePolicy.isOverdue(ticket.getStatus(), ticket.getDueAt(), clock.instant()),
                history);
    }

    private TicketSummary toSummary(Ticket ticket) {
        return new TicketSummary(
                ticket.getId(),
                ticket.getTitle(),
                toCategory(ticket.getCategory()),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getDueAt(),
                ticket.getClosedAt(),
                overduePolicy.isOverdue(ticket.getStatus(), ticket.getDueAt(), clock.instant()));
    }

    private static CategoryResponse toCategory(Category category) {
        return new CategoryResponse(category.getId(), category.getCode(), category.getName());
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
