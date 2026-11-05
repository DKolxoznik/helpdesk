# Эксплуатация

## Запуск

Нужны Docker Desktop и файл настроек:

```powershell
Copy-Item .env.example .env
docker compose up --build -d
```

Приложение: `http://localhost:8080`. Готовность: `http://localhost:8080/actuator/health/readiness`. Пока база не примет подключения, readiness равен `DOWN`. Схема и демонстрационные заявки применяются сами, профилем `docker`.

Логи контейнера идут в stdout:

```powershell
docker compose logs -f app
docker compose logs -f postgres
```

Остановка без удаления данных: `docker compose stop`. Данные лежат в томе `pgdata`. Полное удаление вместе с томом: `docker compose down -v`.

## Сохранность после перезапуска

```powershell
docker compose restart
docker compose exec postgres psql -U helpdesk -d helpdesk -c "select count(*) from ticket;"
```

Число заявок до и после совпадает, потому что каталог PostgreSQL смонтирован в том.

## Резервная копия

Копия снимается из работающего контейнера `postgres` в каталог `backups/`. Каталог не попадает в git.

```powershell
powershell -File scripts/backup.ps1
powershell -File scripts/restore.ps1 -Dump backups\helpdesk-YYYYMMDD-HHMMSS.dump
```

Восстановление идёт в отдельную базу `helpdesk_restore`, рабочая база не перезаписывается. На Linux те же действия выполняют `scripts/backup.sh` и `scripts/restore.sh`.

Проверка:

```powershell
docker compose exec postgres psql -U helpdesk -d helpdesk_restore -c "select count(*) from ticket;"
```

## Диагностика

### База недоступна

Признаки: `docker compose ps` показывает `postgres` не `healthy`, у приложения readiness `DOWN`, в логах приложения есть `Connection refused` или таймаут Hikari. Приложение ждёт базу до 60 секунд.

Что сделать:

1. `docker compose logs postgres` — смотреть, поднялся ли кластер и не кончилось ли место.
2. `docker compose ps` — жив ли healthcheck `pg_isready`.
3. Если контейнер базы падает по кругу, не удалять том. Снять логи, затем `docker compose up -d postgres`.
4. Когда база станет `healthy`, перезапустить приложение: `docker compose restart app`.

### Приложение не проходит health check

Признаки: база `healthy`, у `app` статус `unhealthy`, запрос readiness не возвращает `UP`.

Что сделать:

1. `docker compose logs app` — исключение при старте, ошибка миграции или неверный пароль.
2. Сверить `SPRING_DATASOURCE_PASSWORD` и `POSTGRES_PASSWORD`. Они должны совпадать.
3. Проверить, что порт `8080` на хосте не занят другим процессом.
4. После исправления настроек: `docker compose up -d --force-recreate app`.

### Миграция завершается ошибкой

Признаки: в логах Flyway есть `Migration ... failed`, приложение не становится готовым. В `flyway_schema_history` последняя строка может быть с `success = false`.

Что сделать:

1. Не менять уже применённый файл `V1`–`V3`. Исправление оформляется следующим номером версии.
2. По тексту ошибки понять, это повтор объекта или порванные данные.
3. Если миграция записалась как неуспешная, восстановить базу из копии. Для учебной базы это надёжнее ручной правки истории Flyway.
4. Повторный старт: `docker compose up -d app`.

## Откат приложения

Образы публикуются в GHCR с тегом SHA коммита и подвижным тегом `main`. Схема меняется только аддитивно, поэтому предыдущий образ запускается на текущей базе.

Локально:

```powershell
$env:HELPDESK_IMAGE = "ghcr.io/dkolxoznik/helpdesk:<sha-предыдущего-коммита>"
docker compose up -d --no-build --pull always
```

Том `pgdata` при этом не удаляется.

В GitHub то же действие запускает workflow `rollback`: Actions, workflow rollback, SHA коммита. Workflow поднимает указанный образ, выполняет `scripts/smoke.sh` и останавливает учебный стенд. Токен публикации берётся из `GITHUB_TOKEN`. Пароль учебной базы передаётся секретом `POSTGRES_PASSWORD` и в репозиторий не записывается.

Публикация образа и развёртывание в `ci.yml` запускаются только после успешных тестов и только для push в `main`. Pull request останавливается на проверке форматирования и тестах.
