package ru.tushkov.helpdesk.ticket

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class StatusTransitionPolicyKotlinTest {
    private val policy = StatusTransitionPolicy()

    @Test
    @DisplayName("Kotlin-тест Java-компонента: завершённую заявку нельзя вернуть в работу")
    fun `java policy rejects reopening a resolved ticket`() {
        assertFalse(policy.canTransit(TicketStatus.RESOLVED, TicketStatus.IN_PROGRESS))
    }

    @Test
    @DisplayName("Kotlin-тест Java-компонента: из NEW можно перейти в работу")
    fun `java policy allows new to in progress`() {
        assertTrue(policy.canTransit(TicketStatus.NEW, TicketStatus.IN_PROGRESS))
    }
}
