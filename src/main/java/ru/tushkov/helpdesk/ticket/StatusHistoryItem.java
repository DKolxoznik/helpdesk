package ru.tushkov.helpdesk.ticket;

import java.time.Instant;

public record StatusHistoryItem(TicketStatus fromStatus, TicketStatus toStatus, Instant changedAt) {}
