CREATE SCHEMA IF NOT EXISTS public;

create table public.messages
(
    message_id                  uuid not null
        primary key,
    content                     text,
    message_time_status_changed timestamp with time zone,
    status_message              varchar(255)
        constraint messages_status_message_check
            check ((status_message)::text = ANY
                   ((ARRAY ['PROCESSED'::character varying, 'FAILED'::character varying])::text[])),
    message_time                timestamp with time zone
);