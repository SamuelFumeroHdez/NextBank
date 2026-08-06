create table accounts (
    id          varchar(64)     primary key,
    customer_id varchar(64)     not null,
    iban        varchar(34)     not null unique,
    currency    varchar(3)      not null,
    balance     numeric(19,4)   not null,
    opened_at   timestamp       not null
);

create index idx_accounts_customer_id on accounts (customer_id);

create table outbox (
    event_id       varchar(64)  primary key,
    event_type     varchar(128) not null,
    aggregate_id   varchar(64)  not null,
    aggregate_type varchar(64)  not null,
    payload        text         not null,
    occurred_at    timestamp    not null,
    published      boolean      not null default false
);

create index idx_outbox_unpublished on outbox (published, occurred_at) where published = false;