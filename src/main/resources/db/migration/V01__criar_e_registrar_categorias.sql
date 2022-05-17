CREATE TABLE categoria
(
    codigo bigint primary key generated always as identity,
    nome   varchar(50) not null
);

insert into categoria (nome)
values ('Lazer');
insert into categoria (nome)
values ('Alimentação');
insert into categoria (nome)
values ('Supermercado');
insert into categoria (nome)
values ('Farmácia');
insert into categoria (nome)
values ('Outros');