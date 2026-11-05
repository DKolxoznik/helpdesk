package ru.tushkov.helpdesk.ticket;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OverduePolicyTest {

    private final OverduePolicy policy = new OverduePolicy();
    private final Instant dueAt = Instant.parse("2026-06-15T12:00:00Z");

    @Test
    @DisplayName("До наступления срока заявка не просрочена")
    void falseBeforeDeadline() {
        assertFalse(policy.isOverdue(TicketStatus.NEW, dueAt, dueAt.minusNanos(1)));
    }

    @Test
    @DisplayName("В момент наступления срока заявка просрочена")
    void trueAtDeadline() {
        assertTrue(policy.isOverdue(TicketStatus.IN_PROGRESS, dueAt, dueAt));
    }

    @Test
    @DisplayName("После срока открытая заявка просрочена")
    void trueAfterDeadline() {
        assertTrue(policy.isOverdue(TicketStatus.NEW, dueAt, dueAt.plusSeconds(1)));
    }

    @Test
    @DisplayName("Без срока заявка не просрочена")
    void falseWithoutDeadline() {
        assertFalse(policy.isOverdue(TicketStatus.NEW, null, dueAt));
    }

    @Test
    @DisplayName("Завершённая заявка не считается просроченной")
    void falseWhenResolved() {
        assertFalse(policy.isOverdue(TicketStatus.RESOLVED, dueAt, dueAt.plusSeconds(3600)));
    }

    @Test
    @DisplayName("Отменённая заявка не считается просроченной")
    void falseWhenCancelled() {
        assertFalse(policy.isOverdue(TicketStatus.CANCELLED, dueAt, dueAt.plusSeconds(3600)));
    }
}
