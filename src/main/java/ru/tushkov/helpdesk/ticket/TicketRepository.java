package ru.tushkov.helpdesk.ticket;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    @Query("select t from Ticket t where t.createdAt >= :from and t.createdAt < :to")
    List<Ticket> findCreatedBetween(@Param("from") Instant from, @Param("to") Instant to);
}
