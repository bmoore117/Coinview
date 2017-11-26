
# --- !Ups

create table users (
    id integer primary key --probably end up being the hash of user profile from face, googs, etc.
);

--inheritance based approach to types of currency
create table currency (
    slug varchar(5) primary key,
    name varchar(20)
);
create table fiat (slug varchar(5) primary key) inherits (currency);
create table coin (slug varchar(5) primary key) inherits (currency);

create table purchases (
    id integer primary key,
    user_id integer,
    coin_slug varchar(5),
    coin_amount double precision,
    purchase_date timestamp,
    purchase_currency_slug varchar(5),
    purchase_currency_amount double precision,

    constraint user_id_fk foreign key (user_id) references users(id),
    constraint coin_type_fk foreign key (coin_slug) references coin(slug),
    constraint currency_type_fk foreign key (purchase_currency_slug) references currency(slug),
    check (coin_slug <> purchase_currency_slug)
);

create table historical_prices (
    coin_slug varchar(5),
    price_date timestamp,
    price double precision,
    price_units varchar(5),

    constraint historical_purchases_pk primary key (coin_slug, price_date),
    constraint coin_type_fk foreign key (coin_slug) references coin(slug),
    constraint price_type_fk foreign key (price_units) references currency(slug)
);

insert into currency values ('USD', 'Dollar');
insert into fiat values ('USD', 'Dollar');
insert into currency values ('BTC', 'Bitcoin');
insert into coin values ('BTC', 'Bitcoin');
insert into currency values ('ETH', 'Ethereum');
insert into coin values ('ETH', 'Ethereum');
insert into currency values ('BCH', 'Bitcoin Cash');
insert into coin values ('BCH', 'Bitcoin Cash');