package ru.tushkov.helpdesk.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.tushkov.helpdesk.ticket.CreateTicketRequest;
import ru.tushkov.helpdesk.ticket.Priority;
import ru.tushkov.helpdesk.ticket.TicketDetails;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.main.banner-mode=off")
@Import(TestClockConfiguration.class)
public abstract class ApiTestSupport {

    protected static final UUID SOFTWARE = UUID.fromString("22222222-2222-2222-2222-222222222222");

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine").withStartupTimeout(Duration.ofMinutes(3));

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected TestRestTemplate http;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected MutableClock clock;

    @BeforeEach
    protected void cleanTickets() {
        jdbc.update("delete from ticket_status_history");
        jdbc.update("delete from ticket");
        clock.set(TestClockConfiguration.START);
    }

    protected TicketDetails createTicket(String title, Priority priority, Instant dueAt) {
        ResponseEntity<TicketDetails> response = http.postForEntity(
                "/api/tickets",
                new CreateTicketRequest(title, "описание", SOFTWARE, priority, dueAt),
                TicketDetails.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        return response.getBody();
    }
}
