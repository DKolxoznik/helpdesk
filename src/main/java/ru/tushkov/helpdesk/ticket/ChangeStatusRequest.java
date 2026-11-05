package ru.tushkov.helpdesk.ticket;

import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(@NotNull(message = "Статус обязателен") TicketStatus status) {}
