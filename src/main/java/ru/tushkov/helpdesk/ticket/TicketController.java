package ru.tushkov.helpdesk.ticket;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.tushkov.helpdesk.web.PageResponse;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService tickets;

    public TicketController(TicketService tickets) {
        this.tickets = tickets;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDetails create(@Valid @RequestBody CreateTicketRequest request) {
        return tickets.create(request);
    }

    @GetMapping("/{id}")
    public TicketDetails get(@PathVariable UUID id) {
        return tickets.get(id);
    }

    @PatchMapping("/{id}")
    public TicketDetails update(@PathVariable UUID id, @Valid @RequestBody UpdateTicketRequest request) {
        return tickets.update(id, request);
    }

    @PostMapping("/{id}/status")
    public TicketDetails changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeStatusRequest request) {
        return tickets.changeStatus(id, request.status());
    }

    @GetMapping
    public PageResponse<TicketSummary> list(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return tickets.list(new TicketFilter(status, priority, categoryId, overdue, q), page, size);
    }
}
