package ru.tushkov.helpdesk.report

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import ru.tushkov.helpdesk.ticket.OverduePolicy
import ru.tushkov.helpdesk.ticket.Priority
import ru.tushkov.helpdesk.ticket.TicketStatus
import ru.tushkov.helpdesk.web.InvalidReportPeriodException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class ReportServiceTest {
    private val now = Instant.parse("2026-06-15T12:00:00Z")
    private val clock: Clock = Clock.fixed(now, ZoneOffset.UTC)
    private val overduePolicy = OverduePolicy()

    @Test
    @DisplayName("Пустая выборка даёт нули и null в среднем времени")
    fun `empty sample returns zeros and null average`() {
        val service = service { _, _ -> emptyList() }
        val summary = service.summary(now.minusSeconds(3600), now)

        assertEquals(0L, summary.createdCount)
        assertNull(summary.averageResolutionSeconds)
        assertEquals(0L, summary.openOverdueCount)
        assertEquals(TicketStatus.values().size, summary.byStatus.size)
        assertEquals(0L, summary.byStatus.getValue(TicketStatus.RESOLVED))
        assertEquals(0L, summary.byPriority.getValue(Priority.HIGH))
    }

    @Test
    @DisplayName("Одна выборка считает статусы, приоритеты, просрочку и среднее")
    fun `one sample feeds every metric and skips cancelled tickets`() {
        val createdAt = now.minusSeconds(10_000)
        val tickets =
            listOf(
                snapshot(TicketStatus.NEW, Priority.LOW, createdAt, now.minusSeconds(1), null),
                snapshot(TicketStatus.IN_PROGRESS, Priority.HIGH, createdAt, now.plusSeconds(60), null),
                snapshot(TicketStatus.RESOLVED, Priority.MEDIUM, createdAt, null, createdAt.plusSeconds(3600)),
                snapshot(TicketStatus.RESOLVED, Priority.LOW, createdAt, null, createdAt.plusSeconds(10_800)),
                snapshot(TicketStatus.CANCELLED, Priority.HIGH, createdAt, null, createdAt.plusSeconds(100)),
            )
        val summary = service { _, _ -> tickets }.summary(createdAt, now.plusSeconds(1))

        assertEquals(5L, summary.createdCount)
        assertEquals(1L, summary.byStatus.getValue(TicketStatus.NEW))
        assertEquals(1L, summary.byStatus.getValue(TicketStatus.IN_PROGRESS))
        assertEquals(2L, summary.byStatus.getValue(TicketStatus.RESOLVED))
        assertEquals(1L, summary.byStatus.getValue(TicketStatus.CANCELLED))
        assertEquals(2L, summary.byPriority.getValue(Priority.LOW))
        assertEquals(1L, summary.byPriority.getValue(Priority.MEDIUM))
        assertEquals(2L, summary.byPriority.getValue(Priority.HIGH))
        assertEquals(1L, summary.openOverdueCount)
        assertEquals(7200.0, summary.averageResolutionSeconds)
    }

    @Test
    @DisplayName("Если завершённых заявок нет, среднее время равно null")
    fun `average is null when nothing is resolved`() {
        val createdAt = now.minusSeconds(100)
        val tickets =
            listOf(
                snapshot(TicketStatus.CANCELLED, Priority.LOW, createdAt, null, createdAt.plusSeconds(50)),
                snapshot(TicketStatus.NEW, Priority.MEDIUM, createdAt, now.plusSeconds(10), null),
            )
        val summary = service { _, _ -> tickets }.summary(createdAt, now.plusSeconds(1))

        assertEquals(2L, summary.createdCount)
        assertNull(summary.averageResolutionSeconds)
        assertEquals(0L, summary.openOverdueCount)
    }

    @Test
    @DisplayName("Период, где from не раньше to, отклоняется")
    fun `invalid period is rejected before reading tickets`() {
        var calls = 0
        val source =
            TicketReportSource { _, _ ->
                calls += 1
                emptyList()
            }
        val service = ReportService(source, clock, overduePolicy)

        assertThrows(InvalidReportPeriodException::class.java) {
            service.summary(now, now)
        }
        assertThrows(InvalidReportPeriodException::class.java) {
            service.summary(now.plusSeconds(1), now)
        }
        assertEquals(0, calls)
    }

    @Test
    @DisplayName("Все показатели считаются по одному обращению к источнику")
    fun `summary reads the requested period once`() {
        val from = now.minusSeconds(3600)
        val to = now
        val source = mockk<TicketReportSource>()
        every { source.findCreatedBetween(from, to) } returns emptyList()

        ReportService(source, clock, overduePolicy).summary(from, to)

        verify(exactly = 1) { source.findCreatedBetween(from, to) }
    }

    private fun service(loader: (Instant, Instant) -> List<TicketSnapshot>): ReportService =
        ReportService(
            TicketReportSource { from, to -> loader(from, to) },
            clock,
            overduePolicy,
        )

    private fun snapshot(
        status: TicketStatus,
        priority: Priority,
        createdAt: Instant,
        dueAt: Instant?,
        closedAt: Instant?,
    ): TicketSnapshot = TicketSnapshot(UUID.randomUUID(), status, priority, createdAt, dueAt, closedAt)
}
