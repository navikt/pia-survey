alter table tilleggsinfo
    drop constraint tilleggsinfo_survey_fkey,
    add constraint tilleggsinfo_survey_fkey foreign key (survey) references survey(id) on delete cascade;

alter table tema
    drop constraint tema_survey_fkey,
    add constraint tema_survey_fkey foreign key (survey) references survey(id) on delete cascade;

alter table sporsmal
    drop constraint sporsmal_tema_fkey,
    add constraint sporsmal_tema_fkey foreign key (tema) references tema(id) on delete cascade;

alter table svaralternativ
    drop constraint svaralternativ_sporsmal_fkey,
    add constraint svaralternativ_sporsmal_fkey foreign key (sporsmal) references sporsmal(id) on delete cascade;
