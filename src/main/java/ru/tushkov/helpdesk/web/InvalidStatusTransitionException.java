package ru.tushkov.helpdesk.web;

import ru.tushkov.helpdesk.ticket.TicketStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(TicketStatus from, TicketStatus to) {
        super("Переход статуса " + from + " -> " + to + " запрещён");
    }
}
