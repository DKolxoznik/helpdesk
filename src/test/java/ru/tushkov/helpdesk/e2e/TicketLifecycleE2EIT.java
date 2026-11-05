package ru.tushkov.helpdesk.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import ru.tushkov.helpdesk.support.ApiTestSupport;
import ru.tushkov.helpdesk.support.TestClockConfiguration;
import ru.tushkov.helpdesk.ticket.ChangeStatusRequest;
import ru.tushkov.helpdesk.ticket.Priority;
import ru.tushkov.helpdesk.ticket.TicketDetails;
import ru.tushkov.helpdesk.ticket.TicketStatus;
import ru.tushkov.helpdesk.ticket.UpdateTicketRequest;

@Import(TestClockConfiguration.class)
class TicketLifecycleE2EIT extends ApiTestSupport {

    @Test
    @DisplayName("Полный цикл заявки попадает в сводку со средним временем решения")
    void fullLifecycleThenSummary() {
        TicketDetails created = createTicket("Починить доступ", Priority.HIGH, TestClockConfiguration.START);
        assertTrue(created.overdue());
        assertEquals(TicketStatus.NEW, created.status());

        clock.plus(Duration.ofHours(1));
        TicketDetails inProgress = change(created.id(), TicketStatus.IN_PROGRESS);
        assertEquals(TicketStatus.IN_PROGRESS, inProgress.status());
        assertTrue(inProgress.overdue());

        clock.plus(Duration.ofHours(1));
        TicketDetails resolved = change(created.id(), TicketStatus.RESOLVED);
        assertEquals(TicketStatus.RESOLVED, resolved.status());
        assertFalse(resolved.overdue());
        assertEquals(clock.instant(), resolved.closedAt());
        assertEquals(3, resolved.history().size());
        assertEquals(TicketStatus.RESOLVED, resolved.history().get(2).toStatus());

        JsonNode summary = summary(TestClockConfiguration.START, clock.instant().plusSeconds(1));
        assertEquals(1, summary.get("createdCount").asInt());
        assertEquals(1, summary.get("byStatus").get("RESOLVED").asInt());
        assertEquals(1, summary.get("byPriority").get("HIGH").asInt());
        assertEquals(0, summary.get("openOverdueCount").asInt());
        assertEquals(7200.0, summary.get("averageResolutionSeconds").asDouble(), 0.001);
    }

    @Test
    @DisplayName("DEF-001: запрещённые операции над отменённой заявкой не меняют данные")
    void regressionForbiddenOperationDoesNotChangeStoredTicket() {
        TicketDetails created = createTicket("Не трогать", Priority.LOW, null);
        change(created.id(), TicketStatus.CANCELLED);

        String description = column(created.id(), "description");
        String status = column(created.id(), "status");
        String closedAt = column(created.id(), "cast(closed_at as text)");
        int history = historyCount(created.id());

        ResponseEntity<JsonNode> edit = http.exchange(
                "/api/tickets/" + created.id(),
                HttpMethod.PATCH,
                new HttpEntity<>(new UpdateTicketRequest("подмена", Priority.HIGH, null, false)),
                JsonNode.class);
        ResponseEntity<JsonNode> reopen = http.postForEntity(
                "/api/tickets/" + created.id() + "/status",
                new ChangeStatusRequest(TicketStatus.IN_PROGRESS),
                JsonNode.class);

        assertEquals(HttpStatus.CONFLICT, edit.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, reopen.getStatusCode());
        assertEquals(description, column(created.id(), "description"));
        assertEquals(status, column(created.id(), "status"));
        assertEquals(closedAt, column(created.id(), "cast(closed_at as text)"));
        assertEquals(history, historyCount(created.id()));
        assertEquals("CANCELLED", status);
    }

    private TicketDetails change(UUID id, TicketStatus status) {
        ResponseEntity<TicketDetails> response = http.postForEntity(
                "/api/tickets/" + id + "/status", new ChangeStatusRequest(status), TicketDetails.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    private JsonNode summary(Instant from, Instant to) {
        URI uri = UriComponentsBuilder.fromPath("/api/reports/summary")
                .queryParam("from", from.toString())
                .queryParam("to", to.toString())
                .build()
                .toUri();
        ResponseEntity<JsonNode> response = http.getForEntity(uri, JsonNode.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    private String column(UUID id, String expression) {
        return jdbc.queryForObject("select " + expression + " from ticket where id = ?", String.class, id);
    }

    private int historyCount(UUID id) {
        Integer count = jdbc.queryForObject(
                "select count(*) from ticket_status_history where ticket_id = ?", Integer.class, id);
        return count == null ? 0 : count;
    }
}
