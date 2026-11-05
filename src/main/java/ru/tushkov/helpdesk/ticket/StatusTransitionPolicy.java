package ru.tushkov.helpdesk.ticket;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class StatusTransitionPolicy {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED = Map.of(
            TicketStatus.NEW, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
            TicketStatus.IN_PROGRESS, EnumSet.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED),
            TicketStatus.RESOLVED, EnumSet.noneOf(TicketStatus.class),
            TicketStatus.CANCELLED, EnumSet.noneOf(TicketStatus.class));

    public boolean canTransit(TicketStatus from, TicketStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }
}
