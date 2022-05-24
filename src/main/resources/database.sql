create table task
(
    id          serial
                constraint table_name_pk
                primary key,
    type        varchar not null,
    input_data  varchar not null
);

create unique index table_name_id_uindex
    on task (id);

-- TODO clear after coping in bd
insert into task (id, type, input_data) values
        (1, 'magic square', '1 2 3, 4 5 6, 7 8 9'),
        (2, 'substrings', 'arp live strong, lively alive harp sharp armstrong'),
        (3, 'substrings', 'tarp mice bull, lively alive harp sharp armstrong')

