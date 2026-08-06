alter table accounts alter column opened_at type timestamptz using opened_at at time zone 'UTC';
alter table outbox   alter column occurred_at type timestamptz using occurred_at at time zone 'UTC';