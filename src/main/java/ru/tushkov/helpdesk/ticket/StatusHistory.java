package ru.tushkov.helpdesk.ticket;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_status_history")
public class StatusHistory {

    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 16)
    private TicketStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 16)
    private TicketStatus toStatus;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    protected StatusHistory() {}

    private StatusHistory(UUID id, Ticket ticket, TicketStatus fromStatus, TicketStatus toStatus, Instant changedAt) {
        this.id = id;
        this.ticket = ticket;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedAt = changedAt;
    }

    public static StatusHistory opened(Ticket ticket, Instant changedAt) {
        return new StatusHistory(UUID.randomUUID(), ticket, null, TicketStatus.NEW, changedAt);
    }

    public static StatusHistory transition(
            Ticket ticket, TicketStatus fromStatus, TicketStatus toStatus, Instant changedAt) {
        return new StatusHistory(UUID.randomUUID(), ticket, fromStatus, toStatus, changedAt);
    }

    public UUID getId() {
        return id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public TicketStatus getFromStatus() {
        return fromStatus;
    }

    public TicketStatus getToStatus() {
        return toStatus;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
