create table player(
  id uuid default random_uuid() primary key,
  username text unique not null,
  first_name text not null,
  last_name text not null,
  wallet_cents bigint not null,
  version bigint default 0 not null
);

create table transaction(
  id uuid default random_uuid() primary key,
  player_id uuid references player not null,
  timestamp_utc timestamp not null,
  amount_cents bigint not null,
  type text not null
);
