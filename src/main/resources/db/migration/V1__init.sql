create table category (
    id uuid primary key,
    code varchar(50) not null,
    name varchar(200) not null,
    constraint uk_category_code unique (code)
);

create table ticket (
    id uuid primary key,
    title varchar(200) not null,
    description varchar(4000),
    category_id uuid not null,
    priority varchar(16) not null,
    status varchar(16) not null,
    created_at timestamptz not null,
    due_at timestamptz,
    closed_at timestamptz,
    constraint fk_ticket_category foreign key (category_id) references category (id),
    constraint ck_ticket_priority check (priority in ('LOW', 'MEDIUM', 'HIGH')),
    constraint ck_ticket_status check (status in ('NEW', 'IN_PROGRESS', 'RESOLVED', 'CANCELLED'))
);

create table ticket_status_history (
    id uuid primary key,
    ticket_id uuid not null,
    from_status varchar(16),
    to_status varchar(16) not null,
    changed_at timestamptz not null,
    constraint fk_history_ticket foreign key (ticket_id) references ticket (id),
    constraint ck_history_from_status check (
        from_status is null or from_status in ('NEW', 'IN_PROGRESS', 'RESOLVED', 'CANCELLED')
    ),
    constraint ck_history_to_status check (to_status in ('NEW', 'IN_PROGRESS', 'RESOLVED', 'CANCELLED'))
);

create index idx_ticket_created_at on ticket (created_at);
create index idx_ticket_status on ticket (status);
create index idx_ticket_category on ticket (category_id);
create index idx_history_ticket on ticket_status_history (ticket_id, changed_at);
