create table survey (
    id          varchar(36) primary key,
    ekstern_id  varchar not null,
    opphav      varchar not null,
    type        varchar not null,
    status      varchar not null,
    opprettet   timestamp not null,
    endret      timestamp,
    gyldig_til  timestamp,
    unique (ekstern_id, opphav, type)
);

create table tilleggsinfo (
    id          varchar(36) primary key,
    survey      varchar(36) not null references survey (id),
    type        varchar not null,
    data        jsonb not null default '{}'::jsonb
);

create table tema (
    id          varchar(36) primary key,
    ekstern_id  varchar not null,
    survey      varchar(36) not null references survey (id),
    navn        varchar
);

create table sporsmal (
    id          varchar(36) not null primary key,
    ekstern_id  varchar not null,
    tema        varchar(36) not null references tema (id),
    flervalg    boolean not null default false,
    tekst       varchar not null,
    kategori    varchar default null
);

create table svaralternativ (
    id          varchar(36) not null primary key,
    ekstern_id  varchar not null,
    sporsmal    varchar(36) references sporsmal (id),
    tekst       varchar not null
);
