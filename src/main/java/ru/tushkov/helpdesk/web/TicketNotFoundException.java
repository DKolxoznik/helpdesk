package ru.tushkov.helpdesk.web;

import java.util.UUID;

public class TicketNotFoundException extends RuntimeException {

    public TicketNotFoundException(UUID id) {
        super("Заявка не найдена: " + id);
    }
}
