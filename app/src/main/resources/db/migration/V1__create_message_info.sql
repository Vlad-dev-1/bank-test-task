create table message_info
(
    message_id                  uuid not null
        primary key,
    content                     varchar(255),
    message_time_status_changed timestamp with time zone,
    status_message              varchar(255)
        constraint message_info_status_message_check
            check ((status_message)::text = ANY
                   ((ARRAY ['RECEIVED'::character varying, 'PROCESSING'::character varying, 'PROCESSED'::character varying, 'FAILED'::character varying])::text[])),
    message_time                timestamp with time zone
);