create table balances (
  address text not null,
  token_address text,
  balance text not null,
  primary key (address, token_address));
