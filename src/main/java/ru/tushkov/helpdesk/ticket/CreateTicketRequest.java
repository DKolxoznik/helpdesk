package ru.tushkov.helpdesk.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record CreateTicketRequest(
        @NotBlank(message = "Заголовок обязателен") @Size(max = 200, message = "Заголовок длиннее 200 символов")
                String title,
        @Size(max = 4000, message = "Описание длиннее 4000 символов") String description,
        @NotNull(message = "Категория обязательна") UUID categoryId,
        @NotNull(message = "Приоритет обязателен") Priority priority,
        Instant dueAt) {}
