package ru.tushkov.helpdesk.ticket;

import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class OverduePolicy {

    /**
     * Срок считается наступившим включительно: заявка просрочена при {@code dueAt <= now},
     * если она ещё открыта. Закрытые и отменённые заявки просроченными не считаются.
     */
    public boolean isOverdue(TicketStatus status, Instant dueAt, Instant now) {
        if (dueAt == null || now == null || status == null || status.isTerminal()) {
            return false;
        }
        return !dueAt.isAfter(now);
    }
}
