create table if not exists cerberus.bayan
(
    id       identity,
    response varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

merge into cerberus.bayan (id, response) VALUES
                                             (1, 'В анусе у тебя баян!'),
                                             (2, 'В штанах у тебя баян!'),
                                             (3, 'Очко твое баян!');

alter table cerberus.chat add column bayan_enabled boolean;

update cerberus.chat set bayan_enabled = false;