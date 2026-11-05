package ru.tushkov.helpdesk.report

import org.springframework.stereotype.Service
import ru.tushkov.helpdesk.ticket.OverduePolicy
import ru.tushkov.helpdesk.ticket.Priority
import ru.tushkov.helpdesk.ticket.TicketStatus
import ru.tushkov.helpdesk.web.InvalidReportPeriodException
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Service
class ReportService(
    private val source: TicketReportSource,
    private val clock: Clock,
    private val overduePolicy: OverduePolicy,
) {
    fun summary(
        from: Instant,
        to: Instant,
    ): ReportSummary {
        if (!from.isBefore(to)) {
            throw InvalidReportPeriodException(from, to)
        }
        val tickets = source.findCreatedBetween(from, to).map { it.toMeasured() }
        val now = clock.instant()
        val resolvedDurations =
            tickets
                .filter { it.status == TicketStatus.RESOLVED }
                .mapNotNull { ticket ->
                    ticket.closedAt?.let { closed ->
                        Duration.between(ticket.createdAt, closed).toMillis() / 1000.0
                    }
                }
        return ReportSummary(
            createdCount = tickets.size.toLong(),
            byStatus =
                TicketStatus.values().associateWith { status ->
                    tickets.count { it.status == status }.toLong()
                },
            byPriority =
                Priority.values().associateWith { priority ->
                    tickets.count { it.priority == priority }.toLong()
                },
            openOverdueCount = tickets.count { overduePolicy.isOverdue(it.status, it.dueAt, now) }.toLong(),
            averageResolutionSeconds = resolvedDurations.takeIf { it.isNotEmpty() }?.average(),
        )
    }
}

private data class MeasuredTicket(
    val status: TicketStatus,
    val priority: Priority,
    val createdAt: Instant,
    val dueAt: Instant?,
    val closedAt: Instant?,
)

private fun TicketSnapshot.toMeasured(): MeasuredTicket =
    MeasuredTicket(
        status = status,
        priority = priority,
        createdAt = createdAt,
        dueAt = dueAt,
        closedAt = closedAt,
    )
