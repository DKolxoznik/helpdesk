package ru.tushkov.helpdesk.report

import com.fasterxml.jackson.annotation.JsonInclude
import ru.tushkov.helpdesk.ticket.Priority
import ru.tushkov.helpdesk.ticket.TicketStatus

@JsonInclude(JsonInclude.Include.ALWAYS)
data class ReportSummary(
    val createdCount: Long,
    val byStatus: Map<TicketStatus, Long>,
    val byPriority: Map<Priority, Long>,
    val openOverdueCount: Long,
    val averageResolutionSeconds: Double?,
)
