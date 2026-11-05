insert into ticket (id, title, description, category_id, priority, status, created_at, due_at, closed_at) values
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1',
        'Не открывается корпоративная почта',
        'После смены пароля Outlook показывает ошибку входа.',
        '44444444-4444-4444-4444-444444444444',
        'HIGH',
        'NEW',
        '2026-10-28T09:00:00Z',
        '2026-12-15T18:00:00Z',
        null
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2',
        'Нет связи в переговорной',
        'Пропадает сеть на этаже 3.',
        '11111111-1111-1111-1111-111111111111',
        'MEDIUM',
        'IN_PROGRESS',
        '2026-10-10T08:30:00Z',
        '2026-10-20T18:00:00Z',
        null
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3',
        'Установить обновление редактора',
        'Нужна новая версия на рабочем месте аналитика.',
        '22222222-2222-2222-2222-222222222222',
        'LOW',
        'RESOLVED',
        '2026-10-05T10:00:00Z',
        '2026-10-12T18:00:00Z',
        '2026-10-07T15:00:00Z'
    ),
    (
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4',
        'Заменить клавиатуру',
        'Заявка снята: сотрудник уехал в командировку.',
        '33333333-3333-3333-3333-333333333333',
        'LOW',
        'CANCELLED',
        '2026-10-08T11:00:00Z',
        '2026-10-18T18:00:00Z',
        '2026-10-08T12:00:00Z'
    );

insert into ticket_status_history (id, ticket_id, from_status, to_status, changed_at) values
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', null, 'NEW', '2026-10-28T09:00:00Z'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb2', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', null, 'NEW', '2026-10-10T08:30:00Z'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb3', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'NEW', 'IN_PROGRESS', '2026-10-10T09:00:00Z'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb4', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', null, 'NEW', '2026-10-05T10:00:00Z'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb5', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'NEW', 'IN_PROGRESS', '2026-10-06T10:00:00Z'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb6', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'IN_PROGRESS', 'RESOLVED', '2026-10-07T15:00:00Z'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb7', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', null, 'NEW', '2026-10-08T11:00:00Z'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb8', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'NEW', 'CANCELLED', '2026-10-08T12:00:00Z');
