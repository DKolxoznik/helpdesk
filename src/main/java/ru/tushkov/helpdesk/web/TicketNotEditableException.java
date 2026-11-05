package ru.tushkov.helpdesk.web;

import java.util.UUID;

public class TicketNotEditableException extends RuntimeException {

    public TicketNotEditableException(UUID id) {
        super("Закрытую или отменённую заявку нельзя редактировать: " + id);
    }
}
