package ru.tushkov.helpdesk.ticket;

import java.time.Instant;
import java.util.UUID;
import ru.tushkov.helpdesk.category.CategoryResponse;

public record TicketSummary(
        UUID id,
        String title,
        CategoryResponse category,
        Priority priority,
        TicketStatus status,
        Instant createdAt,
        Instant dueAt,
        Instant closedAt,
        boolean overdue) {}
