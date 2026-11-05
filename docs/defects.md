# Дефекты

## DEF-001. Правка завершённой заявки проходила успешно

Статус: исправлен. Серьёзность: высокая. Требование: FR-5, US-3.

Первая проверка `update` отклоняла только статус `CANCELLED`. Заявка в `RESOLVED` оставалась редактируемой: описание и приоритет менялись, исключение не выбрасывалось.

Шаги:

1. Создать заявку и довести её до `RESOLVED`.
2. Отправить `PATCH /api/tickets/{id}` с новым описанием и приоритетом.
3. Прочитать заявку снова.

Ожидание: `409`, описание, приоритет, статус и история не меняются.

Факт до исправления: метод `TicketService.update` завершался без ошибки. Модульный тест `TicketServiceTest.regressionResolvedTicketIsNotModified` упал так:

```text
TicketServiceTest > DEF-001: завершённую заявку нельзя изменить FAILED
org.opentest4j.AssertionFailedError: Expected ru.tushkov.helpdesk.web.TicketNotEditableException to be thrown, but nothing was thrown.
    at ru.tushkov.helpdesk.ticket.TicketServiceTest.regressionResolvedTicketIsNotModified(TicketServiceTest.java:148)
```

Исправление: запрет стоит на оба конечных статуса через `TicketStatus.isTerminal()`.

Регрессия:

- `TicketServiceTest.regressionResolvedTicketIsNotModified` проверяет, что объект не меняется и сохранение не вызывается;
- `TicketLifecycleE2EIT.regressionForbiddenOperationDoesNotChangeStoredTicket` проверяет то же на PostgreSQL: после `409` описание, статус, время закрытия и число записей истории совпадают с состоянием до запроса.
