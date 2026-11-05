# Результат прогона

Дата прогона: 2026-11-05. Команда: `gradlew.bat test jacocoTestReport`. Результат: 48 тестов, 0 падений.

| Набор | Тестов |
| --- | --- |
| `StatusTransitionPolicyTest` | 8 |
| `OverduePolicyTest` | 6 |
| `CreateTicketValidationTest` | 3 |
| `TicketServiceTest` | 7 |
| `StatusTransitionPolicyKotlinTest` | 2 |
| `ReportServiceTest` | 5 |
| `TicketApiIT` | 11 |
| `ReportApiIT` | 4 |
| `TicketLifecycleE2EIT` | 2 |

Покрытие JaCoCo по инструкции байткода: 1544 из 1702, 90,7%. По строкам: 310 из 351, 88,3%. Пакет `ru.tushkov.helpdesk.ticket`: 95,9% строк. Пакет `ru.tushkov.helpdesk.report`, включая Kotlin-сводку: 100% строк.

Полный HTML-отчёт собирается локально в `build/reports/jacoco/test/html` и в CI публикуется артефактом. Каталог `build` в репозиторий не входит.
