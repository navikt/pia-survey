CREATE TABLE deltaker(
  sesjon_id varchar(36) primary key,
  survey varchar(36) not null references survey (id) on DELETE cascade
);