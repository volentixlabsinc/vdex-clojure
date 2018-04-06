create table transactions (
    id integer primary key autoincrement,	
    from_address text not null,
    to_address text not null,
    amount text not null,
    token_address text not null,
    message text,
    mempool boolean default 1,
    confirmed_at datetime,
    created_at datetime default current_timestamp);
