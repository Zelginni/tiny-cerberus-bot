create schema if not exists cerberus;

create table if not exists cerberus.chat (
        id BIGINT GENERATED BY DEFAULT AS IDENTITY ,
        name varchar(300),
        telegram_id varchar(300) NOT NULL,
        enabled boolean,
        warn_limit int,
        PRIMARY KEY(id)
);

create table if not exists cerberus.chat_user (
        id BIGINT GENERATED BY DEFAULT AS IDENTITY ,
        telegram_id varchar(300) NOT NULL,
        username varchar(300),
        chat_id int NOT NULL,
        PRIMARY KEY(id),
        FOREIGN KEY(chat_id) REFERENCES chat(id)
);

create table if not exists cerberus.warn (
        id BIGINT GENERATED BY DEFAULT AS IDENTITY ,
        user_id int NOT NULL,
        date_created TIMESTAMP,
        author_telegram_id varchar(300) NOT NULL,
        author_username varchar(300),
        PRIMARY KEY(id),
        FOREIGN KEY(user_id) REFERENCES chat_user(id)
)