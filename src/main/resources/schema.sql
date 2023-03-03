create table player(
  id uuid default random_uuid() primary key,
  username text unique not null,
  first_name text not null,
  last_name text not null,
  balance_cents bigint not null,
  version bigint default 0 not null
);
