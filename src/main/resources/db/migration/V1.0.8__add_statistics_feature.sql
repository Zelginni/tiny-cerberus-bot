alter table cerberus.chat add column statistics_enabled boolean;

update cerberus.chat set statistics_enabled = false;
