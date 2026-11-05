package ru.tushkov.helpdesk.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tushkov.helpdesk.category.Category;
import ru.tushkov.helpdesk.category.CategoryRepository;
import ru.tushkov.helpdesk.web.CategoryNotFoundException;
import ru.tushkov.helpdesk.web.InvalidStatusTransitionException;
import ru.tushkov.helpdesk.web.TicketNotEditableException;
import ru.tushkov.helpdesk.web.TicketNotFoundException;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-15T12:00:00Z");

    @Mock
    private TicketRepository tickets;

    @Mock
    private CategoryRepository categories;

    @Mock
    private StatusHistoryRepository histories;

    private TicketService service;
    private final List<StatusHistory> storedHistory = new ArrayList<>();

    @BeforeEach
    void setUp() {
        storedHistory.clear();
        service = new TicketService(
                tickets,
                categories,
                histories,
                new StatusTransitionPolicy(),
                new OverduePolicy(),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("Новая заявка получает статус NEW и первую запись истории")
    void createAssignsNewAndHistory() {
        UUID categoryId = UUID.randomUUID();
        Category category = category(categoryId);
        when(categories.findById(categoryId)).thenReturn(Optional.of(category));
        when(tickets.saveAndFlush(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));
        rememberHistory();

        TicketDetails created = service.create(
                new CreateTicketRequest("  Сеть  ", "  нет пинга  ", categoryId, Priority.HIGH, NOW.plusSeconds(3600)));

        assertEquals("Сеть", created.title());
        assertEquals("нет пинга", created.description());
        assertEquals(TicketStatus.NEW, created.status());
        assertEquals(NOW, created.createdAt());
        assertNull(created.closedAt());
        assertEquals(1, created.history().size());
        assertNull(created.history().get(0).fromStatus());
        assertEquals(TicketStatus.NEW, created.history().get(0).toStatus());
    }

    @Test
    @DisplayName("Неизвестная категория отклоняется до сохранения")
    void unknownCategory() {
        UUID categoryId = UUID.randomUUID();
        when(categories.findById(categoryId)).thenReturn(Optional.empty());
        assertThrows(
                CategoryNotFoundException.class,
                () -> service.create(new CreateTicketRequest("Сеть", null, categoryId, Priority.LOW, null)));
        verify(tickets, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Отсутствующая заявка даёт ошибку поиска")
    void missingTicketReturnsNotFound() {
        UUID id = UUID.randomUUID();
        when(tickets.findById(id)).thenReturn(Optional.empty());
        assertThrows(TicketNotFoundException.class, () -> service.get(id));
    }

    @Test
    @DisplayName("Запрещённый переход не пишет историю и не меняет статус")
    void forbiddenTransitionDoesNotWriteHistory() {
        UUID id = UUID.randomUUID();
        Ticket ticket = ticket(id, TicketStatus.NEW, "исходный");
        when(tickets.findById(id)).thenReturn(Optional.of(ticket));
        assertThrows(InvalidStatusTransitionException.class, () -> service.changeStatus(id, TicketStatus.RESOLVED));
        assertEquals(TicketStatus.NEW, ticket.getStatus());
        verify(histories, never()).save(any());
    }

    @Test
    @DisplayName("Завершение ставит время закрытия и запись истории в одном вызове")
    void resolveSetsClosedAtAndHistory() {
        UUID id = UUID.randomUUID();
        Ticket ticket = ticket(id, TicketStatus.IN_PROGRESS, "описание");
        when(tickets.findById(id)).thenReturn(Optional.of(ticket));
        rememberHistory();

        TicketDetails updated = service.changeStatus(id, TicketStatus.RESOLVED);

        assertEquals(TicketStatus.RESOLVED, updated.status());
        assertEquals(NOW, updated.closedAt());
        assertEquals(TicketStatus.IN_PROGRESS, updated.history().get(0).fromStatus());
        assertEquals(TicketStatus.RESOLVED, updated.history().get(0).toStatus());
    }

    @Test
    @DisplayName("Отмена из NEW фиксирует время закрытия")
    void cancelFromNewSetsClosedAt() {
        UUID id = UUID.randomUUID();
        Ticket ticket = ticket(id, TicketStatus.NEW, "описание");
        when(tickets.findById(id)).thenReturn(Optional.of(ticket));
        rememberHistory();
        TicketDetails updated = service.changeStatus(id, TicketStatus.CANCELLED);
        assertEquals(TicketStatus.CANCELLED, updated.status());
        assertEquals(NOW, ticket.getClosedAt());
    }

    @Test
    @DisplayName("DEF-001: завершённую заявку нельзя изменить")
    void regressionResolvedTicketIsNotModified() {
        UUID id = UUID.randomUUID();
        Ticket ticket = ticket(id, TicketStatus.RESOLVED, "исходное описание");
        when(tickets.findById(id)).thenReturn(Optional.of(ticket));
        UpdateTicketRequest request = new UpdateTicketRequest("подмена", Priority.HIGH, NOW.plusSeconds(10), false);

        assertThrows(TicketNotEditableException.class, () -> service.update(id, request));

        assertEquals("исходное описание", ticket.getDescription());
        assertEquals(Priority.LOW, ticket.getPriority());
        verify(tickets, never()).save(any());
    }

    private void rememberHistory() {
        org.mockito.Mockito.when(histories.save(any(StatusHistory.class))).thenAnswer(invocation -> {
            StatusHistory history = invocation.getArgument(0);
            storedHistory.add(history);
            return history;
        });
        when(histories.findByTicketIdOrderByChangedAtAsc(any())).thenAnswer(invocation -> List.copyOf(storedHistory));
    }

    private static Category category(UUID id) {
        return Category.of(id, "NETWORK", "Сеть");
    }

    private static Ticket ticket(UUID id, TicketStatus status, String description) {
        Category category = category(UUID.randomUUID());
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setTitle("Заявка");
        ticket.setDescription(description);
        ticket.setCategory(category);
        ticket.setPriority(Priority.LOW);
        ticket.setStatus(status);
        ticket.setCreatedAt(NOW.minusSeconds(60));
        return ticket;
    }
}
