package ru.tushkov.helpdesk.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
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
import ru.tushkov.helpdesk.ticket.CreateTicketRequest;
import ru.tushkov.helpdesk.ticket.Priority;
import ru.tushkov.helpdesk.ticket.TicketDetails;
import ru.tushkov.helpdesk.ticket.TicketStatus;
import ru.tushkov.helpdesk.ticket.UpdateTicketRequest;

@Import(TestClockConfiguration.class)
class TicketApiIT extends ApiTestSupport {

    @Test
    @DisplayName("Миграции создают справочник из четырёх категорий")
    void migrationsSeedFourCategories() {
        JsonNode body = getOk("/api/categories");
        assertEquals(4, body.size());
        assertTrue(body.toString().contains("NETWORK"));
        assertTrue(body.toString().contains("SOFTWARE"));
    }

    @Test
    @DisplayName("Заявка создаётся в статусе NEW и читается по идентификатору")
    void createAndGetRoundTrip() {
        TicketDetails created = createTicket("Не печатает принтер", Priority.MEDIUM, null);
        assertEquals(TicketStatus.NEW, created.status());
        assertEquals(TestClockConfiguration.START, created.createdAt());
        assertEquals(1, created.history().size());
        assertEquals(TicketStatus.NEW, created.history().get(0).toStatus());

        ResponseEntity<TicketDetails> loaded = http.getForEntity("/api/tickets/" + created.id(), TicketDetails.class);
        assertEquals(HttpStatus.OK, loaded.getStatusCode());
        assertNotNull(loaded.getBody());
        assertEquals(created.id(), loaded.getBody().id());
        assertEquals("Не печатает принтер", loaded.getBody().title());
    }

    @Test
    @DisplayName("Пустой заголовок возвращает 400 и список полей")
    void blankTitleIs400() {
        CreateTicketRequest request = new CreateTicketRequest("  ", null, SOFTWARE, Priority.LOW, null);
        ResponseEntity<JsonNode> response = http.postForEntity("/api/tickets", request, JsonNode.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status").asInt());
        assertTrue(response.getBody().get("details").toString().contains("title"));
        assertEquals(0, countTickets());
    }

    @Test
    @DisplayName("Неизвестная заявка возвращает 404")
    void missingTicketIs404() {
        ResponseEntity<JsonNode> response = http.getForEntity("/api/tickets/" + UUID.randomUUID(), JsonNode.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not Found", response.getBody().get("error").asText());
    }

    @Test
    @DisplayName("Неизвестная категория возвращает 400")
    void unknownCategoryIs400() {
        CreateTicketRequest request = new CreateTicketRequest("Сеть", null, UUID.randomUUID(), Priority.LOW, null);
        ResponseEntity<JsonNode> response = http.postForEntity("/api/tickets", request, JsonNode.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Список фильтруется по статусу и отдаётся постранично")
    void filterByStatusAndPaginate() {
        createTicket("первая", Priority.LOW, null);
        createTicket("вторая", Priority.HIGH, null);
        createTicket("третья", Priority.MEDIUM, null);
        TicketDetails inProgress = createTicket("четвёртая", Priority.LOW, null);
        http.postForEntity(
                "/api/tickets/" + inProgress.id() + "/status",
                new ChangeStatusRequest(TicketStatus.IN_PROGRESS),
                TicketDetails.class);

        JsonNode page = getOk("/api/tickets?status=NEW&page=0&size=2");
        assertEquals(3, page.get("totalElements").asInt());
        assertEquals(2, page.get("content").size());
        assertEquals(2, page.get("totalPages").asInt());

        URI search = UriComponentsBuilder.fromPath("/api/tickets")
                .queryParam("q", "вторая")
                .build()
                .encode()
                .toUri();
        ResponseEntity<JsonNode> searchResponse = http.getForEntity(search, JsonNode.class);
        assertEquals(HttpStatus.OK, searchResponse.getStatusCode());
        JsonNode text = searchResponse.getBody();
        assertNotNull(text);
        assertEquals(1, text.get("totalElements").asInt());
        assertEquals("вторая", text.get("content").get(0).get("title").asText());
    }

    @Test
    @DisplayName("Смена статуса и история сохраняются вместе")
    void statusChangeWritesHistoryInDatabase() {
        TicketDetails created = createTicket("Доступ в VPN", Priority.HIGH, null);
        assertEquals(1, historyCount(created.id()));

        ResponseEntity<TicketDetails> moved = http.postForEntity(
                "/api/tickets/" + created.id() + "/status",
                new ChangeStatusRequest(TicketStatus.IN_PROGRESS),
                TicketDetails.class);
        assertEquals(HttpStatus.OK, moved.getStatusCode());
        assertEquals("IN_PROGRESS", statusOf(created.id()));
        assertEquals(2, historyCount(created.id()));
    }

    @Test
    @DisplayName("Прямое закрытие новой заявки возвращает 409 и не меняет строку")
    void directResolveIs409() {
        TicketDetails created = createTicket("Закрыть сразу", Priority.LOW, null);
        ResponseEntity<JsonNode> response = http.postForEntity(
                "/api/tickets/" + created.id() + "/status",
                new ChangeStatusRequest(TicketStatus.RESOLVED),
                JsonNode.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("NEW", statusOf(created.id()));
        assertEquals(1, historyCount(created.id()));
    }

    @Test
    @DisplayName("Описание, приоритет и срок меняются у открытой заявки")
    void updateDescriptionPriorityAndDueDate() {
        TicketDetails created = createTicket("Обновить", Priority.LOW, null);
        Instant dueAt = TestClockConfiguration.START.plusSeconds(86_400);
        ResponseEntity<TicketDetails> response = http.exchange(
                "/api/tickets/" + created.id(),
                HttpMethod.PATCH,
                new HttpEntity<>(new UpdateTicketRequest("уточнённое описание", Priority.HIGH, dueAt, false)),
                TicketDetails.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("уточнённое описание", response.getBody().description());
        assertEquals(Priority.HIGH, response.getBody().priority());
        assertEquals(dueAt, response.getBody().dueAt());
    }

    @Test
    @DisplayName("Просрочка включается ровно в момент срока")
    void overdueAtExactDueInstant() {
        Instant now = TestClockConfiguration.START;
        TicketDetails dueNow = createTicket("срок сейчас", Priority.MEDIUM, now);
        TicketDetails dueLater = createTicket("срок позже", Priority.MEDIUM, now.plusSeconds(1));
        assertTrue(dueNow.overdue());
        assertFalse(dueLater.overdue());

        JsonNode overdue = getOk("/api/tickets?overdue=true");
        assertEquals(1, overdue.get("totalElements").asInt());
        assertEquals(
                dueNow.id().toString(), overdue.get("content").get(0).get("id").asText());
    }

    @Test
    @DisplayName("Недопустимый размер страницы возвращает 400")
    void invalidPageIs400() {
        ResponseEntity<JsonNode> response = http.getForEntity("/api/tickets?size=0", JsonNode.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    private JsonNode getOk(String path) {
        ResponseEntity<JsonNode> response = http.getForEntity(path, JsonNode.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    private int countTickets() {
        Integer count = jdbc.queryForObject("select count(*) from ticket", Integer.class);
        return count == null ? 0 : count;
    }

    private int historyCount(UUID id) {
        Integer count = jdbc.queryForObject(
                "select count(*) from ticket_status_history where ticket_id = ?", Integer.class, id);
        return count == null ? 0 : count;
    }

    private String statusOf(UUID id) {
        return jdbc.queryForObject("select status from ticket where id = ?", String.class, id);
    }
}
