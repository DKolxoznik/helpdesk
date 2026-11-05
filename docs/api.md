# Контракт API

Базовый URL локального стенда: `http://localhost:8080`.

Интерактивная схема: `http://localhost:8080/swagger-ui.html`.

Коллекция запросов: `docs/api/helpdesk.postman_collection.json` и `docs/api/helpdesk.http`.

Время передаётся в ISO-8601, UTC, например `2026-06-15T12:00:00Z`.

## Общая ошибка

```json
{
  "timestamp": "2026-06-15T12:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Переход статуса NEW -> RESOLVED запрещён",
  "path": "/api/tickets/2d9c0a2e-1e4a-4e0a-9c1a-6e0a2d9c0a2e/status",
  "details": []
}
```

Для `400` по полям тела `details` содержит `field` и `message`.

## Категории

`GET /api/categories`

```json
[
  {
    "id": "11111111-1111-1111-1111-111111111111",
    "code": "NETWORK",
    "name": "Сеть"
  }
]
```

Остальные фиксированные категории: `SOFTWARE` (`22222222-2222-2222-2222-222222222222`), `HARDWARE` (`33333333-3333-3333-3333-333333333333`), `ACCESS` (`44444444-4444-4444-4444-444444444444`).

## Заявки

### Создать

`POST /api/tickets` отвечает `201`.

```json
{
  "title": "Не печатает принтер",
  "description": "Очередь зависла",
  "categoryId": "33333333-3333-3333-3333-333333333333",
  "priority": "MEDIUM",
  "dueAt": "2026-12-01T18:00:00Z"
}
```

`dueAt` можно не передавать. В ответе карточка заявки: поля заявки, `overdue` и `history`.

### Прочитать

`GET /api/tickets/{id}` отвечает `200` или `404`.

### Изменить описание, приоритет и срок

`PATCH /api/tickets/{id}`

```json
{
  "description": "уточнённое описание",
  "priority": "HIGH",
  "dueAt": "2026-12-02T18:00:00Z",
  "clearDueAt": false
}
```

Поле, которого нет в JSON, не меняется. Строка из пробелов очищает описание. `clearDueAt: true` очищает срок. Закрытая или отменённая заявка отвечает `409`.

### Сменить статус

`POST /api/tickets/{id}/status`

```json
{ "status": "IN_PROGRESS" }
```

Запрещённый переход отвечает `409`.

### Список

`GET /api/tickets?status=NEW&priority=HIGH&categoryId={uuid}&overdue=true&q=принтер&page=0&size=20`

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

Сортировка: сначала более новые, при равном времени создания — по идентификатору.

## Сводка

`GET /api/reports/summary?from=2026-06-01T00:00:00Z&to=2026-07-01T00:00:00Z`

Интервал полуоткрытый: заявка со временем создания, равным `to`, в сводку не входит.

```json
{
  "createdCount": 2,
  "byStatus": {
    "NEW": 1,
    "IN_PROGRESS": 0,
    "RESOLVED": 1,
    "CANCELLED": 0
  },
  "byPriority": {
    "LOW": 0,
    "MEDIUM": 1,
    "HIGH": 1
  },
  "openOverdueCount": 1,
  "averageResolutionSeconds": 7200.0
}
```

`averageResolutionSeconds` равен `null`, если в выборке нет заявок `RESOLVED` с заполненным временем закрытия. Ноль означает, что решение заняло ноль секунд, и это другое значение.
