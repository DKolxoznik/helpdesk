package ru.tushkov.helpdesk.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StatusTransitionPolicyTest {

    private StatusTransitionPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new StatusTransitionPolicy();
    }

    @Test
    @DisplayName("NEW можно перевести в работу")
    void allowsNewToInProgress() {
        assertTrue(policy.canTransit(TicketStatus.NEW, TicketStatus.IN_PROGRESS));
    }

    @Test
    @DisplayName("IN_PROGRESS можно завершить")
    void allowsInProgressToResolved() {
        assertTrue(policy.canTransit(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED));
    }

    @Test
    @DisplayName("NEW нельзя закрыть, минуя работу")
    void rejectsNewToResolved() {
        assertFalse(policy.canTransit(TicketStatus.NEW, TicketStatus.RESOLVED));
    }

    @Test
    @DisplayName("Отмена разрешена из NEW")
    void allowsCancelFromNew() {
        assertTrue(policy.canTransit(TicketStatus.NEW, TicketStatus.CANCELLED));
    }

    @Test
    @DisplayName("Отмена разрешена из IN_PROGRESS")
    void allowsCancelFromInProgress() {
        assertTrue(policy.canTransit(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
    }

    @Test
    @DisplayName("Завершённую заявку нельзя отменить")
    void rejectsCancelFromResolved() {
        assertFalse(policy.canTransit(TicketStatus.RESOLVED, TicketStatus.CANCELLED));
    }

    @Test
    @DisplayName("Завершённую заявку нельзя открыть снова")
    void rejectsReopenResolved() {
        assertFalse(policy.canTransit(TicketStatus.RESOLVED, TicketStatus.IN_PROGRESS));
    }

    @Test
    @DisplayName("Повтор того же статуса не является переходом")
    void rejectsSameStatus() {
        assertFalse(policy.canTransit(TicketStatus.NEW, TicketStatus.NEW));
        assertEquals(false, policy.canTransit(TicketStatus.CANCELLED, TicketStatus.NEW));
    }
}
