package ru.tushkov.helpdesk.report;

import java.time.Instant;
import java.util.UUID;
import org.springframework.lang.Nullable;
import ru.tushkov.helpdesk.ticket.Priority;
import ru.tushkov.helpdesk.ticket.TicketStatus;

public record TicketSnapshot(
        UUID id,
        TicketStatus status,
        Priority priority,
        Instant createdAt,
        @Nullable Instant dueAt,
        @Nullable Instant closedAt) {}
