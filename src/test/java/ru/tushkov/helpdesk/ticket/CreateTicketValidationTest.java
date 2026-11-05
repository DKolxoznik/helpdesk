package ru.tushkov.helpdesk.ticket;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CreateTicketValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Пустой заголовок не проходит проверку")
    void rejectsBlankTitle() {
        CreateTicketRequest request = new CreateTicketRequest("   ", null, UUID.randomUUID(), Priority.LOW, null);
        assertTrue(validator.validate(request).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("title")));
    }

    @Test
    @DisplayName("Без категории заявка невалидна")
    void rejectsMissingCategory() {
        CreateTicketRequest request = new CreateTicketRequest("Принтер", null, null, Priority.MEDIUM, null);
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    @DisplayName("Без приоритета заявка невалидна")
    void rejectsMissingPriority() {
        CreateTicketRequest request = new CreateTicketRequest("Принтер", null, UUID.randomUUID(), null, null);
        assertTrue(validator.validate(request).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("priority")));
    }
}
