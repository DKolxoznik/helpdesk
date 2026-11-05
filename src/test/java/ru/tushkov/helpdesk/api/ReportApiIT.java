package ru.tushkov.helpdesk.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import ru.tushkov.helpdesk.support.ApiTestSupport;
import ru.tushkov.helpdesk.support.TestClockConfiguration;
import ru.tushkov.helpdesk.ticket.Priority;

@Import(TestClockConfiguration.class)
class ReportApiIT extends ApiTestSupport {

    @Test
    @DisplayName("Пустой период возвращает нули и null вместо среднего")
    void emptyPeriodHasNullAverage() {
        JsonNode body =
                summary(TestClockConfiguration.START.plusSeconds(3600), TestClockConfiguration.START.plusSeconds(7200));
        assertEquals(0, body.get("createdCount").asInt());
        assertTrue(body.get("averageResolutionSeconds").isNull());
        assertEquals(0, body.get("openOverdueCount").asInt());
        assertEquals(0, body.get("byStatus").get("NEW").asInt());
        assertEquals(0, body.get("byPriority").get("LOW").asInt());
    }

    @Test
    @DisplayName("Неверный период отчёта возвращает 400")
    void invalidPeriodIs400() {
        Instant from = TestClockConfiguration.START;
        ResponseEntity<JsonNode> equal = http.getForEntity(summaryUri(from, from), JsonNode.class);
        ResponseEntity<JsonNode> reversed = http.getForEntity(summaryUri(from.plusSeconds(5), from), JsonNode.class);
        assertEquals(HttpStatus.BAD_REQUEST, equal.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, reversed.getStatusCode());
        assertNotNull(equal.getBody());
        assertEquals(400, equal.getBody().get("status").asInt());
    }

    @Test
    @DisplayName("Правая граница периода не включается")
    void upperBoundIsExcluded() {
        createTicket("ровно на границе", Priority.LOW, null);
        Instant createdAt = TestClockConfiguration.START;
        JsonNode included = summary(createdAt, createdAt.plusSeconds(1));
        JsonNode excluded = summary(createdAt.plusSeconds(1), createdAt.plusSeconds(2));
        assertEquals(1, included.get("createdCount").asInt());
        assertEquals(0, excluded.get("createdCount").asInt());
    }

    @Test
    @DisplayName("Без параметров периода отчёт возвращает 400")
    void missingParameterIs400() {
        ResponseEntity<JsonNode> response = http.getForEntity("/api/reports/summary", JsonNode.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    private JsonNode summary(Instant from, Instant to) {
        ResponseEntity<JsonNode> response = http.getForEntity(summaryUri(from, to), JsonNode.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }

    private URI summaryUri(Instant from, Instant to) {
        return UriComponentsBuilder.fromPath("/api/reports/summary")
                .queryParam("from", from.toString())
                .queryParam("to", to.toString())
                .build()
                .toUri();
    }
}
