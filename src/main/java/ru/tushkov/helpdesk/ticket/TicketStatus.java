package ru.tushkov.helpdesk.ticket;

public enum TicketStatus {
    NEW,
    IN_PROGRESS,
    RESOLVED,
    CANCELLED;

    public boolean isTerminal() {
        return this == RESOLVED || this == CANCELLED;
    }
}
