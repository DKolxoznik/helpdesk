package ru.tushkov.helpdesk.ticket;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import ru.tushkov.helpdesk.category.CategoryResponse;

public record TicketDetails(
        UUID id,
        String title,
        String description,
        CategoryResponse category,
        Priority priority,
        TicketStatus status,
        Instant createdAt,
        Instant dueAt,
        Instant closedAt,
        boolean overdue,
        List<StatusHistoryItem> history) {}
