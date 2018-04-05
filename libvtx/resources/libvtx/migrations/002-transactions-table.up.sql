create table transactions (
    from_account text not null,
    to_account text not null,
    amount text not null,
    token_address text not null,
    message text,
    mempool boolean default 1);
