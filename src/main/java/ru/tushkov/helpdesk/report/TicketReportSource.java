package ru.tushkov.helpdesk.report;

import java.time.Instant;
import java.util.List;

@FunctionalInterface
public interface TicketReportSource {

    List<TicketSnapshot> findCreatedBetween(Instant from, Instant to);
}
