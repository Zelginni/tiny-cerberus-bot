create table if not exists cerberus.chat_ignored_statistics_topics
(
    chat_id           bigint not null,
    message_thread_id int    not null,
    primary key (chat_id, message_thread_id),
    foreign key (chat_id) references cerberus.chat (id)
);
