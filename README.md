# Сервис заявок технической поддержки

Сервер принимает заявки, ведёт статус и историю и считает сводку за период. Java отвечает за заявки, Kotlin считает отчёт `GET /api/reports/summary`.

![ci](https://github.com/DKolxoznik/helpdesk/actions/workflows/ci.yml/badge.svg)

## Что нужно

- JDK 17
- Docker Desktop
- Порты 8080 и 5432 свободны, либо другие значения в `.env`

Версии инструментов записаны в [docs/design.md](docs/design.md).

## Запуск

```powershell
Copy-Item .env.example .env
docker compose up --build -d
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Готовность: http://localhost:8080/actuator/health/readiness

Демонстрационные заявки загружаются профилем `docker`. Остановка без потери данных: `docker compose stop`. Удаление вместе с томом: `docker compose down -v`.

Сборка без Docker, если PostgreSQL уже запущен:

```powershell
.\gradlew.bat bootRun
```

Адрес базы берётся из `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` и `SPRING_DATASOURCE_PASSWORD`.

## Проверка

```powershell
.\gradlew.bat test jacocoTestReport
```

48 автоматических проверок: правила статуса, просрочка, сводка, REST и PostgreSQL через Testcontainers. План — [docs/test-plan.md](docs/test-plan.md), последний локальный итог — [docs/test-results/summary.md](docs/test-results/summary.md).

Коллекция запросов: [docs/api/helpdesk.http](docs/api/helpdesk.http) и [docs/api/helpdesk.postman_collection.json](docs/api/helpdesk.postman_collection.json). Контракт описан в [docs/api.md](docs/api.md).

## Копия и восстановление

```powershell
powershell -File scripts/backup.ps1
powershell -File scripts/restore.ps1 -Dump backups\helpdesk-YYYYMMDD-HHMMSS.dump
```

Восстановление создаёт отдельную базу `helpdesk_restore`. Подробности, три сценария сбоя и откат образа — в [docs/operations.md](docs/operations.md).

## CI/CD

Workflow `.github/workflows/ci.yml`:

1. Получает код и ставит JDK 17.
2. Проверяет форматирование Java и Kotlin (`spotlessCheck`).
3. Собирает проект и запускает тесты.
4. Публикует отчёты тестов и JaCoCo артефактом.
5. Для push в `main` после успешных тестов собирает образ и публикует его в GHCR с тегом SHA коммита и тегом `main`.
6. Запускает этот образ через Compose и проверяет его скриптом `scripts/smoke.sh`.

Pull request останавливается на тестах и не разворачивает стенд. Если тесты падают, публикация и развёртывание не запускаются. Пароль базы на стенде хранится в секрете `POSTGRES_PASSWORD`.

Возврат к предыдущему образу: Actions → `rollback` → SHA коммита. Миграции только добавляют объекты, поэтому старый образ совместим с текущей схемой.

## Документы

- [Спецификация](docs/specification.md)
- [Проектирование и backlog](docs/design.md)
- [Дефекты](docs/defects.md)
- [Отчёт](docs/report.md)
