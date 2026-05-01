create table cerberus.telegram_known_chat_members (
    chat_id bigint not null,
    user_id bigint not null,
    display_name varchar(255) not null,
    last_seen_at timestamp with time zone not null,
    constraint pk_telegram_known_chat_members primary key (chat_id, user_id)
);
