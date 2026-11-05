package ru.tushkov.helpdesk.web;

import java.time.Instant;

public class InvalidReportPeriodException extends RuntimeException {

    public InvalidReportPeriodException(Instant from, Instant to) {
        super("Неверный период отчёта: from должен быть раньше to (" + from + ", " + to + ")");
    }
}
