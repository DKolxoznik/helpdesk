# Тест-план

Проверки привязаны к историям и требованиям. Время задаёт управляемые часы `MutableClock` и `Clock.fixed`, поэтому календарная дата запуска не влияет на результат. Перед каждым интеграционным тестом заявки и история очищаются, категории из миграции остаются. Параллельный запуск JUnit выключен.

## Модульные проверки

| Проверка | Требование | Тест |
| --- | --- | --- |
| `NEW → IN_PROGRESS`, `IN_PROGRESS → RESOLVED` | FR-4, US-4 | `StatusTransitionPolicyTest` |
| Запрет `NEW → RESOLVED`, отмены закрытой и повторного открытия | FR-4, FR-5 | `StatusTransitionPolicyTest` |
| Тот же запрет со стороны Kotlin на Java-классе | FR-5 | `StatusTransitionPolicyKotlinTest` |
| Просрочка до срока, в момент срока и после | FR-7 | `OverduePolicyTest` |
| Закрытая и заявка без срока не просрочены | FR-7 | `OverduePolicyTest` |
| Пустой заголовок, пустая категория, пустой приоритет | US-1, FR-3 | `CreateTicketValidationTest` |
| Создание ставит `NEW` и первую запись истории | FR-2, US-5 | `TicketServiceTest.createAssignsNewAndHistory` |
| Неизвестная категория не сохраняет заявку | US-7 | `TicketServiceTest.unknownCategory` |
| Нет заявки | FR-10 | `TicketServiceTest.missingTicketReturnsNotFound` |
| Запрещённый переход не пишет историю | FR-4, FR-6 | `TicketServiceTest.forbiddenTransitionDoesNotWriteHistory` |
| Решение и отмена ставят время закрытия | US-4 | `TicketServiceTest` |
| Регрессия DEF-001: правка `RESOLVED` не меняет объект | FR-5 | `TicketServiceTest.regressionResolvedTicketIsNotModified` |
| Пустая выборка, нет `RESOLVED`, неверный период | US-8, FR-9 | `ReportServiceTest` |
| Среднее не включает отменённые, просрочка считается | US-8, FR-7 | `ReportServiceTest` |
| Один вызов источника на всю сводку | FR-9 | `ReportServiceTest` |

## Интеграционные проверки

PostgreSQL 16 поднимается через Testcontainers. Миграции Flyway применяются к пустой базе.

| Проверка | Требование | Тест |
| --- | --- | --- |
| Миграция справочника | US-7 | `TicketApiIT.migrationsSeedFourCategories` |
| Создание и чтение | US-1, US-2 | `TicketApiIT.createAndGetRoundTrip` |
| `400` и пустая таблица после ошибки | FR-10 | `TicketApiIT.blankTitleIs400` |
| `404` | FR-10 | `TicketApiIT.missingTicketIs404` |
| Фильтр, поиск и страница | FR-8, US-6 | `TicketApiIT.filterByStatusAndPaginate` |
| История в базе после смены статуса | FR-6, US-5 | `TicketApiIT.statusChangeWritesHistoryInDatabase` |
| `409` без изменения строки | FR-4 | `TicketApiIT.directResolveIs409` |
| Правка трёх полей | US-3 | `TicketApiIT.updateDescriptionPriorityAndDueDate` |
| Граница срока в API и фильтре | FR-7 | `TicketApiIT.overdueAtExactDueInstant` |
| Неверная страница | US-6 | `TicketApiIT.invalidPageIs400` |
| Пустая сводка и `null` | US-8 | `ReportApiIT.emptyPeriodHasNullAverage` |
| Неверный период | US-8 | `ReportApiIT.invalidPeriodIs400` |
| Граница `[from, to)` | FR-9 | `ReportApiIT.upperBoundIsExcluded` |
| Нет параметров периода | US-8 | `ReportApiIT.missingParameterIs400` |

## Сквозные сценарии

| Сценарий | Требование | Тест |
| --- | --- | --- |
| Создание, работа, решение, сводка со средним 7200 секунд | US-4, US-8 | `TicketLifecycleE2EIT.fullLifecycleThenSummary` |
| Отмена, затем правка и повторное открытие не меняют строку и историю | FR-5 | `TicketLifecycleE2EIT.regressionForbiddenOperationDoesNotChangeStoredTicket` |

## Граничные случаи из задания

| Случай | Где проверяется |
| --- | --- |
| Пустая выборка | `ReportServiceTest`, `ReportApiIT.emptyPeriodHasNullAverage` |
| Нет завершённых заявок | `ReportServiceTest` |
| Момент наступления срока | `OverduePolicyTest.trueAtDeadline`, `TicketApiIT.overdueAtExactDueInstant` |
| Неверный период | `ReportServiceTest`, `ReportApiIT.invalidPeriodIs400` |
| Неизвестный идентификатор | `TicketServiceTest`, `TicketApiIT.missingTicketIs404` |

Команда локального прогона: `gradlew.bat test jacocoTestReport`. Отчёт JaCoCo: `build/reports/jacoco/test/html/index.html`. В CI те же отчёты публикуются артефактом `reports-<sha>`.
