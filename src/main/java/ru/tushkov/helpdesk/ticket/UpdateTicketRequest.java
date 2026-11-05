package ru.tushkov.helpdesk.ticket;

import jakarta.validation.constraints.Size;
import java.time.Instant;

public record UpdateTicketRequest(
        @Size(max = 4000, message = "Описание длиннее 4000 символов") String description,
        Priority priority,
        Instant dueAt,
        boolean clearDueAt) {}
